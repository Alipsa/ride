package se.alipsa.ride.code.groovytab;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
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

import java.io.File;
import java.io.PrintWriter;

public class GroovyTab extends TextAreaTab {

  private final GroovyTextArea groovyTextArea;
  private final Button executeButton;

  private static Logger log = LogManager.getLogger(GroovyTab.class);
  private GroovyShell groovyShell;

  public GroovyTab(String title, Ride gui) {
    super(gui, CodeType.GROOVY);
    setTitle(title);
    executeButton = new Button("Run");
    executeButton.setOnAction(this::runGroovy);
    buttonPane.getChildren().add(executeButton);
    groovyTextArea = new GroovyTextArea(this);
    VirtualizedScrollPane<GroovyTextArea> javaPane = new VirtualizedScrollPane<>(groovyTextArea);
    pane.setCenter(javaPane);
    initSession();
  }

  public void initSession() {
    Binding sharedData = new Binding();
    sharedData.setProperty("inout", gui.getInoutComponent());
    groovyShell = new GroovyShell(sharedData);
  }

  private void runGroovy(ActionEvent actionEvent) {
    runGroovy();
  }

  public void runGroovy() {
    runGroovy(getTextContent());
  }

  public void runGroovy(final String content) {
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
          Platform.runLater(() -> console.append(title));
          groovyShell.setProperty("out", outputWriter);
          groovyShell.setProperty("err", errWriter);

          Object result = groovyShell.evaluate(content);
          if (result != null) {
            gui.getConsoleComponent().getConsole().appendFx("[result] " + result);
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
  public CodeTextArea getCodeArea() {
    return groovyTextArea;
  }
}
