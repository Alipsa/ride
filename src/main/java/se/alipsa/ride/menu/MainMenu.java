package se.alipsa.ride.menu;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.renjin.eval.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.TabTextArea;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class MainMenu extends MenuBar {

    Ride gui;
    MenuItem interruptMI;
    Logger log = LoggerFactory.getLogger(MainMenu.class);

    public MainMenu(Ride gui) {
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
        Menu menuHelp = createHelpMenu();
        getMenus().addAll(menuFile, menuEdit, menuCode, menuView, menuPlots, menuSession,
                menuBuild, menuDebug, menuProfile, menuTools, menuHelp);
    }

    private Menu createHelpMenu() {
        Menu menu = new Menu("Help");
        MenuItem about = new MenuItem("About Ride");
        about.setOnAction(this::displayAbout);

        menu.getItems().addAll(about);
        return menu;
    }

    private void displayAbout(ActionEvent actionEvent) {
        Properties props = new Properties();
        String version = "unknown";
        String releaseTag = "unknown";
        try (InputStream is = FileUtils.getResourceUrl("version.properties").openStream()) {
            props.load(is);
            version = props.getProperty("version");
            releaseTag = props.getProperty("release.tag");
        } catch (IOException e) {

        }
        StringBuilder content = new StringBuilder();
        content.append("Version: ");
        content.append(version);
        content.append("\nRelease tag: ");
        content.append(releaseTag);
        content.append("\n\n See https://github.com/perNyfelt/ride/ for more info or to report issues");
        showInfoAlert("About Ride", content);

    }

    private void showInfoAlert(String title, StringBuilder content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        TextArea ta = new TextArea();
        ta.setWrapText(true);
        ta.setText(content.toString());
        alert.getDialogPane().setContent(ta);
        alert.setResizable(true);
        alert.showAndWait();
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

        gui.getConsoleComponent().setPackageLoader((Class)result.get(GlobalOptions.PKG_LOADER));
        gui.getConsoleComponent().setRemoterepositories(
            (List<Repo>) result.get(GlobalOptions.REMOTE_REPOSITORIES),
            Thread.currentThread().getContextClassLoader()
        );
    }

    public void disableInterruptMenuItem() {
        interruptMI.setDisable(true);
    }

    public void enableInterruptMenuItem() {
        interruptMI.setDisable(false);
    }

    private Menu createSessionMenu() {
        Menu sessionMenu = new Menu("Session");
        MenuItem restartMI = new MenuItem("Restart R");
        restartMI.setOnAction(this::restartR);
        interruptMI = new MenuItem("Interrupt R");
        interruptMI.setOnAction(this::interruptR);
        disableInterruptMenuItem();

        MenuItem sessionInfo = new MenuItem("SessionInfo");
        sessionInfo.setOnAction(this::showSessionInfo);

        sessionMenu.getItems().addAll(restartMI, interruptMI, sessionInfo);
        return sessionMenu;
    }

    private void showSessionInfo(ActionEvent actionEvent) {
        ConsoleComponent cc = gui.getConsoleComponent();
        Session session = cc.getSession();
        StringBuilder content = new StringBuilder();
        content.append("Package loader: ");
        content.append(cc.getPackageLoader());
        content.append("\nClassloader: ");
        content.append(session.getClassLoader().getClass().getName());
        content.append("\nWorking dir: ");
        content.append(session.getWorkingDirectory());
        content.append("\n\n Please execute print(sessionInfo()) for other relevant session info");
        showInfoAlert("Session info", content);
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

        MenuItem quit = new MenuItem("Quit Session");
        quit.setOnAction(e -> gui.endProgram());

        menu.getItems().addAll(fileMenu, save, quit);
        return menu;
    }

    private void nRScript(ActionEvent actionEvent) {
        gui.getCodeComponent().addCodeTab("Unknown", "");
    }

    private void saveContent(ActionEvent event) {
        TabTextArea codeArea = gui.getCodeComponent().getActiveTabTextArea();
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
            FileUtils.writeToFile(file, codeArea.getAllTextContent());
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
