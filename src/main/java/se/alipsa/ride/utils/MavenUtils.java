package se.alipsa.ride.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public class MavenUtils {

  private static final Logger log = LogManager.getLogger();

  public static MavenProject createMavenProject(File pomFile) throws Exception {

    if (pomFile == null || !pomFile.exists()) {
      throw new FileNotFoundException("Pom file " + pomFile + " does not exist");
    }

    MavenXpp3Reader mavenreader = new MavenXpp3Reader();
    FileReader reader = new FileReader(pomFile);
    Model model = mavenreader.read(reader);
    model.setPomFile(pomFile);
    MavenProject project = new MavenProject(model);
    return project;
  }

  public static ClassLoader getMavenClassLoader(MavenProject project) throws Exception {
    List<String> classpathElements = project.getCompileClasspathElements();
    classpathElements.add(project.getBuild().getOutputDirectory());
    classpathElements.add(project.getBuild().getTestOutputDirectory());
    URL[] urls = new URL[classpathElements.size()];
    for (int i = 0; i < classpathElements.size(); ++i) {
      urls[i] = new File(classpathElements.get(i)).toURI().toURL();
    }
    return new URLClassLoader(urls, MavenUtils.class.getClassLoader());
  }

  public static ClassLoader getMavenDependenciesClassloader(File pomFile) throws Exception {
    return getMavenClassLoader(createMavenProject(pomFile));
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
}
