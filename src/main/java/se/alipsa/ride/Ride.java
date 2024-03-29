package se.alipsa.ride;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;
import static se.alipsa.ride.menu.GlobalOptions.MAVEN_HOME;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.maven.MavenUtils;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.environment.EnvironmentComponent;
import se.alipsa.ride.inout.FileOpener;
import se.alipsa.ride.inout.InoutComponent;
import se.alipsa.ride.menu.MainMenu;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.prefs.Preferences;

public class Ride extends Application {

  Logger log = LogManager.getLogger(Ride.class);
  private ConsoleComponent consoleComponent;
  private CodeComponent codeComponent;
  private EnvironmentComponent environmentComponent;
  private InoutComponent inoutComponent;
  private Stage primaryStage;
  private Scene scene;
  private MainMenu mainMenu;
  private Preferences preferences;
  private final Map<String, Object> sessionMap = new HashMap<>();
  private File rideBaseDir;

  private FileOpener fileOpener;

  private static Ride instance;

  public static void main(String[] args) {
    launch(args);
  }

  public static Ride instance() {
    return instance;
  }

  @Override
  public void start(Stage primaryStage) {
    log.info("Starting Ride...");
    instance = this;
    rideBaseDir = Path.of("").toAbsolutePath().toFile();
    /*
    System.out.println(
        log.getName()
        + "\ntraceEnabled = " + log.isTraceEnabled()
        + "\ndebugEnabled = " + log.isDebugEnabled()
        + "\ninfoEnabled  = " + log.isInfoEnabled()
        + "\nwarnEnabled  = " + log.isWarnEnabled()
        + "\nerrorEnabled = " + log.isErrorEnabled()
    );*/

    preferences = Preferences.userRoot().node(Ride.class.getName());
    this.primaryStage = primaryStage;

    // Allow global option for MAVEN_HOME to override system settings.
    String mavenHome = getPrefs().get(MAVEN_HOME, MavenUtils.locateMavenHome());
    if (mavenHome != null && !mavenHome.isBlank()) {
      System.setProperty("MAVEN_HOME", mavenHome);
    }
    BorderPane root = new BorderPane();
    VBox main = new VBox();
    main.setAlignment(Pos.CENTER);
    main.setFillWidth(true);

    root.setCenter(main);

    mainMenu = new MainMenu(this);
    root.setTop(mainMenu);

    scene = new Scene(root, 1366, 768);

    addStyleSheet(getPrefs().get(THEME, BRIGHT_THEME));

    SplitPane leftSplitPane = new SplitPane();
    leftSplitPane.setOrientation(Orientation.VERTICAL);

    consoleComponent = new ConsoleComponent(this);
    stretch(consoleComponent, root);

    environmentComponent = new EnvironmentComponent(this);
    stretch(environmentComponent, root);

    codeComponent = new CodeComponent(this);
    stretch(codeComponent, root);
    leftSplitPane.getItems().addAll(codeComponent, consoleComponent);

    fileOpener = new FileOpener(codeComponent);

    SplitPane rightSplitPane = new SplitPane();
    rightSplitPane.setOrientation(Orientation.VERTICAL);


    inoutComponent = new InoutComponent(this);
    stretch(inoutComponent, root);

    rightSplitPane.getItems().addAll(environmentComponent, inoutComponent);

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getItems().addAll(leftSplitPane, rightSplitPane);
    splitPane.setDividerPositions(0.6, 0.4);

    main.getChildren().add(splitPane);

    primaryStage.setOnCloseRequest(t ->{
      if (getCodeComponent().hasUnsavedFiles()) {
        boolean exitAnyway = Alerts.confirm(
            "Are you sure you want to exit?",
            "There are unsaved files",
            "Are you sure you want to exit \n -even though you have unsaved files?"
        );
        if (!exitAnyway) {
          t.consume();
          return;
        }
      }
      endProgram();
    });

    primaryStage.setTitle("Ride, a Renjin IDE");
    primaryStage.getIcons().add(
        new Image(Objects.requireNonNull(getClass().getResourceAsStream("/image/logo.png")))
    );
    primaryStage.setScene(scene);
    enableDragDrop(scene);
    consoleComponent.initRenjin(Ride.this.getClass().getClassLoader());
    primaryStage.show();
  }

  private void enableDragDrop(Scene scene) {

    scene.setOnDragOver(event -> {
      Dragboard db = event.getDragboard();
      if (db.hasFiles()) {
        // I wish there was a TransferMode.OPEN but there is not
        event.acceptTransferModes(TransferMode.LINK);
        db.setDragView(new Image("image/file.png"));
      } else {
        event.consume();
      }
    });
    // Dropping over surface
    scene.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        success = true;
        for (File file:db.getFiles()) {
          fileOpener.openFile(file, false);
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });
  }

  public void addStyleSheet(String styleSheetPath) {
    scene.getStylesheets().add(Objects.requireNonNull(FileUtils.getResourceUrl(styleSheetPath)).toExternalForm());
  }

  public ObservableList<String> getStyleSheets() {
    return scene.getStylesheets();
  }

  public void endProgram() {
    Platform.exit();
    // Allow some time before calling system exist so stop() can be used to do stuff if neeed
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      public void run() {
        System.exit(0);
      }
    };
    timer.schedule(task, 200);
  }

  private void stretch(Pane component, Pane root) {
    component.prefHeightProperty().bind(root.heightProperty());
    component.prefWidthProperty().bind(root.widthProperty());
  }

  private void stretch(Control component, Pane root) {
    component.prefHeightProperty().bind(root.heightProperty());
    component.prefWidthProperty().bind(root.widthProperty());
  }

  public void setTitle(String title) {
    primaryStage.setTitle("Ride, a Renjin IDE: " + title);
  }

  public ConsoleComponent getConsoleComponent() {
    return consoleComponent;
  }

  public CodeComponent getCodeComponent() {
    return codeComponent;
  }

  public EnvironmentComponent getEnvironmentComponent() {
    return environmentComponent;
  }

  public InoutComponent getInoutComponent() {
    return inoutComponent;
  }

  public Stage getStage() {
    return primaryStage;
  }

  public MainMenu getMainMenu() {
    return mainMenu;
  }

  public void setWaitCursor() {
    Platform.runLater(() -> {
      scene.setCursor(Cursor.WAIT);
      consoleComponent.busy();
    });
  }

  public boolean isWaitCursorSet() {
    return Cursor.WAIT.equals(scene.getCursor());
  }

  public void setNormalCursor() {
    Platform.runLater(() -> {
      scene.setCursor(Cursor.DEFAULT);
      consoleComponent.ready();
    });
  }

  public Preferences getPrefs() {
    return preferences;
  }

  public Scene getScene() {
    return scene;
  }

  public void saveSessionObject(String key, Object val) {
    sessionMap.put(key, val);
  }

  public Object getSessionObject(String key) {
    return sessionMap.get(key);
  }

  public File getRideBaseDir() {
    return rideBaseDir;
  }


}
