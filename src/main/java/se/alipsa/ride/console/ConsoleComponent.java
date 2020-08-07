package se.alipsa.ride.console;

import static se.alipsa.ride.Constants.ICON_HEIGHT;
import static se.alipsa.ride.Constants.ICON_WIDTH;
import static se.alipsa.ride.Constants.INDENT;
import static se.alipsa.ride.menu.GlobalOptions.ADD_BUILDDIR_TO_CLASSPATH;
import static se.alipsa.ride.menu.GlobalOptions.USE_MAVEN_CLASSLOADER;
import static se.alipsa.ride.utils.StringUtils.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.aether.repository.RemoteRepository;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.RenjinVersion;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.aether.ConsoleRepositoryListener;
import org.renjin.aether.ConsoleTransferListener;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import se.alipsa.ride.Ride;
import se.alipsa.ride.TaskListener;
import se.alipsa.ride.code.rtab.RTab;
import se.alipsa.ride.environment.EnvironmentComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.maven.MavenUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import javax.script.ScriptException;

public class ConsoleComponent extends BorderPane {

  public static final Repo RENJIN_REPO = asRepo(AetherFactory.renjinRepo());
  public static final Repo MVN_CENTRAL_REPO = asRepo(AetherFactory.mavenCentral());
  public static final String REMOTE_REPOSITORIES_PREF = "ConsoleComponent.RemoteRepositories";
  public static final String PACKAGE_LOADER_PREF = "ConsoleComponent.PackageLoader";
  private static final Image IMG_RUNNING = new Image(Objects.requireNonNull(FileUtils
      .getResourceUrl("image/running.png")).toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  private static final Image IMG_WAITING = new Image(Objects.requireNonNull(FileUtils
      .getResourceUrl("image/waiting.png")).toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  private static final String DOUBLE_INDENT = INDENT + INDENT;
  private static final Logger log = LogManager.getLogger(ConsoleComponent.class);
  private RenjinScriptEngine engine;
  private Session session;
  private final ImageView runningView;
  private final Button statusButton;
  private final ConsoleTextArea console;
  private final Ride gui;
  private List<RemoteRepository> remoteRepositories;
  private PackageLoader packageLoader;
  private Thread runningThread;
  private File workingDir;
  private Map<Thread, String> threadMap = new HashMap<>();

  public ConsoleComponent(Ride gui) {
    this.gui = gui;
    console = new ConsoleTextArea(gui);
    console.setEditable(false);

    Button clearButton = new Button("Clear");
    clearButton.setOnAction(e -> {
      console.clear();
      console.appendText(">");
    });
    FlowPane topPane = new FlowPane();
    topPane.setPadding(new Insets(1, 10, 1, 5));
    topPane.setHgap(10);

    runningView = new ImageView();
    statusButton = new Button();
    statusButton.setOnAction(e -> interruptProcess());
    statusButton.setGraphic(runningView);
    waiting();

    topPane.getChildren().addAll(statusButton, clearButton);
    setTop(topPane);

    VirtualizedScrollPane<ConsoleTextArea> vPane = new VirtualizedScrollPane<>(console);
    vPane.setMaxWidth(Double.MAX_VALUE);
    vPane.setMaxHeight(Double.MAX_VALUE);
    setCenter(vPane);
    //initRenjin(getStoredRemoteRepositories(), Thread.currentThread().getContextClassLoader());
  }

  private static Repo asRepo(RemoteRepository repo) {
    return new Repo(repo.getId(), repo.getContentType(), repo.getUrl());
  }

  public void initRenjin(ClassLoader parentClassLoader) {
    Platform.runLater(() -> initRenjin(getStoredRemoteRepositories(), parentClassLoader));
  }

  // TODO: run this is a separate thread since pom parsing takes some time and the GUI freezes for a while as a result
  private void initRenjin(List<Repo> repos, ClassLoader parentClassLoader) {
    String version = "unknown";

    try {

      remoteRepositories = new ArrayList<>();
      remoteRepositories.addAll(asRemoteRepositories(repos));

      if (gui.getInoutComponent() == null) {
        log.warn("InoutComponent is null, timing is off");
        throw new RuntimeException("intiRenjin called too soon, InoutComponent is null, timing is off");
      }

      log.info("USE_MAVEN_CLASSLOADER pref is set to {}", gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false));

      ClassLoader cl = parentClassLoader;

      if (gui.getInoutComponent() != null && gui.getInoutComponent().getRoot() != null &&
        gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false)) {
        File pomFile = new File(gui.getInoutComponent().getRootDir(), "pom.xml");
        if (pomFile.exists()) {
          log.info("Parsing pom to use maven classloader");
          cl = MavenUtils.getMavenDependenciesClassloader(pomFile, parentClassLoader);
        } else {
          log.info("Use maven class loader is set but pomfile {} does not exist", pomFile);
        }
      } else {
        File wd = gui.getInoutComponent().getRootDir();
        if (gui.getPrefs().getBoolean(ADD_BUILDDIR_TO_CLASSPATH, true) && wd != null && wd.exists()) {
          File classesDir = new File(wd, "target/classes");
          List<URL> urlList = new ArrayList<>();
          try {
            if (classesDir.exists()) {
              urlList.add(classesDir.toURI().toURL());
            }
            File testClasses = new File(wd, "target/test-classes");
            if (testClasses.exists()) {
              urlList.add(testClasses.toURI().toURL());
            }
          } catch (MalformedURLException e) {
            e.printStackTrace();
          }
          if (urlList.size() > 0) {
            log.info("Adding compile dirs to classloader: {}", urlList);
            cl = new URLClassLoader(urlList.toArray(new URL[0]), cl);
          }
        }
      }

      PackageLoader loader = getPackageLoader(cl);

      if (loader instanceof AetherPackageLoader) {
        cl = ((AetherPackageLoader) loader).getClassLoader();
      }

      SessionBuilder builder = new SessionBuilder();
      session = builder
          .withDefaultPackages()
          .setPackageLoader(loader) // allows library to work without having to include in the pom
          .setClassLoader(cl) //allows imports in r code to work
          .build();

      if (workingDir != null && workingDir.exists()) {
        session.setWorkingDirectory(workingDir);
      }
      // TODO: after implementing a javafx grafics device do session.getOptions().set("device", theGraphicsDevice);
      //GrDevice grDevice = new GrDevice();
      //session.getOptions().set("device", grDevice);

      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
      engine = factory.getScriptEngine(session);

      version = RenjinVersion.getVersionName();
    } catch (Exception e) {
      ExceptionAlert.showAlert("Failed to load Renjin ScriptEngine", e);
    }

    String greeting = "* Renjin " + version + " *";
    String surround = getStars(greeting.length());
    console.append(surround);
    console.append(greeting);
    console.append(surround + "\n>", true);
  }

  private PackageLoader getPackageLoader(ClassLoader parentClassLoader) {
    /*
    We always want to reinitialize the package loader in case a remote repo was added
    not sure why i cached this in the first place, so will keep it here for a while in case something unwanted happens...
    if (packageLoader != null) {
      return packageLoader;
    }
    */
    String pkgLoaderName;
    String overridePackageLoader = System.getProperty(PACKAGE_LOADER_PREF);
    if (overridePackageLoader != null) {
      pkgLoaderName = overridePackageLoader;
    } else {
      pkgLoaderName = gui.getPrefs().get(PACKAGE_LOADER_PREF, AetherPackageLoader.class.getSimpleName());
    }
    PackageLoader loader = packageLoaderForName(parentClassLoader, pkgLoaderName);
    setPackageLoader(loader);
    return loader;
  }

  public PackageLoader getPackageLoader() {
    return packageLoader;
  }

  public void setPackageLoader(PackageLoader loader) {
    //packageLoader = packageLoaderForName(Thread.currentThread().getContextClassLoader(), loader.getSimpleName());
    packageLoader = loader;
    gui.getPrefs().put(PACKAGE_LOADER_PREF, loader.getClass().getSimpleName());
  }

  public PackageLoader packageLoaderForName(ClassLoader parentClassLoader, String pkgLoaderName) {
    if (ClasspathPackageLoader.class.getSimpleName().equals(pkgLoaderName)) {
      return new ClasspathPackageLoader(parentClassLoader);
    }
    //log.info("parentClassLoader = {}, remoteRepositories = {}", parentClassLoader, remoteRepositories);
    AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, remoteRepositories);
    /*
    System.out.println(
        log.getName()
        + "\ntraceEnabled = " + log.isTraceEnabled()
        + "\ndebugEnabled = " + log.isDebugEnabled()
        + "\ninfoEnabled  = " + log.isInfoEnabled()
        + "\nwarnEnabled  = " + log.isWarnEnabled()
        + "\nerrorEnabled = " + log.isErrorEnabled()
    );
     */
    if (log.isDebugEnabled()) {
      log.debug("DEBUG enabled, package loading activities will be echoed to console");
      loader.setRepositoryListener(new ConsoleRepositoryListener(System.out));
      loader.setTransferListener(new ConsoleTransferListener(System.out));
    }
    return loader;
  }

  private List<RemoteRepository> asRemoteRepositories(List<Repo> items) {
    List<RemoteRepository> list = new ArrayList<>();
    for (Repo repo : items) {
      list.add(new RemoteRepository.Builder(repo.getId(), repo.getType(), repo.getUrl()).build());
    }
    return list;
  }

  private List<Repo> asRepos(List<RemoteRepository> repositories) {
    List<Repo> list = new ArrayList<>();
    for (RemoteRepository repo : repositories) {
      list.add(asRepo(repo));
    }
    return list;
  }

  private List<Repo> getStoredRemoteRepositories() {
    List<Repo> list = new ArrayList<>();
    String remotes = gui.getPrefs().get(REMOTE_REPOSITORIES_PREF, null);
    log.debug("Remotes from prefs are: {}", remotes);
    if (remotes == null) {
      log.info("No stored prefs for remote repos, adding defaults");
      addDefaultRepos(list);
      return list;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      list = mapper.readValue(remotes, new TypeReference<List<Repo>>() {
      });
    } catch (InvalidDefinitionException e) {
      e.printStackTrace();
      log.warn("Something is wrong with the pref key {}, deleting it to start fresh", REMOTE_REPOSITORIES_PREF);
      gui.getPrefs().remove(REMOTE_REPOSITORIES_PREF);
      addDefaultRepos(list);
    } catch (IOException e) {
      e.printStackTrace();
      addDefaultRepos(list);
    }
    return list;
  }

  private void addDefaultRepos(List<Repo> list) {
    log.info("add local repo");
    String localRepoPath = System.getProperty("localRepository"); // c:/Users/blah/.m2/repository
    Repo local = new Repo("local", "default", "file:" + localRepoPath);
    list.add(local);
    log.info("add renjin repo");
    list.add(RENJIN_REPO);
    log.info("add maven central repo");
    list.add(MVN_CENTRAL_REPO);
  }

  private String getStars(int length) {
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append("*");
    }
    return buf.toString();
  }

  public void restartR() {
    console.append("Restarting Renjin..\n");
    //initRenjin(getStoredRemoteRepositories(), Thread.currentThread().getContextClassLoader());
    initRenjin(getStoredRemoteRepositories(), gui.getClass().getClassLoader());
    gui.getEnvironmentComponent().clearEnvironment();
  }

  /**
   * TODO: while we can stop the timeline with this we cannot interrupt the scriptengines eval.
   */
  @SuppressWarnings("deprecation")
  public void interruptProcess() {
    log.info("Interrupting runnning process");
    // This is a nasty piece of code but a brutal stop() is the only thing that will break out of the script engine
    if (runningThread != null && runningThread.isAlive()) {
      console.appendFx("\nInterrupting process...");
      runningThread.interrupt();
      // allow two seconds for graceful shutdown
      sleep(2000);
      console.appendFx("\nStopping process...");
      runningThread.stop();
      threadMap.remove(runningThread);
      console.appendText("\n>");
    }
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      log.info("Sleep was interrupted");
    }
  }

  public SEXP runScriptSilent(String script) throws Exception {
    try (PrintWriter out = new PrintWriter(System.out);
         PrintWriter err = new PrintWriter(System.err)) {
      running();
      log.debug("Running script: {}", script);
      session.setStdOut(out);
      session.setStdErr(err);
      SEXP sexp = (SEXP) engine.eval(script);
      waiting();
      return sexp;
    } catch (Exception e) {
      log.warn("Failed to run script: {}", script, e);
      waiting();
      throw e;
    }
  }

  public SEXP fetchVar(String varName) {
    Environment global = getSession().getGlobalEnvironment();
    Context topContext = getSession().getTopLevelContext();
    return global.getVariable(topContext, varName);
  }

  public void runScriptAsync(String script, String title, TaskListener taskListener) {

    running();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          taskListener.taskStarted();
          executeScriptAndReport(script, title);
        } catch (RuntimeException e) {
          // RuntimeExceptions (such as EvalExceptions is not caught so need to wrap all in an exception
          // this way we can get to the original one by extracting the cause from the thrown exception
          System.out.println("Exception caught, rethrowing as wrapped Exception");
          throw new Exception(e);
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      taskListener.taskEnded();
      waiting();
      updateEnvironment();
      promptAndScrollToEnd();
    });
    task.setOnFailed(e -> {
      taskListener.taskEnded();
      waiting();
      updateEnvironment();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }

      String msg = createMessageFromEvalException(ex);

      ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
      promptAndScrollToEnd();
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    startThreadWhenOthersAreFinished(thread, "runScriptAsync: " + title);
  }

  public String createMessageFromEvalException(Throwable ex) {
    String msg = "";

    if (ex instanceof org.renjin.parser.ParseException) {
      msg = "Error parsing R script: ";
    } else if (ex instanceof ScriptException || ex instanceof EvalException) {
      msg = "Error running R script: ";
    } else if (ex instanceof RuntimeException) {
      msg = "An unknown error occurred running R script: ";
    } else if (ex instanceof IOException) {
      msg = "Failed to close writer capturing renjin results";
    } else if (ex instanceof RuntimeScriptException) {
      msg = "An unknown error occurred running R script: ";
    } else if (ex instanceof Exception) {
      msg = "Exception thrown when running script";
    }
    return msg;
  }

  public void promptAndScrollToEnd() {
    console.appendText(">");
    scrollToEnd();
  }

  public void scrollToEnd() {
    console.moveTo(console.getLength());
    console.requestFollowCaret();
  }

  private void updateEnvironment() {
    Environment global = session.getGlobalEnvironment();
    Context topContext = session.getTopLevelContext();
    gui.getEnvironmentComponent().setEnvironment(global, topContext);
    StringVector pkgs = null;
    try {
      pkgs = (StringVector) engine.eval("(.packages())");

    } catch (ScriptException e) {
      ExceptionAlert.showAlert("Error updating environment", e);
    }
    gui.getInoutComponent().setPackages(pkgs);

    // TODO consider setting the working dir in filetree after each run as setwd() night have changed it
    // Below is how to get it:
    // log.info("Working dir is {}", engine.getSession().getWorkingDirectory().getName().getPath());
  }

  public void runTests(RTab rTab) {
    running();
    String script = rTab.getTextContent();
    String title = rTab.getTitle();
    TaskListener taskListener = rTab;
    if (script.contains("testthat")) {
      runTestthatTests(rTab);
    } else {
      runHamcrestTests(script, title, taskListener);
    }
  }

  private void runTestthatTests(RTab rTab) {
    String script = rTab.getTextContent();
    String title = rTab.getTitle();
    TaskListener taskListener = rTab;
    File file = rTab.getFile();
    console.append("");
    console.append("Running testthat tests");
    console.append("----------------------");

    if (file == null || !file.exists()) {
      console.append("Unable to determine script location, you must save the R script first.");
      return;
    }

    Task<Void> task = new Task<Void>() {

      long start;
      long end;

      @Override
      public Void call() {
        taskListener.taskStarted();
        start = System.currentTimeMillis();
        engine.put("inout", gui.getInoutComponent());
        List<TestResult> results = new ArrayList<>();
        try (StringWriter out = new StringWriter();
             StringWriter err = new StringWriter();
             PrintWriter outputWriter = new PrintWriter(out);
             PrintWriter errWriter = new PrintWriter(err)
        ) {
          session.setStdOut(outputWriter);
          session.setStdErr(errWriter);
          FileObject orgWd = session.getWorkingDirectory();
          File scriptDir = file.getParentFile();
          console.appendFx(DOUBLE_INDENT  + "- Setting working directory to " + scriptDir);
          session.setWorkingDirectory(scriptDir);

          TestResult result = runTest(script, title);
          console.appendFx(DOUBLE_INDENT + "- Setting working directory back to " + orgWd);
          session.setWorkingDirectory(orgWd);
          results.add(result);
          Platform.runLater(() -> printResult(title, out, err, result, DOUBLE_INDENT));

          end = System.currentTimeMillis();
          Map<TestResult.OutCome, List<TestResult>> resultMap = results.stream()
             .collect(Collectors.groupingBy(TestResult::getResult));

          List<TestResult> successResults = resultMap.get(TestResult.OutCome.SUCCESS);
          List<TestResult> failureResults = resultMap.get(TestResult.OutCome.FAILURE);
          List<TestResult> errorResults = resultMap.get(TestResult.OutCome.ERROR);
          long successCount = successResults == null ? 0 : successResults.size();
          long failCount = failureResults == null ? 0 : failureResults.size();
          long errorCount = errorResults == null ? 0 : errorResults.size();

          String duration = DurationFormatUtils.formatDuration(end - start, "mm 'minutes, 'ss' seconds, 'SSS' millis '");
          console.appendFx("\nR tests summary:");
          console.appendFx("----------------");
          console.appendFx(format("Tests run: {}, Successes: {}, Failures: {}, Errors: {}",
             results.size(), successCount, failCount, errorCount));
          console.appendFx("Time: " + duration + "\n");
        } catch (IOException e) {
          console.appendWarningFx("Failed to run test");
          ExceptionAlert.showAlert("Failed to run test", e);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      taskListener.taskEnded();
      waiting();
      updateEnvironment();
      promptAndScrollToEnd();
    });
    task.setOnFailed(e -> {
      taskListener.taskEnded();
      waiting();
      updateEnvironment();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }

      String msg = createMessageFromEvalException(ex);

      ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
      promptAndScrollToEnd();
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    startThreadWhenOthersAreFinished(thread, "runTestthatTests: " + title);
  }

  private void runHamcrestTests(String script, String title, TaskListener taskListener) {
    console.append("");
    console.append("Running hamcrest tests");
    console.append("----------------------");

    Task<Void> task = new Task<Void>() {

      long start;
      long end;

      @Override
      public Void call() {
        taskListener.taskStarted();
        start = System.currentTimeMillis();
        engine.put("inout", gui.getInoutComponent());
        List<TestResult> results = new ArrayList<>();
        try (StringWriter out = new StringWriter();
             StringWriter err = new StringWriter();
             PrintWriter outputWriter = new PrintWriter(out);
             PrintWriter errWriter = new PrintWriter(err)
        ) {
          session.setStdOut(outputWriter);
          session.setStdErr(errWriter);
          TestResult result = runTest(script, title);
          results.add(result);
          Platform.runLater(() -> printResult(title, out, err, result, DOUBLE_INDENT));

          //now run each testFunction in that file, in the same Session
          for (Symbol name : session.getGlobalEnvironment().getSymbolNames()) {
            String methodName = name.getPrintName().trim();
            if (methodName.startsWith("test.")) {
              SEXP value = session.getGlobalEnvironment().getVariable(session.getTopLevelContext(), name);
              if (isNoArgsFunction(value)) {
                Context context = session.getTopLevelContext();
                TestResult funcResult = runTestFunction(context, title, name);
                results.add(result);
                Platform.runLater(() -> printResult(methodName, out, err, funcResult, DOUBLE_INDENT));
              }
            }
          }
          end = System.currentTimeMillis();
          Map<TestResult.OutCome, List<TestResult>> resultMap = results.stream()
              .collect(Collectors.groupingBy(TestResult::getResult));

          List<TestResult> successResults = resultMap.get(TestResult.OutCome.SUCCESS);
          List<TestResult> failureResults = resultMap.get(TestResult.OutCome.FAILURE);
          List<TestResult> errorResults = resultMap.get(TestResult.OutCome.ERROR);
          long successCount = successResults == null ? 0 : successResults.size();
          long failCount = failureResults == null ? 0 : failureResults.size();
          long errorCount = errorResults == null ? 0 : errorResults.size();

          String duration = DurationFormatUtils.formatDuration(end - start, "mm 'minutes, 'ss' seconds, 'SSS' millis '");
          console.appendFx("\nR tests summary:");
          console.appendFx("----------------");
          console.appendFx(format("Tests run: {}, Successes: {}, Failures: {}, Errors: {}",
              results.size(), successCount, failCount, errorCount));
          console.appendFx("Time: " + duration + "\n");
        } catch (IOException e) {
          console.appendWarningFx("Failed to run test");
          ExceptionAlert.showAlert("Failed to run test", e);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      taskListener.taskEnded();
       waiting();
       updateEnvironment();
       promptAndScrollToEnd();
   });
    task.setOnFailed(e -> {
      taskListener.taskEnded();
      waiting();
      updateEnvironment();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }

      String msg = createMessageFromEvalException(ex);

      ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
      promptAndScrollToEnd();
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    startThreadWhenOthersAreFinished(thread, "runHamcrestTests: " + title);
  }

  private void printResult(String title, StringWriter out, StringWriter err, TestResult result, String indent) {
    String lines = prefixLines(out, indent);
    if (!"".equals(lines.trim())) {
      console.append(lines);
    }
    out.getBuffer().setLength(0);
    lines = prefixLines(err, indent);
    if (!"".equals(lines.trim())) {
      console.append(lines);
    }
    err.getBuffer().setLength(0);
    if (TestResult.OutCome.SUCCESS.equals(result.getResult())) {
      console.append(indent + format("# {}: Success", title));
    } else {
      console.appendWarning(indent + format("# {}: Failure detected: {}", title, formatMessage(result.getError())));
    }
  }

  private String prefixLines(StringWriter out, String prefix) {
    StringBuilder buf = new StringBuilder();
    String lines = out == null ? "" : out.toString();
    for(String line : lines.trim().split("\n")) {
      buf.append(prefix).append(line).append("\n");
    }
    return prefix + buf.toString().trim();
  }


  private TestResult runTest(String script, String title, String... indentOpt) {
    String indent = INDENT;
    if (indentOpt.length > 0) {
      indent = indentOpt[0];
    }
    TestResult result = new TestResult(title);
    String issue;
    Exception exception;
    String testName = title;
    console.appendFx(indent + format("# Running test {}", title).trim());
    try {
      engine.eval(script);
      result.setResult(TestResult.OutCome.SUCCESS);
      return result;
    } catch (org.renjin.parser.ParseException e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " parsing R script " + testName;
    } catch (ScriptException | EvalException e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " executing test " + testName;
    } catch (RuntimeException e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " occurred running R script " + testName;
    } catch (Exception e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " thrown when running script " + testName;
    }
    result.setResult(TestResult.OutCome.FAILURE);
    result.setError(exception);
    result.setIssue(issue);
    return result;
  }

  private boolean isNoArgsFunction(final SEXP value) {
    if (value instanceof Closure) {
      Closure testFunction = (Closure) value;
      return testFunction.getFormals().length() == 0;
    }
    return false;
  }

  private String formatMessage(final Throwable error) {
    return error.getMessage().trim().replace("\n", ", ");
  }

  private TestResult runTestFunction(final Context context, final String title, final Symbol name) {
    String methodName = name.getPrintName().trim() + "()";
    String testName = title + ": " + methodName;
    console.appendFx(INDENT + format("# Running test function {} in {}", methodName, title).trim());
    String issue;
    Exception exception;
    TestResult result = new TestResult(title);
    try {
      context.evaluate(FunctionCall.newCall(name));
      result.setResult(TestResult.OutCome.SUCCESS);
      return result;
    } catch (EvalException e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " executing test " + testName;
    } catch (RuntimeException e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " occurred running R script " + testName;
    } catch (Exception e) {
      exception = e;
      issue = e.getClass().getSimpleName() + " thrown when running script " + testName;
    }
    result.setResult(TestResult.OutCome.FAILURE);
    result.setError(exception);
    result.setIssue(issue);
    return result;
  }

  private void executeScriptAndReport(String script, String title) throws Exception {

    EnvironmentComponent env = gui.getEnvironmentComponent();
    try (
         AppenderPrintWriter out = new AppenderPrintWriter(console);
         WarningAppenderWriter err = new WarningAppenderWriter(console);
         PrintWriter outputWriter = new PrintWriter(out);
         PrintWriter errWriter = new PrintWriter(err)
    ) {

      engine.put("inout", gui.getInoutComponent());

      Platform.runLater(() -> {
        console.append(title);
        env.addInputHistory(script);
      });

      session.setStdOut(outputWriter);
      session.setStdErr(errWriter);

      engine.eval(script);
      Platform.runLater(() -> env.addOutputHistory(out.toString()));
      postEvalOutput();

    } catch (ScriptException | RuntimeException e) {
      postEvalOutput();
      throw e;
    }
  }

  private void postEvalOutput() throws IOException {
    try (StringWriter warnStrWriter = new StringWriter();
         PrintWriter warnWriter = new PrintWriter(warnStrWriter)) {
      console.flush();
      session.setStdOut(warnWriter);
      session.printWarnings();
      Platform.runLater(() -> console.appendWarning(warnStrWriter.toString()));
      session.clearWarnings();
    }
  }

  public void addOutput(String title, String content, boolean addPrompt, boolean skipNewLine) {
    console.append(title);
    console.append(content, skipNewLine);
    if (addPrompt) {
      promptAndScrollToEnd();
    } else {
      scrollToEnd();
    }
  }

  public void addWarning(String title, String content, boolean addPrompt) {
    console.appendWarning(title);
    console.appendWarning(content);
    if (addPrompt) {
      promptAndScrollToEnd();
    } else {
      scrollToEnd();
    }
  }

  public List<Repo> getRemoteRepositories() {
    return asRepos(remoteRepositories);
  }

  /**
   * this should be called last as the Session is reinitialized at the end
   */
  public void setRemoteRepositories(List<Repo> repos, ClassLoader cl) {
    ObjectMapper mapper = new ObjectMapper();
    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, repos);
      String r = writer.toString();
      log.debug("Writing repos to prefs as: {}", r);
      gui.getPrefs().put(REMOTE_REPOSITORIES_PREF, r);
    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("initializing Renjin with {} repos", repos.size());
    initRenjin(repos, cl);
  }

  public void running() {
    Platform.runLater(() -> {
      runningView.setImage(IMG_RUNNING);
      statusButton.setTooltip(new Tooltip("Process is running, click to abort"));
      showTooltip(statusButton);
      gui.getMainMenu().enableInterruptMenuItem();
    });
    sleep(20);
  }

  public void waiting() {
    Platform.runLater(() -> {
      runningView.setImage(IMG_WAITING);
      statusButton.setTooltip(new Tooltip("Engine is idle"));
      gui.getMainMenu().disableInterruptMenuItem();
    });
  }

  private void showTooltip(Control control) {
    Tooltip customTooltip = control.getTooltip();
    Stage owner = gui.getStage();
    Point2D p = control.localToScene(10.0, 20.0);

    customTooltip.setAutoHide(true);

    customTooltip.show(owner, p.getX()
        + control.getScene().getX() + control.getScene().getWindow().getX(), p.getY()
        + control.getScene().getY() + control.getScene().getWindow().getY());

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      public void run() {
        Platform.runLater(customTooltip::hide);
      }
    };
    timer.schedule(task, 800);
  }

  public void setWorkingDir(File dir) {
    if (dir == null) {
      return;
    }
    try {
      if (session != null) {
        session.setWorkingDirectory(dir);
      }
      workingDir = dir;
    } catch (FileSystemException e) {
      log.warn("Error setting working dir to {} for session", dir, e);
    }
  }

  public Session getSession() {
    return session;
  }

  public void setConsoleMaxSize(int size) {
    console.setConsoleMaxSize(size);
  }

  public int getConsoleMaxSize() {
    return console.getConsoleMaxSize();
  }

  public ConsoleTextArea getConsole() {
    return console;
  }

  public void startThreadWhenOthersAreFinished(Thread thread, String context) {
    if (runningThread == null) {
      log.debug("Starting thread {}", context);
      thread.start();
    } else if (runningThread.getState() == Thread.State.WAITING || runningThread.getState() == Thread.State.TIMED_WAITING) {
      log.debug("Waiting for thread {} to finish", threadMap.get(runningThread));
      try {
        // This is bit ugly as now the console output will not show until the thread has finished.
        runningThread.join();
        thread.start();
      } catch (InterruptedException e) {
        log.warn("Thread was interrupted", e);
        log.info("Running thread {}", context);
        thread.start();
      }

    } else if (runningThread.isAlive() && runningThread.getState() != Thread.State.TERMINATED) {
      log.warn("There is already a process running: {} in state {}, Overriding existing running thread", threadMap.get(runningThread), runningThread.getState());
      thread.start();
    } else {
      if (runningThread.getState() != Thread.State.TERMINATED) {
        log.error("Missed some condition, running thread {} is {}", threadMap.get(runningThread), runningThread.getState());
      }
      thread.start();
    }
    threadMap.remove(runningThread);
    runningThread = thread;
    threadMap.put(thread, context);
  }

  public void busy() {
    this.setCursor(Cursor.WAIT);
    console.setCursor(Cursor.WAIT);
  }

  public void ready() {
    this.setCursor(Cursor.DEFAULT);
    console.setCursor(Cursor.DEFAULT);
  }

}
