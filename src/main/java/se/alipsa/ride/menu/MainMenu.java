package se.alipsa.ride.menu;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.renjin.eval.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabType;
import se.alipsa.ride.code.TextAreaTab;
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

import static se.alipsa.ride.menu.GlobalOptions.CONSOLE_MAX_LENGTH_PREF;

public class MainMenu extends MenuBar {

  Ride gui;
  MenuItem interruptMI;
  Logger log = LoggerFactory.getLogger(MainMenu.class);

  public MainMenu(Ride gui) {
    this.gui = gui;
    Menu menuFile = createFileMenu();
    Menu menuEdit = createEditMenu();
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

  private Menu createEditMenu() {
    Menu menu = new Menu("Edit");
    MenuItem find = new MenuItem("Find");
    find.setOnAction(this::displayFind);
    menu.getItems().add(find);
    return menu;
  }

  private void displayFind(ActionEvent actionEvent) {

    FlowPane pane = new FlowPane();
    pane.setPadding(Constants.FLOWPANE_INSETS);
    pane.setHgap(Constants.HGAP);
    pane.setVgap(Constants.VGAP);

    TextField searchTF = new TextField();
    Button findButton = new Button("search");
    findButton.setOnAction(e -> {
      TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
      CodeArea codeArea = codeTab.getCodeArea();
      int caretPos = codeArea.getCaretPosition();
      String text = codeTab.getAllTextContent().substring(caretPos);
      String searchWord = searchTF.getText();
      if (text.contains(searchWord)) {
        int place = text.indexOf(searchWord);
        codeArea.moveTo(place);
        codeArea.selectRange(caretPos + place, caretPos + place + searchWord.length());
        codeArea.requestFollowCaret();
      }
    });
    pane.getChildren().addAll(searchTF, findButton);
    Scene scene = new Scene(pane);
    Stage stage = new Stage();
    stage.setTitle("Find");
    stage.setScene(scene);
    stage.show();
    stage.toFront();
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

    gui.getConsoleComponent().setPackageLoader((Class) result.get(GlobalOptions.PKG_LOADER));
    gui.getConsoleComponent().setRemoterepositories(
        (List<Repo>) result.get(GlobalOptions.REMOTE_REPOSITORIES),
        Thread.currentThread().getContextClassLoader()
    );
    int consoleMaxLength = result.getInt(CONSOLE_MAX_LENGTH_PREF);
    gui.getPrefs().putInt(CONSOLE_MAX_LENGTH_PREF, consoleMaxLength);
    gui.getConsoleComponent().setConsoleMaxSize(consoleMaxLength);
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
    nRScript.setOnAction(a ->  gui.getCodeComponent().addCodeTab(TabType.R));
    fileMenu.getItems().add(nRScript);

    MenuItem nText = new MenuItem("Text file");
    nText.setOnAction(a -> gui.getCodeComponent().addCodeTab(TabType.TXT));
    fileMenu.getItems().add(nText);

    MenuItem nSql = new MenuItem("SQL file");
    nSql.setOnAction(a -> gui.getCodeComponent().addCodeTab(TabType.SQL));
    fileMenu.getItems().add(nSql);

    MenuItem save = new MenuItem("Save");
    save.setOnAction(this::saveContent);

    MenuItem saveAs = new MenuItem("Save as");
    saveAs.setOnAction(this::saveContentAs);

    MenuItem quit = new MenuItem("Quit Session");
    quit.setOnAction(e -> gui.endProgram());

    menu.getItems().addAll(fileMenu, save, saveAs, quit);
    return menu;
  }



  private void saveContent(ActionEvent event) {
    TextAreaTab codeArea = gui.getCodeComponent().getActiveTab();
    saveContent(codeArea);
  }

  private void saveContentAs(ActionEvent event) {
    TextAreaTab codeArea = gui.getCodeComponent().getActiveTab();
    saveContentAs(codeArea);
  }

  public void saveContent(TextAreaTab codeArea) {
    File file = codeArea.getFile();
    if (file == null) {
      file = promptForFile();
      if (file == null) {
        return;
      }
    }
    try {
      saveFile(codeArea, file);
    } catch (FileNotFoundException e) {
      ExceptionAlert.showAlert("Failed to save file " + file, e);
    }
  }

  public void saveContentAs(TextAreaTab codeArea) {
    File file = promptForFile();
    if (file == null) {
      return;
    }
    try {
      saveFile(codeArea, file);
    } catch (FileNotFoundException e) {
      ExceptionAlert.showAlert("Failed to save file " + file, e);
    }
  }

  private void saveFile(TextAreaTab codeArea, File file) throws FileNotFoundException {
    boolean fileExisted = file.exists();
    FileUtils.writeToFile(file, codeArea.getAllTextContent());
    log.info("File {} saved", file.getAbsolutePath());
    if (!fileExisted) {
      gui.getInoutComponent().fileAdded(file);
    }
    gui.getCodeComponent().fileSaved(file);
    codeArea.contentSaved();
  }

  private File promptForFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
    fileChooser.setTitle("Save File");
    return fileChooser.showSaveDialog(gui.getStage());
  }
}
