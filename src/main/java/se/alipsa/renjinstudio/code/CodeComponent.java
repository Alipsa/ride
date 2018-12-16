package se.alipsa.renjinstudio.code;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import se.alipsa.renjinstudio.console.ConsoleComponent;

public class CodeComponent extends BorderPane {

    private ConsoleComponent console;
    private CodeTextArea code;

    public CodeComponent(ConsoleComponent console) {
        this.console = console;
        FlowPane buttonPane = new FlowPane();
        setTop(buttonPane);

        Button runButton = new Button("Run");

        runButton.setOnAction(this::handleRunAction);
        buttonPane.getChildren().add(runButton);

        TabPane codetp = new TabPane();
        setCenter(codetp);
        Tab codeTab = new Tab();
        code = new CodeTextArea();
        codeTab.setContent(code);
        codetp.getTabs().add(codeTab);
    }

    private void handleRunAction (ActionEvent event) {
        console.runScript(code.getText());
    }
}
