package se.alipsa.renjinstudio.code;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import se.alipsa.renjinstudio.RenjinStudio;
import se.alipsa.renjinstudio.console.ConsoleComponent;

import java.io.File;

public class CodeComponent extends BorderPane {

    private ConsoleComponent console;
    private TabPane pane;

    public CodeComponent(RenjinStudio gui) {
        this.console = gui.getConsoleComponent();
        FlowPane buttonPane = new FlowPane();
        setTop(buttonPane);

        Button runButton = new Button("Run");

        runButton.setOnAction(this::handleRunAction);
        buttonPane.getChildren().add(runButton);

        pane = new TabPane();
        setCenter(pane);
        createAndAddTab("Untitled");
    }

    private CodeTextArea createAndAddTab(String title) {
        Tab codeTab = new Tab();
        codeTab.setText(title);
        CodeTextArea code = new CodeTextArea();
        codeTab.setContent(code);
        pane.getTabs().add(codeTab);
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        selectionModel.select(codeTab);
        return code;
    }

    private void handleRunAction (ActionEvent event) {
        // TODO get code from active tab
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        Tab selected = selectionModel.getSelectedItem();
        CodeTextArea code = (CodeTextArea)selected.getContent();
        console.runScript(code.getText());
    }

    public void addTab(String title, String content) {
        CodeTextArea code = createAndAddTab(title);
        code.replaceText(0,0, content);
    }
}
