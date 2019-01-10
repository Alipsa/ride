package se.alipsa.ride.code.codetab;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;

import java.io.File;

public class CodeTab extends TextAreaTab {

  CodeTextArea codeTextArea;

  Button runButton;
  ConsoleComponent console;

  private Logger log = LoggerFactory.getLogger(CodeTab.class);

  public CodeTab(String title, Ride gui) {
    super(gui);
    this.console = gui.getConsoleComponent();

    setTitle(title);

    BorderPane pane = new BorderPane();

    FlowPane buttonPane = new FlowPane();
    buttonPane.setHgap(5);
    buttonPane.setPadding(new Insets(5, 10, 5, 5));
    pane.setTop(buttonPane);

    runButton = new Button("Run");
    runButton.setOnAction(this::handleRunAction);

    Button runInThreadButton = new Button("Run in Thread");
    runInThreadButton.setOnAction(event -> console.runScriptInThread(codeTextArea.getTextContent(), getTitle()));
    buttonPane.getChildren().addAll(runButton, runInThreadButton);

    codeTextArea = new CodeTextArea(this);
    codeTextArea.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown() && KeyCode.ENTER.equals(e.getCode())) {
        CodeComponent codeComponent = gui.getCodeComponent();
        String rCode = codeTextArea.getText(codeTextArea.getCurrentParagraph()); // current line

        String selected = codeTextArea.selectedTextProperty().getValue();
        // if text is selected then go with that instead
        if (selected != null && !"".equals(selected)) {
          rCode = codeComponent.getTextFromActiveTab();
        }
        console.runScript(rCode, codeComponent.getActiveScriptName());
        //code.displaceCaret(rCode.length() + 1);
        codeTextArea.moveTo(codeTextArea.getCurrentParagraph() + 1, 0);
      }
    });
    VirtualizedScrollPane<CodeTextArea> vPane = new VirtualizedScrollPane<>(codeTextArea);
    pane.setCenter(vPane);
    setContent(pane);
  }

  private void handleRunAction(ActionEvent event) {
    String rCode = codeTextArea.getTextContent();
    log.debug("Running r code {}", rCode);
    console.runScript(rCode, getTitle());
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

  @Override
  public String getTitle() {
    return getText();
  }

  @Override
  public void setTitle(String title) {
    setText(title);
  }
}
