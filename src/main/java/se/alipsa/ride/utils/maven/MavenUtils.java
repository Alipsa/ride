package se.alipsa.ride.utils.maven;

import static se.alipsa.ride.menu.GlobalOptions.ADD_BUILDDIR_TO_CLASSPATH;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.io.DefaultSettingsReader;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.apache.maven.settings.validation.DefaultSettingsValidator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.ConsoleRepositoryEventListener;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Since we have a dependency on AetherPackageLoader we use the same
 * Aether versions here. The versions of Aether are rather old and Aether has moved to apache
 * Consider submitting a PR to Renjin upgrading to the apache equivalents:
 * org.apache.maven.resolver:maven-resolver-api
 * org.apache.maven.resolver:maven-resolver-util
 *
 * Since the package name org.eclipse.aether is still kept in the apache version
 * just upgrading these in Ride will probably not work (will conflict with AetherPackageLoader)
 */
public class MavenUtils {

  private static final Logger log = LogManager.getLogger();

   /**
    * Note:
    * The maven classloader must have Ride as parent otherwise executing R scripts will result in classes mixed from
    * different classloaders and will not work as executing the scripts depend on Renjin script engine loaded by Ride.
    * This has the unfortunate consequence that Ride dependencies will influence the result and not be as "pure" as
    * when just executing the pom.xml.
    */
  public static ClassLoader getMavenClassLoader(Model project, Collection<File> dependencies, ClassLoader parent) throws Exception {
    List<String> classpathElements = getClassPathElements(project);
    List<URL> urls = new ArrayList<>();
    for (int i = 0; i < classpathElements.size(); ++i) {
      String elem = classpathElements.get(i);
      if (elem == null) {
        continue;
      }
      URL url = new File(elem).toURI().toURL();
      urls.add(url);
      log.debug("Adding {} to classloader", url);
    }

    for (File dep : dependencies) {
      if (dep != null && dep.exists()) {
        URL url = dep.toURI().toURL();
        urls.add(url);
        log.debug("Adding {} to classloader", url);
      }
    }
    return new URLClassLoader(urls.toArray(new URL[0]), parent);
  }

  public static List<String> getClassPathElements(Model project) {
    List<String> classpathElements = new ArrayList<>();
    if (Ride.instance() == null || Ride.instance().getPrefs().getBoolean(ADD_BUILDDIR_TO_CLASSPATH, true)) {
      classpathElements.add(project.getBuild().getOutputDirectory());
      classpathElements.add(project.getBuild().getTestOutputDirectory());
    }
    return classpathElements;
  }

  public static ClassLoader getMavenDependenciesClassloader(File pomFile, ClassLoader possibleParent) throws Exception {
    return getMavenClassLoader(parsePom(pomFile), resolveDependencies(pomFile), possibleParent);
  }

  public static InvocationResult runMaven(final File pomFile, String[] mvnArgs, InvocationOutputHandler consoleOutputHandler, InvocationOutputHandler warningOutputHandler) throws MavenInvocationException {
    InvocationRequest request = new DefaultInvocationRequest();
    request.setBatchMode(true);
    request.setPomFile( pomFile );
    request.setGoals(Arrays.asList(mvnArgs) );
    File dir = pomFile.getParentFile();
    request.setBaseDirectory(dir);
    log.info("Running maven from dir {} with args {}", dir, String.join(" ", mvnArgs));
    Invoker invoker = new DefaultInvoker();
    invoker.setOutputHandler(consoleOutputHandler);
    invoker.setErrorHandler(warningOutputHandler);
    return invoker.execute( request );
  }


  public static Set<File> resolveDependencies(File pomFile) throws SettingsBuildingException, ModelBuildingException {
    RepositorySystem repositorySystem = getRepositorySystem();
    RepositorySystemSession repositorySystemSession = getRepositorySystemSession(repositorySystem);

    Model model = parsePom(pomFile);
    List<RemoteRepository> repositories = getRepositories(model);
    Set<File> dependencies = new HashSet<>();
    log.trace("Maven model resolved: {}, parsing its dependencies...", model);
    for (org.apache.maven.model.Dependency d : model.getDependencies()) {
      log.trace("processing dependency: {}", d);
      Artifact artifact = new DefaultArtifact(d.getGroupId(), d.getArtifactId(), d.getType(), d.getVersion());

      ///// Resolve main + transient
      log.debug("resolving {}:{}:{}...", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
      CollectRequest collectRequest = new CollectRequest(new Dependency(artifact, JavaScopes.COMPILE), repositories);
      DependencyFilter filter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
      DependencyRequest request = new DependencyRequest(collectRequest, filter);

      DependencyResult result = null;
      try {
        result = repositorySystem.resolveDependencies(repositorySystemSession, request);
      } catch (DependencyResolutionException | RuntimeException e) {
        log.warn("Error resolving dependent artifact: {}:{}:{}", d.getGroupId(), d.getArtifactId(), d.getVersion(), e);
        continue;
      }

      for (ArtifactResult artifactResult : result.getArtifactResults()) {
        Artifact art = artifactResult.getArtifact();
        log.debug("artifact {} resolved to {}", art, art.getFile());
        dependencies.add(art.getFile());
      }
    }
    /////
    return dependencies;
  }

  public static Model parsePom(File pomFile) throws ModelBuildingException {
    final DefaultModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest()
       .setPomFile(pomFile);

    //ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();
    ModelBuilder modelBuilder = new ParentPomsAsDependencyModelBuilder();
    ModelBuildingResult modelBuildingResult = modelBuilder.build(modelBuildingRequest);

    return modelBuildingResult.getEffectiveModel();
  }


  public static RepositorySystem getRepositorySystem() {
    DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
    serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);

    serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    serviceLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
        log.warn("Error creating Maven service", exception);
      }
    });

    return serviceLocator.getService(RepositorySystem.class);
  }

  public static DefaultRepositorySystemSession getRepositorySystemSession(RepositorySystem system) throws SettingsBuildingException {
    DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();
    LocalRepository localRepository = getLocalRepository();
    repositorySystemSession.setLocalRepositoryManager(
       system.newLocalRepositoryManager(repositorySystemSession, localRepository));

    repositorySystemSession.setRepositoryListener(new ConsoleRepositoryEventListener());

    return repositorySystemSession;
  }

  public static LocalRepository getLocalRepository() throws SettingsBuildingException {
    Settings settings = getSettings();
    String localRepoPath = settings.getLocalRepository();

    if (localRepoPath != null) {
      localRepoPath = localRepoPath.replace("${user.home}", FileUtils.getUserHome().getAbsolutePath());
    } else {
      localRepoPath = new File(FileUtils.getUserHome(), ".m2/repository").getAbsolutePath();
    }
    return new LocalRepository(localRepoPath);
  }

  public static Settings getSettings() throws SettingsBuildingException {
    DefaultSettingsBuilder defaultSettingsBuilder = new DefaultSettingsBuilder();
    DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
    File userSettingsFile = new File(FileUtils.getUserHome(), ".m2/settings.xml");
    if (userSettingsFile.exists()) {
      request.setUserSettingsFile(userSettingsFile);
    } else {
      log.warn("Did not find a settings.xml in {}", userSettingsFile.getAbsolutePath() );
    }
    String m2Home = System.getenv("M2_HOME") != null ? System.getenv("M2_HOME") : System.getenv("MAVEN_HOME");
    if (m2Home != null) {
      File globalSettingsFile = new File(m2Home, "conf/settings.xml");
      if (globalSettingsFile.exists()) {
        request.setGlobalSettingsFile(globalSettingsFile);
      }
    }

    defaultSettingsBuilder.setSettingsWriter(new DefaultSettingsWriter());
    defaultSettingsBuilder.setSettingsReader(new DefaultSettingsReader());
    defaultSettingsBuilder.setSettingsValidator(new DefaultSettingsValidator());
    SettingsBuildingResult build = defaultSettingsBuilder.build(request);
    return build.getEffectiveSettings();
  }


  public static List<RemoteRepository> getRepositories(Model model) {
    List<RemoteRepository> repos = new ArrayList<>();
    model.getRepositories().forEach(r ->
       repos.add(new RemoteRepository.Builder(r.getId(), r.getLayout(), r.getUrl()).build()));
    RemoteRepository central = getCentralMavenRepository();

    Iterator<RemoteRepository> it = repos.iterator();
    boolean addCentral = true;
    while (it.hasNext()) {
      RemoteRepository repo = it.next();
      if (repo.getUrl().equals("http://repo.maven.apache.org/maven2")) {
        it.remove();
      }
      if (repo.getId().equals(central.getId()) || repo.getUrl().equals(central.getUrl())) {
        addCentral = false;
      }
    }
    if (addCentral) {
      repos.add(central);
    }
    return repos;
  }

  private static RemoteRepository getCentralMavenRepository() {
    return new RemoteRepository.Builder("central", "default", "https://central.maven.org/maven2/")
       .build();
  }
}
