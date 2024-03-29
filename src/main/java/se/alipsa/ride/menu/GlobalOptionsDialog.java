package se.alipsa.ride.menu;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.console.ConsoleComponent.PACKAGE_LOADER_PREF;
import static se.alipsa.ride.console.ConsoleTextArea.CONSOLE_MAX_LENGTH_DEFAULT;
import static se.alipsa.ride.menu.GlobalOptions.*;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.GuiUtils;
import se.alipsa.ride.utils.IntField;

import java.util.List;

class GlobalOptionsDialog extends Dialog<GlobalOptions> {

  private final TableView<Repo> reposTable;
  private final ComboBox<Class<?>> pkgLoaderCb;
  private final IntField intField;
  private final ComboBox<String> themes;
  private final CheckBox useMavenFileClasspath;
  private final TextField mavenHome;
  private final CheckBox restartSessionAfterMvnRun;
  private final CheckBox addBuildDirToClasspath;
  private final CheckBox enableGit;
  private final CheckBox autoRunGlobal;
  private final CheckBox autoRunProject;


  GlobalOptionsDialog(Ride gui) {
    setTitle("Global options");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    Label pkgLoaderLabel = new Label("Package Loader");
    grid.add(pkgLoaderLabel, 0, 0);
    pkgLoaderCb = new ComboBox<>();
    pkgLoaderCb.setConverter(new PackageLoaderClassConverter());
    pkgLoaderCb.getItems().addAll(ClasspathPackageLoader.class, AetherPackageLoader.class);
    String defaultPkgLoader = gui.getPrefs().get(PACKAGE_LOADER_PREF, AetherPackageLoader.class.getSimpleName());
    if (AetherPackageLoader.class.getSimpleName().equals(defaultPkgLoader)) {
      pkgLoaderCb.getSelectionModel().select(AetherPackageLoader.class);
    } else {
      pkgLoaderCb.getSelectionModel().select(ClasspathPackageLoader.class);
    }

    grid.add(pkgLoaderCb, 1, 0);

    Label reposLabel = new Label("Remote Repositories");
    grid.add(reposLabel, 0, 1);

    reposTable = new TableView<>();
    reposTable.setContextMenu(reposContextMenu());
    List<Repo> repos = gui.getConsoleComponent().getRemoteRepositories();

    TableColumn<Repo, String> idCol = new TableColumn<>("id");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
    idCol.setCellFactory(TextFieldTableCell.forTableColumn());
    idCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setId(t.getNewValue())
    );

    TableColumn<Repo, String> typeCol = new TableColumn<>("type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
    typeCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setType(t.getNewValue())
    );

    TableColumn<Repo, String> urlCol = new TableColumn<>("url");
    urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
    urlCol.setCellFactory(TextFieldTableCell.forTableColumn());
    urlCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setUrl(t.getNewValue())
    );

    reposTable.setRowFactory(tableView -> {
      final TableRow<Repo> row = new TableRow<>();
      final ContextMenu contextMenu = reposContextMenu();

      final MenuItem removeMenuItem = new MenuItem("delete row");
      removeMenuItem.setOnAction(event -> reposTable.getItems().remove(row.getItem()));
      contextMenu.getItems().add(removeMenuItem);

      // Set context menu on row, but use a binding to make it only show for non-empty rows:
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );
      return row;
    });


    urlCol.setMinWidth(450);
    reposTable.getColumns().add(idCol);
    reposTable.getColumns().add(typeCol);
    reposTable.getColumns().add(urlCol);


    reposTable.setItems(createObservable(repos));
    reposTable.setEditable(true);

    if (ClasspathPackageLoader.class.equals(pkgLoaderCb.getValue())) {
      reposTable.setDisable(true);
    }

    grid.add(reposTable, 1, 1);

    pkgLoaderCb.valueProperty().addListener(e -> {
      reposTable.setDisable(ClasspathPackageLoader.class.equals(pkgLoaderCb.getValue()));
    });

    Label consoleMaxSizeLabel = new Label("Console max size");
    grid.add(consoleMaxSizeLabel, 0, 2);
    intField = new IntField(1000, Integer.MAX_VALUE, gui.getPrefs().getInt(CONSOLE_MAX_LENGTH_PREF, CONSOLE_MAX_LENGTH_DEFAULT));
    grid.add(intField, 1, 2);

    Label styleTheme = new Label("Style theme");
    grid.add(styleTheme, 0, 3);
    themes = new ComboBox<>();
    themes.getItems().addAll(DARK_THEME, BRIGHT_THEME, BLUE_THEME);
    themes.getSelectionModel().select(gui.getPrefs().get(THEME, BRIGHT_THEME));
    grid.add(themes, 1, 3);

    FlowPane cpPane = new FlowPane();
    grid.add(cpPane, 0,4, 2, 1);

    Label useMavenFileClasspathLabel = new Label("Use pom classpath");
    useMavenFileClasspathLabel.setTooltip(new Tooltip("Use classpath from pom.xml (if available) when running R code"));
    useMavenFileClasspathLabel.setPadding(new Insets(0, 37, 0, 0));
    cpPane.getChildren().add(useMavenFileClasspathLabel);
    useMavenFileClasspath = new CheckBox();
    useMavenFileClasspath.setSelected(gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false));
    cpPane.getChildren().add(useMavenFileClasspath);

    Label addBuildDirToClasspathLabel = new Label("Add build dir to classpath");
    addBuildDirToClasspathLabel.setPadding(new Insets(0, 27, 0, 70));
    addBuildDirToClasspathLabel.setTooltip(new Tooltip("Add target/classes and target/test-classes to classpath"));
    cpPane.getChildren().add(addBuildDirToClasspathLabel);
    addBuildDirToClasspath = new CheckBox();
    addBuildDirToClasspath.setSelected(gui.getPrefs().getBoolean(ADD_BUILDDIR_TO_CLASSPATH, true));
    cpPane.getChildren().add(addBuildDirToClasspath);

    // When developing packages we need to reload the session after mvn has been run
    // so that new definitions can be picked up from target/classes.
    Label restartSessionAfterMvnRunLabel = new Label("Restart session after mvn build");
    restartSessionAfterMvnRunLabel.setPadding(new Insets(0, 27, 0, 27));
    restartSessionAfterMvnRunLabel.setTooltip(new Tooltip("When developing packages we need to reload the session after mvn has been run\nso that new definitions can be picked up from target/classes"));
    cpPane.getChildren().add(restartSessionAfterMvnRunLabel);
    restartSessionAfterMvnRun = new CheckBox();
    restartSessionAfterMvnRun.setSelected(gui.getPrefs().getBoolean(RESTART_SESSION_AFTER_MVN_RUN, true));
    cpPane.getChildren().add(restartSessionAfterMvnRun);

    Label mavenHomeLabel = new Label("MAVEN_HOME");
    mavenHomeLabel.setTooltip(new Tooltip("The location of your maven installation directory"));
    //mavenHomeLabel.setPadding(new Insets(0, 27, 0, 0));
    grid.add(mavenHomeLabel, 0,5);

    HBox mavenHomePane = new HBox();
    mavenHomePane.setAlignment(Pos.CENTER_LEFT);
    mavenHome = new TextField();
    HBox.setHgrow(mavenHome, Priority.ALWAYS);
    mavenHome.setText(gui.getPrefs().get(MAVEN_HOME, System.getProperty("MAVEN_HOME", System.getenv("MAVEN_HOME"))));
    mavenHomePane.getChildren().add(mavenHome);
    grid.add(mavenHomePane, 1,5,2, 1);

    FlowPane gitOptionPane = new FlowPane();
    Label enableGitLabel = new Label("Enable git integration");
    enableGitLabel.setPadding(new Insets(0, 20, 0, 0));
    enableGitLabel.setTooltip(new Tooltip("note: git must be initialized in the project dir for integration to work"));
    gitOptionPane.getChildren().add(enableGitLabel);
    enableGit = new CheckBox();
    enableGit.setSelected(gui.getPrefs().getBoolean(ENABLE_GIT, true));
    gitOptionPane.getChildren().add(enableGit);
    grid.add(gitOptionPane, 0, 6, 2, 1);

    FlowPane autoRunPane = new FlowPane();
    Label autoRunGlobalLabel = new Label("Run global autorun.R on session init");
    autoRunGlobalLabel.setTooltip(new Tooltip("Run autorun.R from Ride install dir each time a session (re)starts."));
    autoRunGlobalLabel.setPadding(new Insets(0, 20, 0, 0));
    autoRunGlobal = new CheckBox();
    autoRunGlobal.setSelected(gui.getPrefs().getBoolean(AUTORUN_GLOBAL, false));
    autoRunPane.getChildren().addAll(autoRunGlobalLabel, autoRunGlobal);

    Label autoRunProjectLabel = new Label("Run project autorun.R on session init");
    autoRunProjectLabel.setTooltip(new Tooltip("Run autorun.R from the project dir (working dir) each time a session (re)starts"));
    autoRunProjectLabel.setPadding(new Insets(0, 20, 0, 20));
    autoRunProject = new CheckBox();
    autoRunProject.setSelected(gui.getPrefs().getBoolean(AUTORUN_PROJECT, false));
    autoRunPane.getChildren().addAll(autoRunProjectLabel, autoRunProject);

    grid.add(autoRunPane, 0,7, 2, 1);

    getDialogPane().setPrefSize(800, 420);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    setResizable(true);

    GuiUtils.addStyle(gui, this);

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
  }

  private ContextMenu reposContextMenu() {
    final ContextMenu contextMenu = new ContextMenu();
    final MenuItem addMenuItem = new MenuItem("add row");
    addMenuItem.setOnAction(event -> addRow(new Repo()));

    final Menu addDefault = new Menu("add default");
    final MenuItem addBedataDriven = new MenuItem("Renjin repo");
    addBedataDriven.setOnAction(this::addRenjinRepo);
    final MenuItem addMavenCentral = new MenuItem("Maven Central");
    addMavenCentral.setOnAction(this::addMvnCentralRepo);
    addDefault.getItems().addAll(addBedataDriven, addMavenCentral);

    contextMenu.getItems().addAll(addMenuItem, addDefault);
    return contextMenu;
  }

  private void addRow(Repo repo) {
    reposTable.getItems().add(repo);
  }

  private void addRenjinRepo(ActionEvent actionEvent) {
    addRow(ConsoleComponent.RENJIN_REPO);
  }

  private void addMvnCentralRepo(ActionEvent actionEvent) {
    addRow(ConsoleComponent.MVN_CENTRAL_REPO);
  }

  private ObservableList<Repo> createObservable(List<Repo> repos) {
    if (repos == null) {
      return FXCollections.emptyObservableList();
    }
    return FXCollections.observableArrayList(repos);
  }

  private GlobalOptions createResult() {
    GlobalOptions result = new GlobalOptions();
    result.put(REMOTE_REPOSITORIES, reposTable.getItems());
    result.put(PKG_LOADER, pkgLoaderCb.getSelectionModel().getSelectedItem());
    result.put(CONSOLE_MAX_LENGTH_PREF, intField.getValue());
    result.put(THEME, themes.getValue());
    result.put(USE_MAVEN_CLASSLOADER, useMavenFileClasspath.isSelected());
    result.put(ADD_BUILDDIR_TO_CLASSPATH, addBuildDirToClasspath.isSelected());
    result.put(RESTART_SESSION_AFTER_MVN_RUN, restartSessionAfterMvnRun.isSelected());
    result.put(ENABLE_GIT, enableGit.isSelected());
    result.put(AUTORUN_GLOBAL, autoRunGlobal.isSelected());
    result.put(AUTORUN_PROJECT, autoRunProject.isSelected());
    return result;
  }


}
