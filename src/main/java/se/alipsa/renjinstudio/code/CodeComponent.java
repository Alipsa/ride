package se.alipsa.renjinstudio.code;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.renjinstudio.RenjinStudio;
import se.alipsa.renjinstudio.console.ConsoleComponent;
import se.alipsa.renjinstudio.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class CodeComponent extends BorderPane {

    private ConsoleComponent console;
    private TabPane pane;

    private Logger log = LoggerFactory.getLogger(CodeComponent.class);

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
        VirtualizedScrollPane vPane = new VirtualizedScrollPane<>(code);
        codeTab.setContent(vPane);
        pane.getTabs().add(codeTab);
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        selectionModel.select(codeTab);
        return code;
    }

    private void handleRunAction (ActionEvent event) {
        CodeTextArea code = getActiveCodeTextArea();
        String rCode = code.getText();
        log.debug("Running r code {}", rCode);
        console.runScript(rCode);
    }

    public CodeTextArea getActiveCodeTextArea() {
        Tab selected = getActiveTab();
        return (CodeTextArea)selected.getContent();
    }

    private Tab getActiveTab() {
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        return selectionModel.getSelectedItem();
    }

    public CodeTextArea addTab(String title, String content) {
        CodeTextArea code = createAndAddTab(title);
        code.replaceText(0,0, content);
        return code;
    }

    public void addTab(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String content = String.join("\n", lines);
            CodeTextArea code = addTab(file.getName(), content);
            code.setFile(file);
        } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to read content of file " + file, e);
        }
    }

    public void fileSaved(File file) {
        CodeTextArea area = getActiveCodeTextArea();
        getActiveTab().setText(file.getName());
    }
}
