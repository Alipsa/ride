package se.alipsa.ride.menu;

import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;

public class MainMenuBar extends MenuBar {

    Ride gui;

    Logger log = LoggerFactory.getLogger(MainMenuBar.class);

    public MainMenuBar(Ride gui) {
        this.gui = gui;
        Menu menuFile = createFileMenu();
        Menu menuEdit = new Menu("Edit");
        Menu menuCode = new Menu("Code");
        Menu menuView = new Menu("View");
        Menu menuPlots = new Menu("Plots");
        Menu menuSession = createSessionMenu();
        Menu menuBuild = new Menu("Build");
        Menu menuDebug = new Menu("Debug");
        Menu menuProfile = new Menu("Profile");
        Menu menuTools = createToolsMenu();
        Menu menuHelp = new Menu("Help");
        getMenus().addAll(menuFile, menuEdit, menuCode, menuView, menuPlots, menuSession,
                menuBuild, menuDebug, menuProfile, menuTools, menuHelp);
    }

    private Menu createToolsMenu() {

        Menu toolsMenu = new Menu("Tools");
        MenuItem globalOption = new MenuItem("Global Options");
        globalOption.setOnAction(this::handleGlobalOptions);
        toolsMenu.getItems().add(globalOption);
        return toolsMenu;
    }

    private void handleGlobalOptions(ActionEvent actionEvent) {
        GlobalOptionsDialog dialog = new GlobalOptionsDialog(gui);
        Optional<GlobalOptions> res = dialog.showAndWait();
        if (!res.isPresent()) {
            return;
        }
        GlobalOptions result = res.get();
        gui.setRenjinJar((File)result.get(GlobalOptions.RENJIN_JAR));

        gui.getConsoleComponent().setRemoterepositories(
            (List<Repo>) result.get(GlobalOptions.REMOTE_REPOSITORIES),
            Thread.currentThread().getContextClassLoader()
        );
    }



    private Menu createSessionMenu() {
        Menu sessionMenu = new Menu("Session");
        MenuItem restartMI = new MenuItem("Restart R");
        restartMI.setOnAction(this::restartR);
        MenuItem interruptMI = new MenuItem("Interrupt R");
        interruptMI.setOnAction(this::interruptR);

        sessionMenu.getItems().addAll(restartMI, interruptMI);
        return sessionMenu;
    }

    private void interruptR(ActionEvent actionEvent) {
        gui.getConsoleComponent().interruptR();
    }

    private void restartR(ActionEvent evt) {
        gui.getConsoleComponent().restartR();
        gui.getInoutComponent().setPackages(null);
    }

    private Menu createFileMenu() {
        Menu menu = new Menu("File");

        Menu fileMenu = new Menu("New File");

        MenuItem nRScript = new MenuItem("R Script");
        nRScript.setOnAction(this::nRScript);
        fileMenu.getItems().add(nRScript);

        MenuItem save = new MenuItem("Save");
        save.setOnAction(this::saveContent);
        menu.getItems().addAll(fileMenu, save);

        return menu;
    }

    private void nRScript(ActionEvent actionEvent) {
        gui.getCodeComponent().addTab("Unknown", "");
    }

    private void saveContent(ActionEvent event) {
        CodeTextArea codeArea = gui.getCodeComponent().getActiveCodeTextArea();
        File file = codeArea.getFile();
        if (file == null)  {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
            fileChooser.setTitle("Save File");
            file = fileChooser.showSaveDialog(gui.getStage());
            if (file == null) {
                return;
            }
        }

        try {
            boolean fileExisted = file.exists();
            FileUtils.writeToFile(file, codeArea.getText());
            log.info("File {} saved", file.getAbsolutePath());
            if (!fileExisted) {
                gui.getInoutComponent().fileAdded(file);
            }
            gui.getCodeComponent().fileSaved(file);
        } catch (FileNotFoundException e) {
            ExceptionAlert.showAlert("Failed to save file " + file, e);
        }

    }
}
