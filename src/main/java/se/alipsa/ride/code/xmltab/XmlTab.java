package se.alipsa.ride.code.xmltab;

import static se.alipsa.ride.menu.GlobalOptions.RESTART_SESSION_AFTER_MVN_RUN;
import static se.alipsa.ride.menu.GlobalOptions.USE_MAVEN_CLASSLOADER;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.console.ConsoleTextArea;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.mavenutils.MavenUtils;

import java.io.File;

public class XmlTab extends TextAreaTab {

  private final XmlTextArea xmlTextArea;

  private final Button executeButton;
  private final TextField targetsField;
  private final Label goalLabel;


  private final InvocationOutputHandler consoleOutputHandler;
  private final InvocationOutputHandler warningOutputHandler;
  private final MavenUtils mavenUtils;

  private static final Logger log = LogManager.getLogger(XmlTab.class);

  public XmlTab(String title, Ride gui) {
    super(gui, CodeType.XML);
    setTitle(title);

    mavenUtils = new MavenUtils();
    executeButton = new Button("Run");
    executeButton.setOnAction(this::runMaven);
    buttonPane.getChildren().add(executeButton);

    targetsField = new TextField();
    targetsField.setText("clean install");
    targetsField.setPrefColumnCount(30);
    goalLabel = new Label("Goals:");

    Button packageBrowserButton = new Button("Lookup");
    packageBrowserButton.setTooltip(new Tooltip("Search for package on Renjin CRAN"));
    packageBrowserButton.setOnAction(this::lockupPackage);
    buttonPane.getChildren().addAll(goalLabel, targetsField, packageBrowserButton);

    xmlTextArea = new XmlTextArea(this);
    VirtualizedScrollPane<CodeTextArea> xmlPane = new VirtualizedScrollPane<>(xmlTextArea);
    pane.setCenter(xmlPane);
    consoleOutputHandler = new ConsoleOutputHandler();
    warningOutputHandler = new ConsoleWarningOutputHandler();

    saveButton.setOnAction(a -> {
      gui.getMainMenu().saveContent(this);
      if (getFile() != null && getFile().getName().equals("pom.xml") && gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false)) {
        // TODO check if the dependecies have changed so we do not restart the session for nothing
        log.info("Maven build file saved, reloading classloader and restarting session");
        gui.getConsoleComponent().initRenjin(gui.getClass().getClassLoader());
      }
    });
  }

  private void lockupPackage(ActionEvent actionEvent) {
    PackageBrowserDialog browserDialog = new PackageBrowserDialog(gui);
    browserDialog.show();
  }

  private void runMaven(ActionEvent actionEvent) {
    getGui().getConsoleComponent().running();

    String args = targetsField.getText();
    if (args == null || StringUtils.isBlank(args)) {
      Alerts.warn("Maven arguments", "No goals (e.g. clean install) was supplied to maven");
      return;
    }
    final String[] mvnArgs = args.split(" ");
    getGui().getConsoleComponent().addOutput("Running 'mvn " + String.join(" ", mvnArgs) + "'", "", false, true);

    Task<Void> task = new Task<>() {
      @Override
      public Void call() throws Exception {
        try {
          InvocationResult result = MavenUtils.runMaven(getFile(), mvnArgs, consoleOutputHandler, warningOutputHandler);
          if (result.getExitCode() != 0) {
            if (result.getExecutionException() != null) {
              throw result.getExecutionException();
            } else {
              throw new MavenInvocationException("Maven build failed with exit code " + result.getExitCode());
            }
          }
        } catch (RuntimeException e) {
          // RuntimeExceptions (such as EvalExceptions is not caught so need to wrap all in an exception
          // this way we can get to the original one by extracting the cause from the thrown exception
          log.warn(e.getClass().getSimpleName() + " caught, rethrowing as wrapped Exception", e);
          throw new Exception(e);
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      boolean hasRenjinPlugin = false;
      try {
        Model model = mavenUtils.parsePom(getFile());
        hasRenjinPlugin = model.getBuild().getPlugins().stream().anyMatch(p -> "org.renjin".equals(p.getGroupId()) && "renjin-maven-plugin".equals(p.getArtifactId()));
      } catch (SettingsBuildingException | ModelBuildingException ex) {
        ExceptionAlert.showAlert("Failed to parse pom file", ex);
      }
      if (getGui().getPrefs().getBoolean(RESTART_SESSION_AFTER_MVN_RUN, false) && hasRenjinPlugin &&
          (args.contains("compile") || args.contains("package") || args.contains("install") || args.contains("site"))) {
        getGui().getConsoleComponent().restartR();
      }
      getGui().getConsoleComponent().promptAndScrollToEnd();
      getGui().getInoutComponent().refreshFileTree();
      getGui().getConsoleComponent().waiting();
    });

    task.setOnFailed(e -> {
      getGui().getConsoleComponent().waiting();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      getGui().getInoutComponent().refreshFileTree();
      getGui().getConsoleComponent().promptAndScrollToEnd();
      ExceptionAlert.showAlert("Maven Build Failed: " + ex.getMessage(), ex);
    });

    Thread mvnThread = new Thread(task);
    mvnThread.setDaemon(false);
    gui.getConsoleComponent().startThreadWhenOthersAreFinished(mvnThread, "runMaven");
  }

  private class ConsoleOutputHandler implements InvocationOutputHandler {
    private final ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    private final ConsoleTextArea console = consoleComponent.getConsole();
    @Override
    public void consumeLine(String line) {
      //System.out.println(line);
      Platform.runLater(() -> {
        if (line.startsWith("[ERROR]") || line.startsWith("[WARN")) {
          console.appendWarning(line, true);
        } else {
          console.append(line, true);
        }
        consoleComponent.scrollToEnd();
      });
    }
  }

  private class ConsoleWarningOutputHandler implements InvocationOutputHandler {
    private final ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    private final ConsoleTextArea console = consoleComponent.getConsole();
    @Override
    public void consumeLine(String line) {
      //System.err.println(line);
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
      goalLabel.setVisible(true);
      targetsField.setVisible(true);
    } else {
      executeButton.setVisible(false);
      goalLabel.setVisible(false);
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
