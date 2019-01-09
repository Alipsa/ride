package se.alipsa.ride;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.environment.EnvironmentComponent;
import se.alipsa.ride.inout.InoutComponent;
import se.alipsa.ride.menu.MainMenu;
import se.alipsa.ride.utils.FileUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class Ride extends Application {

  private ConsoleComponent consoleComponent;
  private CodeComponent codeComponent;
  private EnvironmentComponent environmentComponent;
  private InoutComponent inoutComponent;
  private Stage primaryStage;
  private Scene scene;
  private MainMenu mainMenu;

  Logger log = LoggerFactory.getLogger(Ride.class);

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    this.primaryStage = primaryStage;

    BorderPane root = new BorderPane();
    VBox main = new VBox();
    main.setAlignment(Pos.CENTER);
    main.setFillWidth(true);

    root.setCenter(main);

    mainMenu = new MainMenu(this);
    root.setTop(mainMenu);

    scene = new Scene(root, 1366, 768);
    addStyleSheet("R-keywords.css");
    addStyleSheet("XML-keywords.css");

    SplitPane leftSplitPane = new SplitPane();
    leftSplitPane.setOrientation(Orientation.VERTICAL);

    consoleComponent = new ConsoleComponent(this);
    stretch(consoleComponent, root);

    codeComponent = new CodeComponent(this);
    stretch(codeComponent, root);
    leftSplitPane.getItems().addAll(codeComponent, consoleComponent);


    SplitPane rightSplitPane = new SplitPane();
    rightSplitPane.setOrientation(Orientation.VERTICAL);

    environmentComponent = new EnvironmentComponent(this);
    stretch(environmentComponent, root);

    inoutComponent = new InoutComponent(this);
    stretch(inoutComponent, root);

    rightSplitPane.getItems().addAll(environmentComponent, inoutComponent);

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getItems().addAll(leftSplitPane, rightSplitPane);

    main.getChildren().add(splitPane);

    primaryStage.setOnCloseRequest(t -> {
      endProgram();
    });

    primaryStage.setTitle("Ride, a Renjin IDE");
    primaryStage.getIcons().add(new Image(FileUtils.getResourceUrl("image/logo.png").toExternalForm()));
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public void addStyleSheet(String styleSheetPath) {
    scene.getStylesheets().add(FileUtils.getResourceUrl(styleSheetPath).toExternalForm());
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
    timer.schedule(task, 100);
  }

  private void stretch(Pane component, Pane root) {
    component.prefHeightProperty().bind(root.heightProperty());
    component.prefWidthProperty().bind(root.widthProperty());
  }

  private void stretch(Control component, Pane root) {
    component.prefHeightProperty().bind(root.heightProperty());
    component.prefWidthProperty().bind(root.widthProperty());
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
    scene.setCursor(Cursor.WAIT);
    consoleComponent.setCursor(Cursor.WAIT);
  }

  public void setNormalCursor() {
    scene.setCursor(Cursor.DEFAULT);
    consoleComponent.setCursor(Cursor.DEFAULT);
  }

  public Preferences getPrefs() {
    return Preferences.userRoot().node(Ride.class.getName());
  }

}
