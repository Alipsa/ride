package se.alipsa.ride.code;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class CodeComponent extends BorderPane {

    private ConsoleComponent console;
    private TabPane pane;

    private Logger log = LoggerFactory.getLogger(CodeComponent.class);

    public CodeComponent(Ride gui) {
        this.console = gui.getConsoleComponent();
        FlowPane buttonPane = new FlowPane();
        setTop(buttonPane);

        Button runButton = new Button("Run");

        runButton.setOnAction(this::handleRunAction);
        buttonPane.getChildren().add(runButton);

        pane = new TabPane();
        setCenter(pane);
        addTabAndActivate(createCodeTab("Untitled"));
    }

    private TextAreaTab createCodeTab(String title) {
        CodeTab codeTab = new CodeTab(title, console, this);
        return codeTab;
    }

    private TextAreaTab addTabAndActivate(TextAreaTab codeTab) {
        pane.getTabs().add(codeTab);
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        selectionModel.select(codeTab);
        return codeTab;
    }


    private void handleRunAction(ActionEvent event) {
        String rCode = getTextFromActiveTab();
        log.debug("Running r code {}", rCode);
        console.runScript(rCode, getActiveScriptName());
    }

    public String getActiveScriptName() {
        return getActiveTab().getText();

    }

    public String getTextFromActiveTab() {
        TabTextArea ta = getActiveTabTextArea();
        return ta.getTextContent();
    }

    public TabTextArea getActiveTabTextArea() {
        TextAreaTab selected = getActiveTab();
        return selected.getTabTextArea();
    }

    private TextAreaTab getActiveTab() {
        SingleSelectionModel selectionModel = pane.getSelectionModel();
        return (TextAreaTab)selectionModel.getSelectedItem();
    }

    public CodeTextArea addCodeTab(String title, String content) {

        TextAreaTab tab = addTabAndActivate(createCodeTab(title));
        //CodeTextArea code = (CodeTextArea)((VirtualizedScrollPane)tab.getContent()).getContent();
        CodeTextArea code = (CodeTextArea)tab.getTabTextArea();
        code.replaceText(0, 0, content);
        return code;
    }

    public void addCodeTab(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String content = String.join("\n", lines);
            CodeTextArea code = addCodeTab(file.getName(), content);
            code.setFile(file);
        } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to read content of file " + file, e);
        }
    }

    public TabTextArea addTxtTab(String title, String content) {
        TxtTab txtTab = new TxtTab(title, content);
        addTabAndActivate(txtTab);
        return txtTab.getTabTextArea();
    }

    public void addTxtTab(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String content = String.join("\n", lines);
            TxtTextArea txtTa = (TxtTextArea)addTxtTab(file.getName(), content);
            txtTa.setFile(file);
        } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to read content of file " + file, e);
        }
    }

    public void addTab(File file, TabType type) {
        //TODO implement me instead of separate methods for each type
    }

    public void fileSaved(File file) {
        getActiveTab().setText(file.getName());
        getActiveTabTextArea().setFile(file);
    }
}
