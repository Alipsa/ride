package se.alipsa.ride.code.codetab;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.TabTextArea;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;

import java.io.File;

public class CodeTab extends TextAreaTab {

    CodeTextArea codeTextArea;

    Button runButton;
    ConsoleComponent console;

    private Logger log = LoggerFactory.getLogger(CodeTab.class);

    public CodeTab(String title, ConsoleComponent console, CodeComponent codeComponent) {
        this.console = console;
        setTitle(title);

        BorderPane pane = new BorderPane();

        runButton = new Button("Run");
        FlowPane buttonPane = new FlowPane();
        pane.setTop(buttonPane);

        runButton.setOnAction(this::handleRunAction);
        buttonPane.getChildren().add(runButton);

        codeTextArea = new CodeTextArea();
        codeTextArea.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown() && KeyCode.ENTER.equals(e.getCode())) {
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
        console.runScript(rCode, getText());
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
    public void replaceText(int start, int end, String content) {
        codeTextArea.replaceText(start, end, content);
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
