package se.alipsa.ride.code.xmltab;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.console.ConsoleTextArea;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.util.Arrays;

public class XmlTab extends TextAreaTab {

  private XmlTextArea xmlTextArea;

  private Button executeButton;
  private TextField targetsField;

  private static final Logger log = LoggerFactory.getLogger(XmlTab.class);

  public XmlTab(String title, Ride gui) {
    super(gui, CodeType.XML);
    setTitle(title);

    executeButton = new Button("Run");
    executeButton.setOnAction(this::runMaven);
    buttonPane.getChildren().add(executeButton);

    targetsField = new TextField();
    buttonPane.getChildren().add(targetsField);

    xmlTextArea = new XmlTextArea(this);
    VirtualizedScrollPane<CodeTextArea> xmlPane = new VirtualizedScrollPane<>(xmlTextArea);
    pane.setCenter(xmlPane);
  }

  private void runMaven(ActionEvent actionEvent) {
    getGui().getConsoleComponent().running();
    InvocationRequest request = new DefaultInvocationRequest();
    request.setBatchMode(true);
    final File pomFile = getFile();
    request.setPomFile( pomFile );
    String args = targetsField.getText();
    if (args == null || StringUtils.isBlank(args)) {
      Alerts.warn("Maven arguments", "No arguments (e.g. clean install) was supplied to maven");
      return;
    }
    final String[] mvnArgs = args.split(" ");
    getGui().getConsoleComponent().addOutput("Running 'mvn " + String.join(" ", mvnArgs) + "'", "", false, true);

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          request.setGoals(Arrays.asList(mvnArgs) );
          File dir = pomFile.getParentFile();
          request.setBaseDirectory(dir);
          log.info("Running maven from dir {} with args {}", dir, String.join(" ", mvnArgs));
          Invoker invoker = new DefaultInvoker();
          invoker.setOutputHandler(new ConsoleOutputHandler());
          invoker.setErrorHandler(new ConsoleWarningOutputHandler());
          InvocationResult result = invoker.execute( request );
          if ( result.getExitCode() != 0 ) {
            throw result.getExecutionException();
          }
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
      getGui().getConsoleComponent().waiting();
      getGui().getConsoleComponent().promptAndScrollToEnd();
    });

    task.setOnFailed(e -> {
      getGui().getConsoleComponent().waiting();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      ExceptionAlert.showAlert("Maven Build Failed: " + ex.getMessage(), ex);
      getGui().getConsoleComponent().promptAndScrollToEnd();
    });

    Thread mvnThread = new Thread(task);
    mvnThread.setDaemon(false);
    mvnThread.start();
  }

  private class ConsoleOutputHandler implements InvocationOutputHandler {
    private ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    private ConsoleTextArea console = consoleComponent.getConsole();
    @Override
    public void consumeLine(String line) {
      System.out.println(line);
      Platform.runLater(() -> {
        console.append(line);
        consoleComponent.scrollToEnd();
      });
    }
  }

  private class ConsoleWarningOutputHandler implements InvocationOutputHandler {
    private ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    private ConsoleTextArea console = consoleComponent.getConsole();
    @Override
    public void consumeLine(String line) {
      System.err.println(line);
      Platform.runLater(() -> {
        console.appendWarning(line);
        consoleComponent.scrollToEnd();
      });
    }
  }

  @Override
  public File getFile() {
    return xmlTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    xmlTextArea.setFile(file);
    if ("pom.xml".equalsIgnoreCase(file.getName())) {
      executeButton.setVisible(true);
      targetsField.setVisible(true);
    } else {
      executeButton.setVisible(false);
      targetsField.setVisible(false);
    }
  }

  @Override
  public String getTextContent() {
    return xmlTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return xmlTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    xmlTextArea.replaceContentText(start, end, content);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return xmlTextArea;
  }
}
