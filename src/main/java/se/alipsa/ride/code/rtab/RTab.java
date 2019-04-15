package se.alipsa.ride.code.rtab;

import javafx.scene.control.Button;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;

import java.io.File;

public class RTab extends TextAreaTab {

  private RTextArea rTextArea;

  private ConsoleComponent console;

  private Button runTestsButton;
  private boolean isRunTestButtonDisabled = false;

  private Logger log = LoggerFactory.getLogger(RTab.class);

  public RTab(String title, Ride gui) {
    super(gui, TabType.R);
    this.console = gui.getConsoleComponent();

    setTitle(title);


    Button runButton = new Button("Run"); // async
    runButton.setOnAction(event -> console.runScriptAsync(rTextArea.getTextContent(), getTitle()));
    buttonPane.getChildren().add(runButton);

    runTestsButton = new Button("Run tests");
    runTestsButton.setOnAction(evt -> console.runTests(rTextArea.getTextContent(), getTitle()));
    buttonPane.getChildren().add(runTestsButton);
    disableRunTestsButton();

    rTextArea = new RTextArea(this);
    VirtualizedScrollPane<RTextArea> vPane = new VirtualizedScrollPane<>(rTextArea);
    pane.setCenter(vPane);
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
  public CodeArea getCodeArea() {
    return rTextArea;
  }
}
