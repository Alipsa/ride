package se.alipsa.ride.code;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.console.ConsoleComponent;

public class CodeTab extends TextAreaTab {

    CodeTextArea codeTextArea;

    public CodeTab(String title, ConsoleComponent console, CodeComponent codeComponent) {
        setText(title);

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
        setContent(vPane);
    }

    @Override
    TabTextArea getTabTextArea() {
        return codeTextArea;
    }
}
