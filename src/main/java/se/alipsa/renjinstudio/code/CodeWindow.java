package se.alipsa.renjinstudio.code;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

public class CodeWindow extends BorderPane {


    public CodeWindow() {
        FlowPane buttonPane = new FlowPane();
        setTop(buttonPane);

        Button runButton = new Button("Run");
        buttonPane.getChildren().add(runButton);

        TabPane codetp = new TabPane();
        setCenter(codetp);
        Tab codeTab = new Tab();
        TextArea code = new TextArea();
        code.setText("Code");
        codeTab.setContent(code);
        codetp.getTabs().add(codeTab);
    }
}
