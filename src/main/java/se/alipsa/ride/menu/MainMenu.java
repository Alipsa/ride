package se.alipsa.ride.menu;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.menu.GlobalOptions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.RenjinVersion;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repl.JlineRepl;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.munin.MuninMdrTab;
import se.alipsa.ride.code.munin.MuninRTab;
import se.alipsa.ride.code.munin.ReportType;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.*;
import se.alipsa.ride.utils.git.GitUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.prefs.Preferences;

public class MainMenu extends MenuBar {

  private final Ride gui;
  private MenuItem interruptMI;
  private static final Logger log = LogManager.getLogger(MainMenu.class);
  private final List<String> searchStrings = new UniqueList<>();
  private Stage searchWindow;

  public MainMenu(Ride gui) {
    this.gui = gui;
    Menu menuFile = createFileMenu();
    Menu menuEdit = createEditMenu();
    Menu menuCode = createCodeMenu();
    //Menu menuView = new Menu("View");
    //Menu menuPlots = new Menu("Plots");
    Menu menuSession = createSessionMenu();
    //Menu menuBuild = new Menu("Build");
    //Menu menuDebug = new Menu("Debug");
    //Menu menuProfile = new Menu("Profile");
    Menu menuTools = createToolsMenu();
    Menu menuMunin = createMuninMenu();
    Menu menuHelp = createHelpMenu();
    getMenus().addAll(menuFile, menuEdit, menuCode, /*menuView, menuPlots,*/ menuSession,
        /*menuBuild, menuDebug, menuProfile, */ menuTools, menuMunin, menuHelp);
  }

  private Menu createMuninMenu() {
    Menu menu = new Menu("Munin");
    MenuItem configureMI = new MenuItem("Configure");
    configureMI.setOnAction(a -> configureMuninConnection());
    MenuItem loadReportMI = new MenuItem("Load report");
    loadReportMI.setOnAction(a -> loadMuninReport());
    MenuItem createUnmanagedReportMI = new MenuItem("Create R report");
    createUnmanagedReportMI.setOnAction(a -> createUnmanagedReport());
    MenuItem createMdrReportMI = new MenuItem("Create mdr report");
    createMdrReportMI.setOnAction(a -> createMdrReport());
    menu.getItems().addAll(configureMI, loadReportMI, createUnmanagedReportMI, createMdrReportMI);
    return menu;
  }

  private void createMdrReport() {
    MuninReport report = new MuninReport(DEFAULT_MDR_REPORT_NAME, ReportType.MDR);
    MuninMdrTab tab = new MuninMdrTab(gui, report);
    gui.getCodeComponent().addTabAndActivate(tab);
  }

  private void createUnmanagedReport() {
    MuninReport report = new MuninReport(DEFAULT_R_REPORT_NAME, ReportType.UNMANAGED);
    report.setDefinition("library('se.alipsa:htmlcreator')\n\n" +
        "html.clear()\n" +
        "html.add('<h1>add content like this here</h1>')");
    MuninRTab tab = new MuninRTab(gui, report);
    gui.getCodeComponent().addTabAndActivate(tab);
  }

  private void loadMuninReport() {
    MuninConnection con = (MuninConnection) gui.getSessionObject(SESSION_MUNIN_CONNECTION);
    if (con == null) {
      con = configureMuninConnection();
    }
    if (con == null) return;

    MuninReportDialog reportDialog = new MuninReportDialog(gui);
    Optional<MuninReport> result = reportDialog.showAndWait();
    if (!result.isPresent()) return;
    MuninReport report = result.get();
    TextAreaTab tab;
    tab = ReportType.MDR.equals(report.getReportType()) ? new MuninMdrTab(gui, report) : new MuninRTab(gui, report);
    //tab.replaceContentText(0, 0, report.getDefinition());
    gui.getCodeComponent().addTabAndActivate(tab);
  }


  public MuninConnection configureMuninConnection() {
    Dialog<MuninConnection> dialog = new Dialog<>();
    GridPane pane = new GridPane();
    dialog.getDialogPane().setContent(pane);

    Preferences prefs = gui.getPrefs();

    pane.add(new Label("Server name or IP: "), 0,0);
    TextField serverTf = new TextField();
    serverTf.setText(prefs.get(PREF_MUNIN_SERVER, ""));
    pane.add(serverTf, 1, 0);

    pane.add(new Label("Server port: "), 0, 1);
    IntField serverPortIf = new IntField(0, 65535, 8088);
    serverPortIf.setValue(prefs.getInt(PREF_MUNIN_PORT, 8088));
    pane.add(serverPortIf, 1, 1);

    pane.add(new Label("Username: "), 0, 2);
    TextField userNameTf = new TextField();
    userNameTf.setText(prefs.get(PREF_MUNIN_USERNAME, ""));
    pane.add(userNameTf,1, 2);

    pane.add(new Label("Password: "), 0, 3);
    PasswordField pwdTf = new PasswordField();
    pane.add(pwdTf, 1, 3);

    dialog.setResultConverter(callback -> {
      MuninConnection con = new MuninConnection();
      con.setServerName(serverTf.getText());
      con.setServerPort(serverPortIf.getValue());
      con.setUserName(userNameTf.getText());
      con.setPassword(pwdTf.getText());
      return con;
    });

    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    GuiUtils.addStyle(gui, dialog);

    if(serverTf.getText().length() > 0 && userNameTf.getText().length() > 0) {
      Platform.runLater(pwdTf::requestFocus);
    }

    Optional<MuninConnection> opt = dialog.showAndWait();
    MuninConnection con = opt.orElse(null);
    if (con == null) return null;
    gui.saveSessionObject(SESSION_MUNIN_CONNECTION, con);
    prefs.put(PREF_MUNIN_SERVER, con.getServerName());
    prefs.putInt(PREF_MUNIN_PORT, con.getServerPort());
    prefs.put(PREF_MUNIN_USERNAME, con.getUserName());
    return con;
  }

  private Menu createCodeMenu() {
    Menu menu = new Menu("Code");
    MenuItem commentItem = new MenuItem("Toggle line comments  ctrl+shift+C");
    commentItem.setOnAction(this::commentLines);
    menu.getItems().add(commentItem);
    SeparatorMenuItem separator = new SeparatorMenuItem();
    menu.getItems().add(separator);

    MenuItem projectWizard = new MenuItem("Create maven project");
    projectWizard.setOnAction(this::showProjectWizard);
    menu.getItems().add(projectWizard);

    MenuItem packageWizard = new MenuItem("Create package project");
    packageWizard.setOnAction(this::showPackageWizard);
    menu.getItems().add(packageWizard);

    MenuItem createBasicPomMI = new MenuItem("Create basic pom.xml");
    createBasicPomMI.setOnAction(this::createBasicPom);
    menu.getItems().add(createBasicPomMI);

    MenuItem cloneProjectMI = new MenuItem("Clone a git project");
    cloneProjectMI.setOnAction(this::cloneProject);
    menu.getItems().add(cloneProjectMI);

    return menu;
  }

  private void cloneProject(ActionEvent actionEvent) {
    CloneProjectDialog dialog = new CloneProjectDialog(gui);
    Optional<CloneProjectDialogResult> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    CloneProjectDialogResult res = result.get();
    try {
      gui.setWaitCursor();
      gui.getInoutComponent().cloneGitRepo(res.url, res.targetDir);
      gui.setNormalCursor();
    } catch (GitAPIException | RuntimeException e) {
      ExceptionAlert.showAlert("Failed to clone repository: " + e.getMessage(), e);
    }
  }

  private void createBasicPom(ActionEvent actionEvent) {
    CreateProjectWizardDialog dialog = new CreateProjectWizardDialog(gui, "Create basic pom", false);
    Optional<CreateProjectWizardResult> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    CreateProjectWizardResult res = result.get();
    try {
      String mainProjectScript = camelCasedPackageName(res) + ".R";
      String pomContent = createPom("templates/project-pom.xml", res.groupName, res.projectName, mainProjectScript);
      FileUtils.writeToFile(new File(res.dir, "pom.xml"), pomContent);
      gui.getInoutComponent().refreshFileTree();
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to create basic pom", e);
    }
  }


  private String camelCasedPackageName(CreateProjectWizardResult res) {
    return CaseUtils.toCamelCase(res.projectName, true,
        ' ', '_', '-', ',', '.', '/', '\\');
  }

  private void showProjectWizard(ActionEvent actionEvent) {
    CreateProjectWizardDialog dialog = new CreateProjectWizardDialog(gui);
    Optional<CreateProjectWizardResult> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    CreateProjectWizardResult res = result.get();
    try {
      Files.createDirectories(res.dir.toPath());

      String camelCasedPackageName = camelCasedPackageName(res);
      String mainProjectScript = camelCasedPackageName + ".R";
      String pomContent = createPom("templates/project-pom.xml", res.groupName, res.projectName, mainProjectScript);
      FileUtils.writeToFile(new File(res.dir, "pom.xml"), pomContent);

      Path mainPath = new File(res.dir, "R").toPath();
      Files.createDirectories(mainPath);
      Path rFile = mainPath.resolve(mainProjectScript);
      Files.createFile(rFile);
      Path testPath = new File(res.dir, "tests").toPath();
      Files.createDirectories(testPath);
      Path testFile = Files.createFile(testPath.resolve(camelCasedPackageName + "Test.R"));
      FileUtils.writeToFile(testFile.toFile(), "library('hamcrest')\n");

      /*
      String lowercaseProjectName = camelCasedPackageName.toLowerCase();

      String groupPath = res.groupName.replace('.', '/');

      Path loaderPath = new File(res.dir, "src/main/java/" + groupPath + "/" + lowercaseProjectName).toPath();
      Files.createDirectories(loaderPath);
      String loaderContent = FileUtils.readContent("templates/ScriptLoader.java")
          .replace("[groupId]", res.groupName)
          .replace("[lowercaseProjectName]", lowercaseProjectName)
          .replace("[fileName]", rFile.getFileName().toString());
      FileUtils.writeToFile(new File(loaderPath.toFile(), "ScriptLoader.java"), loaderContent);

       */
      /*
      Path javaTestPath = new File(res.dir, "src/test/java/" + groupPath + "/" + lowercaseProjectName).toPath();
      Files.createDirectories(javaTestPath);
      String javaTestContent = FileUtils.readContent("templates/Test.java")
          .replace("[groupId]", res.groupName)
          .replace("[lowercaseProjectName]", lowercaseProjectName)
          .replace("[className]", camelCasedPackageName);
      FileUtils.writeToFile(new File(javaTestPath.toFile(), camelCasedPackageName + "Test.java"), javaTestContent);
       */

      Path testResourcePath = new File(res.dir, "tests/resources/").toPath();
      Files.createDirectories(testResourcePath);
      FileUtils.copy("templates/log4j.properties", testResourcePath.toFile());

      if (res.changeToDir) {
        gui.getInoutComponent().changeRootDir(res.dir);
      } else {
        gui.getInoutComponent().refreshFileTree();
      }
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to create package project", e);
    }
  }

  private String createPom(String s, String groupName, String projectName, String... mainProjectScript) throws IOException {
    String content = FileUtils.readContent(s);
    if (mainProjectScript.length > 0) {
      content = content.replace("[mainScriptName]", mainProjectScript[0]);
    }
    return content
            .replace("[groupId]", groupName)
            .replace("[artifactId]", projectName)
            .replace("[name]", projectName)
            .replace("[renjinVersion]", RenjinVersion.getVersionName());
  }

  private void showPackageWizard(ActionEvent actionEvent) {
    CreatePackageWizardDialog dialog = new CreatePackageWizardDialog(gui);
    Optional<CreatePackageWizardResult> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    CreatePackageWizardResult res = result.get();
    try {
      Files.createDirectories(res.dir.toPath());

      String camelCasedPackageName = CaseUtils.toCamelCase(res.packageName, true,
         ' ', '_', '-', ',', '.', '/', '\\');

      String pomContent = createPom("templates/package-pom.xml", res.groupName, res.packageName);
      FileUtils.writeToFile(new File(res.dir, "pom.xml"), pomContent);

      FileUtils.copy("templates/NAMESPACE", res.dir);
      Path mainPath = new File(res.dir, "src/main/R").toPath();
      Files.createDirectories(mainPath);
      Path rFile = mainPath.resolve(camelCasedPackageName + ".R");
      Files.createFile(rFile);
      FileUtils.writeToFile(rFile.toFile(), "# remember to add export(function name) to NAMESPACE to make them available");
      Path testPath = new File(res.dir, "src/test/R").toPath();
      Files.createDirectories(testPath);
      Path testFile = Files.createFile(testPath.resolve(camelCasedPackageName + "Test.R"));
      FileUtils.writeToFile(testFile.toFile(), "library('hamcrest')\nlibrary('"
         + res.groupName + ":" + res.packageName + "')\n");
      if (res.changeToDir) {
        gui.getInoutComponent().changeRootDir(res.dir);
      } else {
        gui.getInoutComponent().refreshFileTree();
      }
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to create package project", e);
    }
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
    MenuItem undo = new MenuItem("Undo  ctrl+Z");
    undo.setOnAction(this::undo);
    MenuItem redo = new MenuItem("Redo ctrl+Y");
    redo.setOnAction(this::redo);
    MenuItem find = new MenuItem("Find ctrl+F");
    find.setOnAction(this::displayFind);
    menu.getItems().addAll(undo, redo, find);
    return menu;
  }

  private void redo(ActionEvent actionEvent) {
    TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
    CodeTextArea codeArea = codeTab.getCodeArea();
    codeArea.redo();
  }

  private void undo(ActionEvent actionEvent) {
    TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
    CodeTextArea codeArea = codeTab.getCodeArea();
    codeArea.undo();
  }

  private void displayFind(ActionEvent actionEvent) {
    displayFind();
  }

  public void displayFind() {
    if (searchWindow != null) {
      searchWindow.toFront();
      searchWindow.requestFocus();
      return;
    }

    VBox vBox = new VBox();
    vBox.setPadding(new Insets(3));
    FlowPane pane = new FlowPane();
    vBox.getChildren().add(pane);
    Label resultLabel = new Label();
    resultLabel.setPadding(new Insets(1));
    vBox.getChildren().add(resultLabel);
    pane.setPadding(Constants.FLOWPANE_INSETS);
    pane.setHgap(Constants.HGAP);
    pane.setVgap(Constants.VGAP);
    Button findButton = new Button("search");

    ComboBox<String> searchInput = new ComboBox<>();
    searchInput.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        findButton.fire();
      }
    });
    searchInput.setEditable(true);
    if (searchStrings.size() > 0) {
      searchStrings.forEach(s -> searchInput.getItems().add(s));
      searchInput.setValue(searchStrings.get(searchStrings.size()-1));
    }

    findButton.setOnAction(e -> {
      TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
      if (codeTab == null) {
        resultLabel.setText("No active code tab exists, nothing to search in");
        return;
      }
      CodeTextArea codeArea = codeTab.getCodeArea();
      int caretPos = codeArea.getCaretPosition();
      String text = codeTab.getAllTextContent().substring(caretPos);
      String searchWord = searchInput.getValue();
      if (searchWord == null) {
        searchWord = searchInput.getEditor().getText();
        if (searchWord == null) {
          log.warn("searchWord is null and nothing entered in the combobox text field, nothing that can be searched");
          resultLabel.setText("Nothing to search for");
          return;
        }
      }
      searchStrings.add(searchWord);
      if (!searchInput.getItems().contains(searchWord)) {
        searchInput.getItems().add(searchWord);
      }
      if (text.contains(searchWord)) {
        int place = text.indexOf(searchWord);
        codeArea.moveTo(place);
        codeArea.selectRange(caretPos + place, caretPos + place + searchWord.length());
        codeArea.requestFollowCaret();
        resultLabel.setText("found on line " + (codeArea.getCurrentParagraph() + 1));
      } else {
        resultLabel.setText(searchWord + " not found");
      }
    });

    Button toTopButton = new Button("To beginning");
    toTopButton.setOnAction(a -> {
      TextAreaTab codeTab = gui.getCodeComponent().getActiveTab();
      CodeTextArea codeArea = codeTab.getCodeArea();
      codeArea.moveTo(0);
      codeArea.requestFollowCaret();
    });
    pane.getChildren().addAll(searchInput, findButton, toTopButton);
    Scene scene = new Scene(vBox);
    scene.getStylesheets().addAll(Ride.instance().getStyleSheets());
    searchWindow = new Stage();
    searchWindow.setOnCloseRequest(event -> searchWindow = null);
    searchWindow.setTitle("Find");
    searchWindow.setScene(scene);
    searchWindow.sizeToScene();
    searchWindow.show();
    searchWindow.toFront();
    searchWindow.setAlwaysOnTop(true);

  }

  private Menu createHelpMenu() {
    Menu menu = new Menu("Help");
    
    MenuItem manual = new MenuItem("User Manual");
    manual.setOnAction(this::displayUserManual);
    
    MenuItem about = new MenuItem("About Ride");
    about.setOnAction(this::displayAbout);

    MenuItem checkVersion = new MenuItem("Check for updates");
    checkVersion.setOnAction(this::checkForUpdates);

    MenuItem viewLogFile = new MenuItem("View logfile");
    viewLogFile.setOnAction(this::viewLogFile);

    menu.getItems().addAll(manual, checkVersion, viewLogFile, about);
    return menu;
  }

  private void viewLogFile(ActionEvent actionEvent) {
    try {
      org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
      Map.Entry<String, Appender> appenderEntry = logger.get().getAppenders().entrySet().stream()
          .filter(e -> "RideLog".equals(e.getKey())).findAny().orElse(null);
      if (appenderEntry == null) {
        Alerts.warn("Failed to find log file", "Failed to find an appender called RideLog");
        return;
      }
      FileAppender appender = (FileAppender) appenderEntry.getValue();

      File logFile = new File(appender.getFileName());
      if (!logFile.exists()) {
        Alerts.warn("Failed to find log file", "Failed to find log file " + logFile.getAbsolutePath());
        return;
      }
      try {
        String content = FileUtils.readContent(logFile);
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        showInfoAlert(logFile.getAbsolutePath(), content,  Math.min(screenBounds.getWidth(), 1200.0), Math.min(screenBounds.getHeight(), 830.0));
      } catch (IOException e) {
        ExceptionAlert.showAlert("Failed to read log file content", e);
      }
    } catch (RuntimeException e) {
      ExceptionAlert.showAlert("Failed to show log file", e);
    }
  }

  private void checkForUpdates(ActionEvent actionEvent) {
      gui.setWaitCursor();
      Alert popup = new Alert(Alert.AlertType.INFORMATION);
      popup.setTitle("Check latest version");
      popup.getDialogPane().setHeaderText("Ride version info");
      TextArea textArea = new TextArea("Checking for the latest version....");
      textArea.setEditable(false);
      textArea.setWrapText(true);
      GridPane gridPane = new GridPane();
      gridPane.setMaxWidth(Double.MAX_VALUE);
      gridPane.add(textArea, 0, 0);
      popup.getDialogPane().setContent(gridPane);
      popup.setResizable(true);
      popup.initOwner(gui.getStage());
      popup.show();

      Platform.runLater(() -> {
        try {
          URL url = new URL("https://api.github.com/repos/perNyfelt/ride/releases/latest");
          ObjectMapper mapper = new ObjectMapper();
          JsonNode rootNode = mapper.readTree(url);
          JsonNode tagNode = rootNode.findValue("tag_name");
          String tag = tagNode.asText();
          String releaseTag = "unknown";
          String version = "unknown";
          Properties props = new Properties();
          try (InputStream is = Objects.requireNonNull(FileUtils.getResourceUrl("version.properties")).openStream()) {
            props.load(is);
            version = props.getProperty("version");
            releaseTag = props.getProperty("release.tag");
          } catch (IOException e) {
            ExceptionAlert.showAlert("Failed to load properties file", e);
          }
          StringBuilder sb = new StringBuilder("Your version: ")
              .append(version)
              .append("\nYour release tag:")
              .append(releaseTag)
              .append("\n\nLatest version on github: ").append(tag);

          int versionDiff = SemanticVersion.compare(releaseTag, tag);
          boolean identicalVersion = releaseTag.equalsIgnoreCase(tag);
          if (versionDiff < 0) {
            sb.append("\nA newer version is available.");
          } else if (versionDiff > 0) {
            sb.append("\nYou appear to be running a later version than what is released on github");
          } else {
            sb.append("\nThe semantic version number matches with the latest release.");
            if(!identicalVersion) {
              sb.append("\nHowever, versions are not identical");
            }
          }
          if (identicalVersion) {
            sb.append("\nYou are running the latest version");
          } else if (versionDiff < 1){
            sb.append("\nGet the latest release from https://github.com/perNyfelt/ride/releases/latest");
          }
          textArea.setText(sb.toString());
          gui.setNormalCursor();
        } catch (IOException e) {
          gui.setNormalCursor();
          ExceptionAlert.showAlert("Failed to get latest version", e);
        }
    });
  }

  private void displayUserManual(ActionEvent actionEvent) {
    new UserManual(gui).show();
  }

  private void displayAbout(ActionEvent actionEvent) {
    Properties props = new Properties();
    String version = "unknown";
    String releaseTag = "unknown";
    try (InputStream is = Objects.requireNonNull(FileUtils.getResourceUrl("version.properties")).openStream()) {
      props.load(is);
      version = props.getProperty("version");
      releaseTag = props.getProperty("release.tag");
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to load properties file", e);
    }
    StringBuilder content = new StringBuilder();
    content.append("Version: ");
    content.append(version);
    content.append("\nRelease tag: ");
    content.append(releaseTag);
    content.append("\n\n See https://github.com/perNyfelt/ride/ for more info or to report issues");
    showInfoAlert("About Ride", content,500, 200);

  }

  private void showInfoAlert(String title, StringBuilder content, double contentWidth, double contentHeight) {
    showInfoAlert(title, content.toString(), contentWidth, contentHeight);
  }

  private void showInfoAlert(String title, String content, double contentWidth, double contentHeight) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    UnStyledCodeArea ta = new UnStyledCodeArea();
    ta.getStyleClass().add("txtarea");
    ta.setWrapText(true);
    ta.replaceText(content);
    VirtualizedScrollPane<UnStyledCodeArea> scrollPane = new VirtualizedScrollPane<>(ta);
    alert.getDialogPane().setContent(scrollPane);
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
    alert.setResizable(true);

    alert.getDialogPane().setPrefHeight(contentHeight);
    alert.getDialogPane().setPrefWidth(contentWidth);

    String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);
    URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
    if (styleSheetUrl != null) {
      alert.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
    }

    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    stage.getIcons().addAll(gui.getStage().getIcons());

    alert.showAndWait();
  }

  private Menu createToolsMenu() {

    Menu toolsMenu = new Menu("Tools");
    MenuItem globalOption = new MenuItem("Global Options");
    globalOption.setOnAction(this::handleGlobalOptions);
    toolsMenu.getItems().add(globalOption);

    MenuItem replMI = new MenuItem("Run REPL in console");
    replMI.setOnAction(this::runRepl);
    if (System.console() == null) {
      replMI.setDisable(true);
    }
    toolsMenu.getItems().add(replMI);

    return toolsMenu;
  }

  private void runRepl(ActionEvent actionEvent) {
    if (System.console() == null) {
      Alerts.info("No console available", "No console available, change the launcher script to start with a console (e.g. java instead of javaw on windows)");
      return;
    }
    System.out.println();
    System.out.println("Console is now running the Renjin REPL, type quit() to exit");
    System.out.println();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          JlineRepl repl = new JlineRepl(SessionBuilder.buildDefault());
          repl.run();
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> System.out.println("Bye!"));
    task.setOnFailed(e -> ExceptionAlert.showAlert("Failure in REPL", task.getException()));
    Thread replThread = new Thread(task);
    replThread.setDaemon(false);
    replThread.start();
  }

  private void handleGlobalOptions(ActionEvent actionEvent) {
    GlobalOptionsDialog dialog = new GlobalOptionsDialog(gui);
    Optional<GlobalOptions> res = dialog.showAndWait();
    boolean shouldRestartR = false;

    if (!res.isPresent()) {
      return;
    }

    gui.setWaitCursor();
    GlobalOptions result = res.get();

    String mavenHome = (String) result.get(MAVEN_HOME);
    if (mavenHome != null && !mavenHome.trim().isEmpty()) {
      System.setProperty("MAVEN_HOME", mavenHome);
      gui.getPrefs().put(MAVEN_HOME, mavenHome);
    }

    @SuppressWarnings("rawtypes")
    Class<?> selectedPkgLoader = (Class) result.get(GlobalOptions.PKG_LOADER);
    if (!selectedPkgLoader.isInstance(gui.getConsoleComponent().getPackageLoader())) {
      PackageLoader loader = gui.getConsoleComponent().packageLoaderForName(this.getClass().getClassLoader(), selectedPkgLoader.getSimpleName());
      gui.getConsoleComponent().setPackageLoader(loader);
      log.info("Package loader changed, restarting R session");
      shouldRestartR = true;
    }

    List<Repo> selectedRepos = result.getRepoList(GlobalOptions.REMOTE_REPOSITORIES);
    Collections.sort(selectedRepos);
    List<Repo> currentRepos = gui.getConsoleComponent().getRemoteRepositories();
    Collections.sort(currentRepos);

    if (!currentRepos.equals(selectedRepos)) {
      log.info("Remote repositories changed, restarting R session");
      log.info("selectedRepos = {}\n currentRepos = {}", selectedRepos, currentRepos);
      gui.getConsoleComponent().setRemoteRepositories(
         selectedRepos,
         gui.getClass().getClassLoader()
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

    boolean useMavenClassLoader = result.getBoolean(USE_MAVEN_CLASSLOADER);
    if (useMavenClassLoader != gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, !useMavenClassLoader)) {
      log.info("useMavenClassLoader changed, restarting R session");
      gui.getPrefs().putBoolean(USE_MAVEN_CLASSLOADER, useMavenClassLoader);
      shouldRestartR = true;
    }

    boolean restartSessionAfterMaven = result.getBoolean(RESTART_SESSION_AFTER_MVN_RUN);
    gui.getPrefs().putBoolean(RESTART_SESSION_AFTER_MVN_RUN, restartSessionAfterMaven);

    boolean addBuildDirToClasspath = result.getBoolean(ADD_BUILDDIR_TO_CLASSPATH);
    if (addBuildDirToClasspath != gui.getPrefs().getBoolean(ADD_BUILDDIR_TO_CLASSPATH, !addBuildDirToClasspath)) {
      log.info("addBuildDirToClasspath changed, restarting R session");
      gui.getPrefs().putBoolean(ADD_BUILDDIR_TO_CLASSPATH, addBuildDirToClasspath);
      shouldRestartR = true;
    }

    boolean enableGit = result.getBoolean(ENABLE_GIT);
    gui.getInoutComponent().setEnableGit(enableGit);
    gui.getPrefs().putBoolean(ENABLE_GIT, enableGit);

    if (shouldRestartR) {
      restartR();
    }

    gui.setNormalCursor();
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
    showInfoAlert("Session info", content, 600, 300);
  }

  private void interruptR(ActionEvent actionEvent) {
    gui.getConsoleComponent().interruptProcess();
  }

  private void restartR(ActionEvent evt) {
    restartR();
  }

  private void restartR() {
    gui.getConsoleComponent().restartR();
    gui.getInoutComponent().setPackages(null);
    gui.getEnvironmentComponent().rRestarted();
  }

  private Menu createFileMenu() {
    Menu menu = new Menu("File");

    Menu fileMenu = new Menu("New File");

    MenuItem nRScript = new MenuItem("R Script");
    nRScript.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.R));
    fileMenu.getItems().add(nRScript);

    MenuItem nGroovy = new MenuItem("Groovy");
    nGroovy.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.GROOVY));
    fileMenu.getItems().add(nGroovy);

    MenuItem nJava = new MenuItem("Java");
    nJava.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.JAVA));
    fileMenu.getItems().add(nJava);

    MenuItem nJs = new MenuItem("Javascript");
    nJs.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.JAVA_SCRIPT));
    fileMenu.getItems().add(nJs);

    MenuItem nMarkdown = new MenuItem("Markdown");
    nMarkdown.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.MD));
    fileMenu.getItems().add(nMarkdown);

    MenuItem nMdr = new MenuItem("Mdr");
    nMdr.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.MDR));
    fileMenu.getItems().add(nMdr);

    MenuItem nSql = new MenuItem("SQL");
    nSql.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.SQL));
    fileMenu.getItems().add(nSql);

    MenuItem nText = new MenuItem("Text");
    nText.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.TXT));
    fileMenu.getItems().add(nText);

    MenuItem nXml = new MenuItem("Xml");
    nXml.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.XML));
    fileMenu.getItems().add(nXml);

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
      Git git = gui.getInoutComponent().getGit();
      if(codeArea.getTreeItem() != null && git != null) {
        String path = GitUtils.asRelativePath(codeArea.getFile(), gui.getInoutComponent().getRootDir());
        GitUtils.colorNode(git, path, codeArea.getTreeItem());
      }
    } catch (FileNotFoundException e) {
      ExceptionAlert.showAlert("Failed to save file " + file, e);
    }
  }

  private void saveContentAs(TextAreaTab codeArea) {
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
    log.debug("File {} saved", file.getAbsolutePath());
    codeArea.setTitle(file.getName());
    if (!fileExisted) {
      gui.getInoutComponent().fileAdded(file);
    }
    gui.getCodeComponent().fileSaved(file);
    codeArea.contentSaved();
  }

  public File promptForFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
    fileChooser.setTitle("Save File");
    return fileChooser.showSaveDialog(gui.getStage());
  }

  public File promptForFile(String fileTypeDescription, String extension, String suggestedName) {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter fileExtensions =
        new FileChooser.ExtensionFilter(
            fileTypeDescription, "*" + extension);
    fileChooser.getExtensionFilters().add(fileExtensions);
    fileChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
    fileChooser.setTitle("Save File");
    fileChooser.setInitialFileName(suggestedName);
    File file = fileChooser.showSaveDialog(gui.getStage());
    if (file != null && !file.getName().endsWith(extension)) {
      file = new File(file.getParentFile(), file.getName() + extension);
    }
    return file;
  }
}
