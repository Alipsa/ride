package se.alipsa.ride.console;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.aether.repository.RemoteRepository;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.RenjinVersion;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.Animation;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import javax.script.ScriptException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.utils.StringUtils.format;

public class ConsoleComponent extends BorderPane {

  public static final Repo RENJIN_REPO = asRepo(AetherFactory.renjinRepo());
  public static final Repo MVN_CENTRAL_REPO = asRepo(AetherFactory.mavenCentral());
  public static final String REMOTE_REPOSITORIES_PREF = "ConsoleComponent.RemoteRepositories";
  public static final String PACKAGE_LOADER_PREF = "ConsoleComponent.PackageLoader";
  private static final Image IMG_RUNNING = new Image(FileUtils
      .getResourceUrl("image/running.png").toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  private static final Image IMG_WAITING = new Image(FileUtils
      .getResourceUrl("image/waiting.png").toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  private static final String DOUBLE_INDENT = INDENT + INDENT;
  private static final Logger log = LoggerFactory.getLogger(ConsoleComponent.class);
  private RenjinScriptEngine engine;
  private Session session;
  private ImageView runningView;
  private Button statusButton;
  private ConsoleTextArea console;
  private Ride gui;
  private List<RemoteRepository> remoteRepositories;
  private PackageLoader packageLoader;
  private Timeline scriptExecutionTimeline;
  private Thread scriptThread;

  public ConsoleComponent(Ride gui) {
    this.gui = gui;
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
    statusButton.setOnAction(e -> interruptR());
    statusButton.setGraphic(runningView);
    waiting();

    topPane.getChildren().addAll(statusButton, clearButton);
    setTop(topPane);

    console = new ConsoleTextArea(gui);
    console.setEditable(false);

    VirtualizedScrollPane<ConsoleTextArea> vPane = new VirtualizedScrollPane<>(console);
    vPane.setMaxWidth(Double.MAX_VALUE);
    vPane.setMaxHeight(Double.MAX_VALUE);
    setCenter(vPane);
    initRenjin(getStoredRemoteRepositories(), Thread.currentThread().getContextClassLoader());
  }

  private static Repo asRepo(RemoteRepository repo) {
    return new Repo(repo.getId(), repo.getContentType(), repo.getUrl());
  }

  private void initRenjin(List<Repo> repos, ClassLoader parentClassLoader) {
    String version = "unknown";

    try {
      RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();

      remoteRepositories = new ArrayList<>();
      remoteRepositories.addAll(asRemoteRepositories(repos));

      PackageLoader loader = getPackageLoader(parentClassLoader);

      ClassLoader cl = classloader(loader, parentClassLoader);

      SessionBuilder builder = new SessionBuilder();
      session = builder
          .withDefaultPackages()
          .setPackageLoader(loader) // allows library to work without having to include in the pom
          .setClassLoader(cl) //allows imports in r code to work
          .build();

      // TODO: after implementing a javafx grafics device do session.getOptions().set("device", theGraphicsDevice);

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

  private ClassLoader classloader(PackageLoader loader, ClassLoader parentClassLoader) {
    if (loader instanceof AetherPackageLoader) {
      return ((AetherPackageLoader) loader).getClassLoader();
    }
    return parentClassLoader;
  }

  private PackageLoader getPackageLoader(ClassLoader parentClassLoader) {
    if (packageLoader != null) {
      return packageLoader;
    }
    String pkgLoaderName;
    String overrridePackageLoader = System.getProperty(PACKAGE_LOADER_PREF);
    if (overrridePackageLoader != null) {
      pkgLoaderName = overrridePackageLoader;
    } else {
      pkgLoaderName = gui.getPrefs().get(PACKAGE_LOADER_PREF, AetherPackageLoader.class.getSimpleName());
    }
    PackageLoader loader = packageLoaderForName(parentClassLoader, pkgLoaderName);
    setPackageLoader(loader.getClass());
    return loader;
  }

  public PackageLoader getPackageLoader() {
    return packageLoader;
  }

  public void setPackageLoader(Class<?> loader) {
    packageLoader = packageLoaderForName(Thread.currentThread().getContextClassLoader(), loader.getSimpleName());
    gui.getPrefs().put(PACKAGE_LOADER_PREF, loader.getSimpleName());
  }

  private PackageLoader packageLoaderForName(ClassLoader parentClassLoader, String pkgLoaderName) {
    if (ClasspathPackageLoader.class.getSimpleName().equals(pkgLoaderName)) {
      return new ClasspathPackageLoader(parentClassLoader);
    }
    //log.info("parentClassLoader = {}, remoteRepositories = {}", parentClassLoader, remoteRepositories);
    AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, remoteRepositories);
    //loader.setRepositoryListener(new ConsoleRepositoryListener(System.out));
    //loader.setTransferListener(new ConsoleTransferListener(System.out));
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
    log.info("add renjin repo");
    list.add(RENJIN_REPO);
    log.info("add maven central repo");
    list.add(MVN_CENTRAL_REPO);
  }

  private String getStars(int length) {
    StringBuffer buf = new StringBuffer(length);
    for (int i = 0; i < length; i++) {
      buf.append("*");
    }
    return buf.toString();
  }

  public void restartR() {
    console.append("Restarting Renjin..\n");
    initRenjin(getStoredRemoteRepositories(), Thread.currentThread().getContextClassLoader());
    gui.getEnvironmentComponent().clearEnvironment();
  }

  /**
   * TODO: while we can stop the timeline with this we cannot interrupt the scriptengines eval.
   */
  public void interruptR() {
    log.info("Interrupting runnning script");
    if (scriptExecutionTimeline != null && Animation.Status.RUNNING.equals(scriptExecutionTimeline.getStatus())) {
      console.appendText("\nInterrupting Renjin...\n>");
      // Would be nice with something like engine.stop() here;
      scriptExecutionTimeline.stop();
    }
    // This is a nasty piece of code but a brutal stop() is the only thing that will break out of the script engine
    if (scriptThread != null && scriptThread.isAlive()) {
      console.append("\nInterrupting Renjin thread...");
      scriptThread.interrupt();
      scriptThread.stop();
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
      session.setStdOut(out);
      session.setStdErr(err);
      SEXP sexp = (SEXP) engine.eval(script);
      waiting();
      return sexp;
    } catch (Exception e) {
      waiting();
      throw e;
    }
  }

  public SEXP fetchVar(String varName) {
    Environment global = getSession().getGlobalEnvironment();
    Context topContext = getSession().getTopLevelContext();
    return global.getVariable(topContext, varName);
  }

  public void runScriptSync(String script, String title) {
    running();
    try (StringWriter outputWriter = new StringWriter()) {
      executeScriptAndReport(script, title, outputWriter);
    } catch (IOException e) {
      waiting();
      ExceptionAlert.showAlert("Failed to close writer capturing renjin results: ", e);
    }
  }

  public void runScriptAsync(String script, String title) {
    running();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
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
      waiting();
      updateEnvironment();
      promptAndScrollToEnd();
    });
    task.setOnSucceeded(e -> {
      waiting();
      updateEnvironment();
      promptAndScrollToEnd();
    });
    task.setOnFailed(e -> {
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
    scriptThread = new Thread(task);
    scriptThread.setDaemon(false);
    scriptThread.start();
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

  private void promptAndScrollToEnd() {
    console.appendText(">");
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

  // TODO run async
  public void runTests(String script, String title) {
    running();
    console.append("");
    console.append("Running hamcrest tests");
    console.append("----------------------");

    long start = System.currentTimeMillis();
    engine.put("inout", gui.getInoutComponent());
    List<TestResult> results = new ArrayList<>();
    try (StringWriter out = new StringWriter();
         StringWriter err = new StringWriter();
         PrintWriter outputWriter = new PrintWriter(out);
         PrintWriter errWriter = new PrintWriter(err)
    ) {
      session.setStdOut(outputWriter);
      session.setStdErr(errWriter);
      TestResult result =  runTest(script, title);
      results.add(result);
      printResult(title, out, err, result, DOUBLE_INDENT);

      //now run each testFunction in that file, in the same Session
      for (Symbol name : session.getGlobalEnvironment().getSymbolNames()) {
        String methodName = name.getPrintName().trim();
        if (methodName.startsWith("test.")) {
          SEXP value = session.getGlobalEnvironment().getVariable(session.getTopLevelContext(), name);
          if (isNoArgsFunction(value)) {
            Context context = session.getTopLevelContext();
            result = runTestFunction(context, title, name);
            results.add(result);
            printResult(methodName, out, err, result, DOUBLE_INDENT);
          }
        }
      }
      long end = System.currentTimeMillis();
      Map<TestResult.OutCome, List<TestResult>> resultMap = results.stream()
          .collect(Collectors.groupingBy(TestResult::getResult));

      List<TestResult> successResults = resultMap.get(TestResult.OutCome.SUCCESS);
      List<TestResult> failureResults = resultMap.get(TestResult.OutCome.FAILURE);
      List<TestResult> errorResults = resultMap.get(TestResult.OutCome.ERROR);
      long successCount = successResults == null ? 0 : successResults.size();
      long failCount = failureResults == null ? 0 : failureResults.size();
      long errorCount = errorResults == null ? 0 : errorResults.size();

      String duration = DurationFormatUtils.formatDuration(end-start, "mm 'minutes, 'ss' seconds, 'SSS' millis '");
      console.append("\nR tests summary:");
      console.append("----------------");
      console.append(format("Tests run: {}, Successes: {}, Failures: {}, Errors: {}",
          results.size(), successCount, failCount, errorCount));
      console.append("Time: " + duration + "\n");
    } catch (IOException e) {
      console.appendWarning("Failed to run test");
      ExceptionAlert.showAlert("Failed to run test", e);
    }
    updateEnvironment();
    promptAndScrollToEnd();
    waiting();
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
    StringBuffer buf = new StringBuffer();
    String lines = out.toString() == null ? "" : out.toString();
    for(String line : lines.trim().split("\n")) {
      buf.append(prefix).append(line).append("\n");
    }
    return prefix + buf.toString().trim();
  }


  private TestResult runTest(String script, String title) {
    TestResult result = new TestResult(title);
    String issue;
    Exception exception;
    String testName = title;
    console.append(INDENT + format("# Running test {}", title).trim());
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
    console.append(INDENT + format("# Running test function {} in {}", methodName, title).trim());
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

    try (OutputStream out = new AppenderOutputStream();
         WarningAppenderOutputStream err = new WarningAppenderOutputStream();
         PrintWriter outputWriter = new PrintWriter(out);
         PrintWriter errWriter = new PrintWriter(err)
    ) {

      engine.put("inout", gui.getInoutComponent());
      //engine.put("packageLoader", getPackageLoader());

      Platform.runLater(() -> console.append(title));
      session.setStdOut(outputWriter);
      session.setStdErr(errWriter);

      engine.eval(script);
      postEvalOutput();

    } catch (Exception e) {
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

  private void executeScriptAndReport(String script, String title, StringWriter outputWriter) {
    // A bit unorthodox use of timeline but this allows us to interrupt a running script
    // since the running script must be on the jfx thread to allow interaction with the gui
    // e.g. for plots, this is the best way to do that.
    scriptExecutionTimeline = new Timeline();
    KeyFrame scriptFrame = new KeyFrame(Duration.seconds(1), evt -> {
      try (StringWriter warnStrWriter = new StringWriter();
           PrintWriter warnWriter = new PrintWriter(warnStrWriter)) {
        engine.put("inout", gui.getInoutComponent());
        engine.getContext().setWriter(outputWriter);
        engine.getContext().setErrorWriter(outputWriter);
        outputWriter.write(title + "\n");
        engine.eval(script);
        console.append(outputWriter.toString(), true);

        session.setStdOut(warnWriter);
        session.printWarnings();
        console.appendWarning(warnStrWriter.toString());
        session.clearWarnings();
        //outputWriter.write(">");

      } catch (org.renjin.parser.ParseException e) {
        Platform.runLater(() ->
            ExceptionAlert.showAlert("Error parsing R script: " + e.getMessage(), e));
      } catch (ScriptException | EvalException e) {
        Platform.runLater(() ->
            ExceptionAlert.showAlert("Error running R script: " + e.getMessage(), e));
      } catch (RuntimeException e) {
        Platform.runLater(() ->
            ExceptionAlert.showAlert("A runtime error occurred running R script: " + e.getMessage(), e));
      } catch (Exception e) {
        Platform.runLater(() ->
            ExceptionAlert.showAlert("Exception thrown when running script: " + e.getMessage(), e));
      }
    });

    KeyFrame pkgFrame = new KeyFrame(Duration.seconds(1), evt -> {
      waiting();
      updateEnvironment();
      promptAndScrollToEnd();
    });

    scriptExecutionTimeline.getKeyFrames().addAll(scriptFrame, pkgFrame);
    scriptExecutionTimeline.setCycleCount(1);
    scriptExecutionTimeline.play();

  }

  public List<Repo> getRemoteRepositories() {
    return asRepos(remoteRepositories);
  }

  /**
   * this should be called last as the Session is reinitialized at the end
   */
  public void setRemoterepositories(List<Repo> repos, ClassLoader cl) {
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

  private void running() {
    Platform.runLater(() -> {
      runningView.setImage(IMG_RUNNING);
      statusButton.setTooltip(new Tooltip("Script is running, click to abort"));
      showTooltip(statusButton);
      gui.getMainMenu().enableInterruptMenuItem();
    });
    sleep(10);
  }

  private void waiting() {
    Platform.runLater(() -> {
      runningView.setImage(IMG_WAITING);
      statusButton.setTooltip(new Tooltip("Engine is idle"));
      gui.getMainMenu().disableInterruptMenuItem();
    });
  }

  public void showTooltip(Control control) {
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
        Platform.runLater(() -> customTooltip.hide());
      }
    };
    timer.schedule(task, 800);
  }

  public void setWorkingDir(File dir) {
    if (dir == null) {
      return;
    }
    try {
      session.setWorkingDirectory(dir);
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

  class AppenderOutputStream extends OutputStream {
    @Override
    public void write(int b) {
      console.appendChar((char) b);
    }
  }

  class WarningAppenderOutputStream extends OutputStream {
    @Override
    public void write(int b) {
      console.appendWarnChar((char) b);
    }
  }
}
