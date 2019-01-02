package se.alipsa.ride.code;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.codetab.CodeTab;
import se.alipsa.ride.code.javatab.JavaTab;
import se.alipsa.ride.code.txttab.TxtTab;
import se.alipsa.ride.code.xmltab.XmlTab;
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

        pane = new TabPane();
        setCenter(pane);
        addCodeTab();
    }

    public void addCodeTab() {
        addTabAndActivate(createCodeTab("Untitled"));
    }

    private TextAreaTab createCodeTab(String title) {
        CodeTab codeTab = new CodeTab(title, console, this);
        return codeTab;
    }

    private TabTextArea addTabAndActivate(TextAreaTab codeTab) {
        pane.getTabs().add(codeTab);
        SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
        selectionModel.select(codeTab);
        return codeTab;
    }

    public String getActiveScriptName() {
        return getActiveTab().getTitle();

    }

    public String getTextFromActiveTab() {
        TabTextArea ta = getActiveTab();
        return ta.getTextContent();
    }


    public TextAreaTab getActiveTab() {
        SingleSelectionModel selectionModel = pane.getSelectionModel();
        return (TextAreaTab)selectionModel.getSelectedItem();
    }

    /*
    public TabTextArea addCodeTab(String title, String content) {

        TabTextArea tab = addTabAndActivate(createCodeTab(title));
        tab.replaceText(0, 0, content);
        return tab;
    } */

    /*
    public void addCodeTab(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String content = String.join("\n", lines);
            TabTextArea tab = addCodeTab(file.getName(), content);
            tab.setFile(file);
        } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to read content of file " + file, e);
        }
    } */

    /*
    public TabTextArea addTxtTab(String title, String content) {
        TxtTab txtTab = new TxtTab(title, content);
        addTabAndActivate(txtTab);
        return txtTab;
    } */

    /*
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
    } */

    public void addTab(File file, TabType type) {
        List<String> lines;
        TextAreaTab tab;
        String title = file.getName();
        switch (type) {
            case R: tab = new CodeTab(title, console, this);
            break;
            case TXT: tab = new TxtTab(title);
            break;
            case XML: tab = new XmlTab(title);
            break;
            case JAVA: tab = new JavaTab(title);
            break;
            default: tab = new TxtTab(title);
        }
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
            String content = String.join("\n", lines);
            tab.setFile(file);
            tab.replaceText(0,0,content);
        } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to read content of file " + file, e);
        }
        addTabAndActivate(tab);
    }

    public void fileSaved(File file) {
        getActiveTab().setTitle(file.getName());
        getActiveTab().setFile(file);
    }
}
