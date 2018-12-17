package se.alipsa.renjinstudio.menu;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.renjinstudio.RenjinStudio;
import se.alipsa.renjinstudio.code.CodeTextArea;
import se.alipsa.renjinstudio.utils.Alerts;
import se.alipsa.renjinstudio.utils.ExceptionAlert;
import se.alipsa.renjinstudio.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainMenuBar extends MenuBar {

    RenjinStudio gui;

    Logger log = LoggerFactory.getLogger(MainMenuBar.class);

    public MainMenuBar(RenjinStudio gui) {
        this.gui = gui;
        Menu menuFile = createFileMenu();
        Menu menuEdit = new Menu("Edit");
        Menu menuCode = new Menu("Code");
        Menu menuView = new Menu("View");
        Menu menuPlots = new Menu("Plots");
        Menu menuSession = new Menu("Session");
        Menu menuBuild = new Menu("Build");
        Menu menuDebug = new Menu("Debug");
        Menu menuProfile = new Menu("Profile");
        Menu menuTools = new Menu("Tools");
        Menu menuHelp = new Menu("Help");
        getMenus().addAll(menuFile, menuEdit, menuCode, menuView, menuPlots, menuSession,
                menuBuild, menuDebug, menuProfile, menuTools, menuHelp);
    }

    private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");
        MenuItem save = new MenuItem("Save");
        save.setOnAction(this::saveContent);
        fileMenu.getItems().add(save);

        return fileMenu;
    }

    private void saveContent(ActionEvent event) {
        CodeTextArea codeArea = gui.getCodeComponent().getActiveCodeTextArea();
        File file = codeArea.getFile();
        if (file == null)  {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            file = fileChooser.showSaveDialog(gui.getStage());
            if (file == null) {
                return;
            }
        }

        try {
            FileUtils.writeToFile(file, codeArea.getText());
            log.info("File {} saved", file.getAbsolutePath());
            gui.getInoutComponent().fileAdded(file);
            gui.getCodeComponent().fileSaved(file);
        } catch (FileNotFoundException e) {
            ExceptionAlert.showAlert("Failed to save file " + file, e);
        }

    }
}
