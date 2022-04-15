package se.alipsa.ride.code.groovytab;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.AppenderPrintWriter;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.console.ConsoleTextArea;
import se.alipsa.ride.console.WarningAppenderWriter;
import se.alipsa.ride.utils.ExceptionAlert;

import javax.script.ScriptEngine;
import java.io.File;
import java.io.PrintWriter;

public class GroovyTab extends TextAreaTab {

  private final GroovyTextArea groovyTextArea;

  private static final Logger log = LogManager.getLogger(GroovyTab.class);
  private final GroovyScriptEngineFactory factory = new GroovyScriptEngineFactory();
  private ScriptEngine engine;

  public GroovyTab(String title, Ride gui) {
    super(gui, CodeType.GROOVY);
    setTitle(title);
    Button executeButton = new Button("Run");
    executeButton.setOnAction(a -> runGroovy());
    buttonPane.getChildren().add(executeButton);

    Button resetButton = new Button("Restart session");
    resetButton.setOnAction(a -> {
      initSession();
      gui.getConsoleComponent().getConsole().append("[Session restarted]", true);
      gui.getConsoleComponent().promptAndScrollToEnd();
    });
    buttonPane.getChildren().add(resetButton);

    groovyTextArea = new GroovyTextArea(this);
    VirtualizedScrollPane<GroovyTextArea> javaPane = new VirtualizedScrollPane<>(groovyTextArea);
    pane.setCenter(javaPane);
    initSession();
  }

  public void initSession() {
    engine = factory.getScriptEngine();
    engine.put("inout", gui.getInoutComponent());
  }

  public void runGroovy() {
    runGroovy(getTextContent());
  }

  public void runGroovy(final String content) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();

    final ConsoleTextArea console = consoleComponent.getConsole();
    final String title = getTitle();

    Task<Void> task = new Task<>() {
      @Override
      public Void call() throws Exception {
        try (
            AppenderPrintWriter out = new AppenderPrintWriter(console);
            WarningAppenderWriter err = new WarningAppenderWriter(console);
            PrintWriter outputWriter = new PrintWriter(out);
            PrintWriter errWriter = new PrintWriter(err)
        ) {
          Platform.runLater(() -> console.append(title, true));
          engine.getContext().setWriter(outputWriter);
          engine.getContext().setErrorWriter(errWriter);
          Object result = engine.eval(content);
          if (result != null) {
            gui.getConsoleComponent().getConsole().appendFx("[result] " + result, true);
          }
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      console.appendNewlineIfNeeded();
      console.flush();
      gui.getConsoleComponent().promptAndScrollToEnd();
      consoleComponent.waiting();
    });

    task.setOnFailed(e -> {
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      consoleComponent.waiting();
      ExceptionAlert.showAlert(ex.getMessage(), ex);
      gui.getConsoleComponent().promptAndScrollToEnd();
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    consoleComponent.startThreadWhenOthersAreFinished(thread, "groovyScript");
  }

  @Override
  public File getFile() {
    return groovyTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    groovyTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return groovyTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return groovyTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    groovyTextArea.replaceContentText(start, end, content);
  }

  @Override
  public void replaceContentText(String content, boolean isReadFromFile) {
    groovyTextArea.replaceContentText(content, isReadFromFile);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return groovyTextArea;
  }
}
