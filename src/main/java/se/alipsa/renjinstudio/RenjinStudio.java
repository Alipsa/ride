package se.alipsa.renjinstudio;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import se.alipsa.renjinstudio.code.CodeComponent;
import se.alipsa.renjinstudio.console.ConsoleComponent;
import se.alipsa.renjinstudio.environment.EnvironmentComponent;
import se.alipsa.renjinstudio.inout.InoutComponent;
import se.alipsa.renjinstudio.menu.MainMenuBar;
import se.alipsa.renjinstudio.utils.FileUtils;

public class RenjinStudio extends Application {

    private ConsoleComponent console;
    private CodeComponent codeComponent;
    private EnvironmentComponent environmentComponent;
    private InoutComponent inoutComponent;
    private Stage primaryStage;
    private Scene scene;

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

        root.setTop(new MainMenuBar(this));

        scene = new Scene(root, 1366, 768);
        scene.getStylesheets().add(FileUtils.getResourceUrl("R-keywords.css").toExternalForm());

        SplitPane leftSplitPane = new SplitPane();
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        console = new ConsoleComponent(this);
        stretch(console, root);

        codeComponent = new CodeComponent(this);
        stretch(codeComponent, root);
        leftSplitPane.getItems().addAll(codeComponent, console);


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


        primaryStage.setTitle("Renjin Studio");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        return console;
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

    public void setWaitCursor() {
        scene.setCursor(Cursor.WAIT);
    }

    public void setNormalCursor() {
        scene.setCursor(Cursor.DEFAULT);
    }
}
