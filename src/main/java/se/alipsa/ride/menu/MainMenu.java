package se.alipsa.ride.menu;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;
import static se.alipsa.ride.menu.GlobalOptions.*;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.text.CaseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
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
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.GitUtils;
import se.alipsa.ride.utils.UniqueList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    Menu menuHelp = createHelpMenu();
    getMenus().addAll(menuFile, menuEdit, menuCode, /*menuView, menuPlots,*/ menuSession,
        /*menuBuild, menuDebug, menuProfile, */ menuTools, menuHelp);
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

    return menu;
  }

  private void createBasicPom(ActionEvent actionEvent) {
    CreateProjectWizardDialog dialog = new CreateProjectWizardDialog(gui, "Create basic pom", false);
    Optional<CreateProjectWizardResult> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    CreateProjectWizardResult res = result.get();
    try {
      String pomContent = createPom("templates/project-pom.xml", res.groupName, res.projectName);
      FileUtils.writeToFile(new File(res.dir, "pom.xml"), pomContent);
      gui.getInoutComponent().refreshFileTree();
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to create basic pom", e);
    }
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

      String camelCasedPackageName = CaseUtils.toCamelCase(res.projectName, true,
          ' ', '_', '-', ',', '.', '/', '\\');

      String pomContent = createPom("templates/project-pom.xml", res.groupName, res.projectName);
      FileUtils.writeToFile(new File(res.dir, "pom.xml"), pomContent);

      Path mainPath = new File(res.dir, "src/main/R").toPath();
      Files.createDirectories(mainPath);
      Path rFile = mainPath.resolve(camelCasedPackageName + ".R");
      Files.createFile(rFile);
      Path testPath = new File(res.dir, "src/test/R").toPath();
      Files.createDirectories(testPath);
      Path testFile = Files.createFile(testPath.resolve(camelCasedPackageName + "Test.R"));
      FileUtils.writeToFile(testFile.toFile(), "library('hamcrest')\n");

      String lowercaseProjectName = camelCasedPackageName.toLowerCase();

      String groupPath = res.groupName.replace('.', '/');
      Path loaderPath = new File(res.dir, "src/main/java/" + groupPath + "/" + lowercaseProjectName).toPath();
      Files.createDirectories(loaderPath);
      String loaderContent = FileUtils.readContent("templates/ScriptLoader.java")
          .replace("[groupId]", res.groupName)
          .replace("[lowercaseProjectName]", lowercaseProjectName)
          .replace("[fileName]", rFile.getFileName().toString());
      FileUtils.writeToFile(new File(loaderPath.toFile(), "ScriptLoader.java"), loaderContent);

      Path javaTestPath = new File(res.dir, "src/test/java/" + groupPath + "/" + lowercaseProjectName).toPath();
      Files.createDirectories(javaTestPath);
      String javaTestContent = FileUtils.readContent("templates/Test.java")
          .replace("[groupId]", res.groupName)
          .replace("[lowercaseProjectName]", lowercaseProjectName)
          .replace("[className]", camelCasedPackageName);
      FileUtils.writeToFile(new File(javaTestPath.toFile(), camelCasedPackageName + "Test.java"), javaTestContent);

      Path testResourcePath = new File(res.dir, "src/test/resources/").toPath();
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

  private String createPom(String s, String groupName, String projectName) throws IOException {
    return FileUtils.readContent(s)
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
      searchWindow.show();
      searchWindow.toFront();
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
      CodeTextArea codeArea = codeTab.getCodeArea();
      int caretPos = codeArea.getCaretPosition();
      String text = codeTab.getAllTextContent().substring(caretPos);
      String searchWord = searchInput.getValue();
      if (searchWord == null) {
        searchWord = searchInput.getEditor().getText();
        if (searchWord == null) {
          log.warn("searchWord is null and nothing entered in the combobox text field, nothing that can be searched");
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
    searchWindow = new Stage();
    searchWindow.setTitle("Find");
    searchWindow.setScene(scene);
    searchWindow.sizeToScene();
    searchWindow.show();
    searchWindow.toFront();
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
    log.debug("Adding style sheet {}", cssPath);
    webEngine.setUserStyleSheetLocation(cssPath);
    browser.getStylesheets().addAll(gui.getStyleSheets());

    FlowPane linkPane = new FlowPane();
    borderPane.setTop(linkPane);

    URL featuresUrl = FileUtils.getResourceUrl("manual/features.html");
    URL interactionUrl = FileUtils.getResourceUrl("manual/InteractingWithRide.html");
    URL shortcutsUrl = FileUtils.getResourceUrl("manual/KeyBoardShortcuts.html");
    URL examplesUrl = FileUtils.getResourceUrl("manual/examples.html");
    URL packagesUrl = FileUtils.getResourceUrl("manual/packages.html");

    Button featuresButton = new Button("Ride features");
    featuresButton.setOnAction(e -> webEngine.load(Objects.requireNonNull(featuresUrl).toExternalForm()));

    Button rideShortCuts = new Button("Ride keyboard shortcuts");
    rideShortCuts.setOnAction(e -> webEngine.load(Objects.requireNonNull(shortcutsUrl).toExternalForm()));

    Button interactingWithRideButton = new Button("Interacting with Ride");
    interactingWithRideButton.setOnAction(e -> webEngine.load(Objects.requireNonNull(interactionUrl).toExternalForm()));

    Button examplesButton = new Button("Tips and tricks");
    examplesButton.setOnAction(e -> webEngine.load(Objects.requireNonNull(examplesUrl).toExternalForm()));

    Button packagesButton = new Button("Packages");
    packagesButton.setOnAction(e -> webEngine.load(Objects.requireNonNull(packagesUrl).toExternalForm()));

    linkPane.getChildren().addAll(featuresButton, rideShortCuts, interactingWithRideButton, packagesButton, examplesButton);

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


    Scene dialog = new Scene(borderPane, 1024, 768);
    Stage stage = new Stage();
    stage.setWidth(1024);
    stage.setHeight(768);
    stage.initModality(Modality.NONE);
    stage.initOwner(gui.getStage());
    stage.setTitle("User Manual");
    stage.setScene(dialog);
    stage.show();
    webEngine.load(Objects.requireNonNull(featuresUrl).toExternalForm());
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);
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
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    UnStyledCodeArea ta = new UnStyledCodeArea();
    ta.getStyleClass().add("txtarea");
    ta.setWrapText(true);
    ta.replaceText(content.toString());
    alert.getDialogPane().setContent(ta);
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

    alert.showAndWait();
  }

  private Menu createToolsMenu() {

    Menu toolsMenu = new Menu("Tools");
    MenuItem globalOption = new MenuItem("Global Options");
    globalOption.setOnAction(this::handleGlobalOptions);
    toolsMenu.getItems().add(globalOption);

    MenuItem replMI = new MenuItem("Run REPL in console");
    replMI.setOnAction(this::runRepl);
    toolsMenu.getItems().add(replMI);

    return toolsMenu;
  }

  private void runRepl(ActionEvent actionEvent) {
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
      /* use gui classloader to be consistent
      gui.getConsoleComponent().setRemoteRepositories(selectedRepos,
         Thread.currentThread().getContextClassLoader()
      );
       */
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

    MenuItem nText = new MenuItem("Text file");
    nText.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.TXT));
    fileMenu.getItems().add(nText);

    MenuItem nSql = new MenuItem("SQL file");
    nSql.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.SQL));
    fileMenu.getItems().add(nSql);

    MenuItem nMarkdown = new MenuItem("Markdown file");
    nMarkdown.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.MD));
    fileMenu.getItems().add(nMarkdown);

    MenuItem nXml = new MenuItem("Xml file");
    nXml.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.XML));
    fileMenu.getItems().add(nXml);

    MenuItem nJava = new MenuItem("Java file");
    nJava.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.JAVA));
    fileMenu.getItems().add(nJava);

    MenuItem nGroovy = new MenuItem("Groovy file");
    nGroovy.setOnAction(a -> gui.getCodeComponent().addCodeTab(CodeType.GROOVY));
    fileMenu.getItems().add(nGroovy);

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
