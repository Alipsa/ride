package se.alipsa.ride.code.rtab;

import javafx.scene.control.Button;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.TaskListener;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;

import java.io.File;

public class RTab extends TextAreaTab implements TaskListener {

  private RTextArea rTextArea;

  private final ConsoleComponent console;

  Button runButton;
  private final Button runTestsButton;
  private boolean isRunTestButtonDisabled = false;

  private static Logger log = LogManager.getLogger(RTab.class);

  public RTab(String title, Ride gui) {
    super(gui, CodeType.R);
    this.console = gui.getConsoleComponent();

    setTitle(title);

    runButton = new Button("Run"); // async
    runButton.setOnAction(event -> console.runScriptAsync(rTextArea.getTextContent(), getTitle(), this));
    buttonPane.getChildren().add(runButton);

    runTestsButton = new Button("Run tests");
    runTestsButton.setOnAction(evt -> console.runTests(this));
    buttonPane.getChildren().add(runTestsButton);
    disableRunTestsButton();

    rTextArea = new RTextArea(this);
    VirtualizedScrollPane<RTextArea> vPane = new VirtualizedScrollPane<>(rTextArea);
    pane.setCenter(vPane);
    gui.getEnvironmentComponent().addContextFunctionsUpdateListener(rTextArea);
    setOnClosed(e -> gui.getEnvironmentComponent().removeContextFunctionsUpdateListener(rTextArea));
  }

  @Override
  public File getFile() {
    return rTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    rTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return rTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return rTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    rTextArea.replaceContentText(start, end, content);
  }

  @Override
  public void replaceContentText(String content, boolean isReadFromFile) {
    rTextArea.replaceContentText(content, isReadFromFile);
  }

  public void enableRunTestsButton() {
    if (isRunTestButtonDisabled) {
      runTestsButton.setDisable(false);
      isRunTestButtonDisabled = false;
    }
  }

  public void disableRunTestsButton() {
    if (!isRunTestButtonDisabled) {
      runTestsButton.setDisable(true);
      isRunTestButtonDisabled = true;
    }
  }

  @Override
  public void taskStarted() {
    if(!isRunTestButtonDisabled) {
      runTestsButton.setDisable(true);
    }
    runButton.setDisable(true);
  }

  @Override
  public void taskEnded() {
    if (!isRunTestButtonDisabled) {
      runTestsButton.setDisable(false);
    }
    runButton.setDisable(false);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return rTextArea;
  }
}
