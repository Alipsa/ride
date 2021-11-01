package se.alipsa.ride.code.jstab;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.io.File;
import java.io.PrintWriter;

public class JsTab extends TextAreaTab {

  private final JsTextArea jsTextArea;
  private final Button executeButton;

  private static Logger log = LogManager.getLogger(JsTab.class);
  private ScriptEngine engine;

  public JsTab(String title, Ride gui) {
    super(gui, CodeType.JAVA_SCRIPT);
    setTitle(title);
    executeButton = new Button("Run");
    executeButton.setOnAction(a -> runJavascript());
    buttonPane.getChildren().add(executeButton);
    Button resetButton = new Button("Restart session");
    resetButton.setOnAction(a -> {
      initSession();
      gui.getConsoleComponent().getConsole().append("[Session restarted]", true);
      gui.getConsoleComponent().promptAndScrollToEnd();
    });
    buttonPane.getChildren().add(resetButton);
    jsTextArea = new JsTextArea(this);
    VirtualizedScrollPane<JsTextArea> scrollPane = new VirtualizedScrollPane<>(jsTextArea);
    pane.setCenter(scrollPane);
    initSession();
  }

  public void initSession() {
    NashornScriptEngineFactory nashornScriptEngineFactory = new NashornScriptEngineFactory();
    String[] options = new String[] {"--language=es6", "--no-deprecation-warning"};
    engine = nashornScriptEngineFactory.getScriptEngine(options);

    engine.put("inout", gui.getInoutComponent());
  }

  public void runJavascript() {
    runJavascript(getTextContent());
  }

  public void runJavascript(final String content) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();

    final ConsoleTextArea console = consoleComponent.getConsole();
    final String title = getTitle();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try (
                AppenderPrintWriter out = new AppenderPrintWriter(console);
                WarningAppenderWriter err = new WarningAppenderWriter(console);
                PrintWriter outputWriter = new PrintWriter(out);
                PrintWriter errWriter = new PrintWriter(err)
        ) {
          Platform.runLater(() -> console.append(title, true));
          ScriptContext context = engine.getContext();
          context.setWriter(outputWriter);
          context.setErrorWriter(errWriter);

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
    consoleComponent.startThreadWhenOthersAreFinished(thread, "javascript");
  }

  @Override
  public File getFile() {
    return jsTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    jsTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return jsTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return jsTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    jsTextArea.replaceContentText(start, end, content);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return jsTextArea;
  }
}