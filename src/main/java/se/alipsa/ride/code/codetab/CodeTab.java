package se.alipsa.ride.code.codetab;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;

import java.io.File;

import static se.alipsa.ride.Constants.FLOWPANE_INSETS;

public class CodeTab extends TextAreaTab {

  private CodeTextArea codeTextArea;

  private ConsoleComponent console;

  private Button runTestsButton;
  private boolean isRunTestButtonDisabled = false;

  private Logger log = LoggerFactory.getLogger(CodeTab.class);

  public CodeTab(String title, Ride gui) {
    super(gui, TabType.R);
    this.console = gui.getConsoleComponent();

    setTitle(title);


    Button runButton = new Button("Run"); // async
    runButton.setOnAction(event -> console.runScriptAsync(codeTextArea.getTextContent(), getTitle()));
    buttonPane.getChildren().add(runButton);

    runTestsButton = new Button("Run tests");
    runTestsButton.setOnAction(evt -> console.runTests(codeTextArea.getTextContent(), getTitle()));
    buttonPane.getChildren().add(runTestsButton);
    disableRunTestsButton();

    codeTextArea = new CodeTextArea(this);
    VirtualizedScrollPane<CodeTextArea> vPane = new VirtualizedScrollPane<>(codeTextArea);
    pane.setCenter(vPane);
  }

  @Override
  public File getFile() {
    return codeTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    codeTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return codeTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return codeTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    codeTextArea.replaceContentText(start, end, content);
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
    return codeTextArea;
  }
}
