package se.alipsa.ride.menu;

import static se.alipsa.ride.Constants.THEME;
import static se.alipsa.ride.menu.GlobalOptions.CONSOLE_MAX_LENGTH_PREF;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.renjin.eval.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class MainMenu extends MenuBar {

  Ride gui;
  MenuItem interruptMI;
  Logger log = LoggerFactory.getLogger(MainMenu.class);

  public MainMenu(Ride gui) {
    this.gui = gui;
    Menu menuFile = createFileMenu();
    Menu menuEdit = createEditMenu();
    Menu menuCode = createCodeMenu();
    //Menu menuView = new Menu("View");
    //Menu menuPlots = new Menu("Plots");
    Menu menuSession = createSessionMenu();
    //Menu menuBuild = new Menu("Build");
    Menu menuDebug = new Menu("Debug");
    //Menu menuProfile = new Menu("Profile");
    Menu menuTools = createToolsMenu();
    Menu menuHelp = createHelpMenu();
    getMenus().addAll(menuFile, menuEdit, menuCode, /*menuView, menuPlots,*/ menuSession,
        /*menuBuild, */ menuDebug, /*menuProfile, */ menuTools, menuHelp);
  }

  private Menu createCodeMenu() {
    Menu menu = new Menu("Code");
    MenuItem commentItem = new MenuItem("Toggle line comments  ctrl+shift+C");
    commentItem.setOnAction(this::commentLines);
    menu.getItems().addAll(commentItem);
    return menu;
  }

  private void commentLines(ActionEvent actionEvent) {
    commentLines();
  }

  public void commentLines() {
    CodeTextArea codeArea = gui.getCodeComponent().getActiveTab().getCodeArea();
    String lineComment;
    switch (gui.getCodeComponent().getActiveTab().getCodeType()) {
      case R:
        lineComment = "#";
        break;
      case SQL:
        lineComment = "--";
        break;
      case JAVA:
        lineComment = "//";
        break;
      default:
        return;
    }
    String selected = codeArea.selectedTextProperty().getValue();
    // if text is selected then go with that
    if (selected != null && !"".equals(selected)) {

      IndexRange range = codeArea.getSelection();
      String s = toggelComment(selected, lineComment);
      codeArea.replaceText(range, s);
    } else { // toggle current line
      String text = codeArea.getText(codeArea.getCurrentParagraph());
      String s = toggelComment(text, lineComment);
      int org = codeArea.getCaretPosition();
      codeArea.moveTo(codeArea.getCurrentParagraph(), 0);
      int start = codeArea.getCaretPosition();
      int end = start + text.length();
      codeArea.replaceText(start, end, s);
      codeArea.moveTo(org);
    }
  }

  private String toggelComment(String selected, String lineComment) {
    String[] lines = selected.split("\n");
    List<String> commented = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith(lineComment)) {
        commented.add(line.substring(lineComment.length()));
      } else {
        commented.add(lineComment + line);
      }
    }
    return String.join("\n", commented);
  }

  private Menu createEditMenu() {
    Menu menu = new Menu("Edit");
    MenuItem find = new MenuItem("Find  ctrl+F");
    find.setOnAction(this::displayFind);
    menu.getItems().add(find);
    return menu;
  }

  private void displayFind(ActionEvent actionEvent) {
    displayFind();
  }

  public void displayFind() {

    FlowPane pane = new FlowPane();
    pane.setPadding(Constants.FLOWPANE_INSETS);
    pane.setHgap(Constants.HGAP);
    pane.setVgap(Constants.VGAP);

    TextField searchTF = new TextField();
    Button findButton = new Button("search");
    findButton.setOnAction(e -> {
      TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
      CodeTextArea codeArea = codeTab.getCodeArea();
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
    
    MenuItem manual = new MenuItem("User Manual");
    manual.setOnAction(this::diplayUserManual);
    
    MenuItem about = new MenuItem("About Ride");
    about.setOnAction(this::displayAbout);

    menu.getItems().addAll(manual, about);
    return menu;
  }

  private void diplayUserManual(ActionEvent actionEvent) {
    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();
    BorderPane borderPane = new BorderPane();
    borderPane.setCenter(browser);
    String cssPath = gui.getStyleSheets().get(0);
    log.info("Adding style sheet {}", cssPath);
    webEngine.setUserStyleSheetLocation(cssPath);
    browser.getStylesheets().addAll(gui.getStyleSheets());

    FlowPane linkPane = new FlowPane();
    borderPane.setTop(linkPane);

    URL interactionUrl = FileUtils.getResourceUrl("manual/InteractingWithRide.html");
    URL shortcutsUrl = FileUtils.getResourceUrl("manual/KeyBoardShortcuts.html");
    URL examplesUrl = FileUtils.getResourceUrl("manual/examples.html");
    URL packagesUrl = FileUtils.getResourceUrl("manual/packages.html");

    Button rideShortCuts = new Button("Ride keyboard shortcuts");
    rideShortCuts.setOnAction(e -> webEngine.load(shortcutsUrl.toExternalForm()));

    Button interactingWithRideButton = new Button("Interacting with Ride");
    interactingWithRideButton.setOnAction(e -> webEngine.load(interactionUrl.toExternalForm()));

    Button examplesButton = new Button("Examples");
    examplesButton.setOnAction(e -> webEngine.load(examplesUrl.toExternalForm()));

    Button packagesButton = new Button("Packages");
    packagesButton.setOnAction(e -> webEngine.load(packagesUrl.toExternalForm()));

    linkPane.getChildren().addAll(rideShortCuts, interactingWithRideButton, examplesButton, packagesButton);

    webEngine.setCreatePopupHandler(
       (PopupFeatures config) -> {
         WebView nBrowser = new WebView();
         // Always use bright theme as external links will usually look funny when coming from dark mode
         nBrowser.getEngine().setUserStyleSheetLocation(FileUtils.getResourceUrl(Constants.BRIGHT_THEME).toExternalForm());
         nBrowser.getStylesheets().addAll(gui.getStyleSheets());
         Scene scene = new Scene(nBrowser);
         Stage stage = new Stage();
         stage.setScene(scene);
         stage.show();
         return nBrowser.getEngine();
       });


    Scene dialog = new Scene(borderPane);
    Stage stage = new Stage();
    stage.initModality(Modality.NONE);
    stage.initOwner(gui.getStage());
    stage.setTitle("User Manual");
    stage.setScene(dialog);
    stage.show();
    webEngine.load(shortcutsUrl.toExternalForm());
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);



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
    UnStyledCodeArea ta = new UnStyledCodeArea();
    ta.getStyleClass().add("txtarea");
    ta.setWrapText(true);
    ta.replaceText(content.toString());
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

    Class selectedPkgLoader = (Class) result.get(GlobalOptions.PKG_LOADER);
    if (!selectedPkgLoader.isInstance(gui.getConsoleComponent().getPackageLoader())) {
      gui.getConsoleComponent().setPackageLoader(selectedPkgLoader);
      log.info("Package loader changed, restarting R session");
      restartR();
    }

    List<Repo> selectedRepos = (List<Repo>)result.get(GlobalOptions.REMOTE_REPOSITORIES);
    Collections.sort(selectedRepos);
    List<Repo> currentRepos = gui.getConsoleComponent().getRemoteRepositories();
    Collections.sort(currentRepos);

    if (!currentRepos.equals(selectedRepos)) {
      log.info("Remote repositories changed, restarting R session");
      log.info("selectedRepos = {}\n currentRepos = {}", selectedRepos, currentRepos);
      gui.getConsoleComponent().setRemoterepositories(selectedRepos,
          Thread.currentThread().getContextClassLoader()
      );
    }

    int consoleMaxLength = result.getInt(CONSOLE_MAX_LENGTH_PREF);
    if (gui.getConsoleComponent().getConsoleMaxSize() != consoleMaxLength) {
      gui.getPrefs().putInt(CONSOLE_MAX_LENGTH_PREF, consoleMaxLength);
      gui.getConsoleComponent().setConsoleMaxSize(consoleMaxLength);
    }

    String theme = (String)result.get(THEME);
    if (!gui.getScene().getStylesheets().contains(theme)) {
      gui.getScene().getStylesheets().clear();
      gui.addStyleSheet(theme);
      gui.getPrefs().put(THEME, theme);
    }
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
    restartR();
  }

  private void restartR() {
    gui.getConsoleComponent().restartR();
    gui.getInoutComponent().setPackages(null);
  }

  private Menu createFileMenu() {
    Menu menu = new Menu("File");

    Menu fileMenu = new Menu("New File");

    MenuItem nRScript = new MenuItem("R Script");
    nRScript.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.R));
    fileMenu.getItems().add(nRScript);

    MenuItem nText = new MenuItem("Text file");
    nText.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.TXT));
    fileMenu.getItems().add(nText);

    MenuItem nSql = new MenuItem("SQL file");
    nSql.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.SQL));
    fileMenu.getItems().add(nSql);

    MenuItem save = new MenuItem("Save  ctrl+S");
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
