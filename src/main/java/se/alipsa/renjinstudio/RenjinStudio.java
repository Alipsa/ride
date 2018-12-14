package se.alipsa.renjinstudio;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import se.alipsa.renjinstudio.code.CodeComponent;
import se.alipsa.renjinstudio.console.ConsoleComponent;
import se.alipsa.renjinstudio.menu.MainMenuBar;

public class RenjinStudio extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        HBox main = new HBox();
        main.setAlignment(Pos.CENTER);
        root.setCenter(main);

        root.setTop(new MainMenuBar());

        Scene scene = new Scene(root, 1024, 768);

        SplitPane leftSplitPane = new SplitPane();
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        ConsoleComponent console = new ConsoleComponent();
        leftSplitPane.getItems().addAll(new CodeComponent(console), console);


        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(Orientation.VERTICAL);

        TabPane northEasttp = new TabPane();
        Tab northEastTab = new Tab();
        TextArea northEast = new TextArea();
        northEast.setText("Environment");
        northEastTab.setContent(northEast);
        northEasttp.getTabs().add(northEastTab);

        TabPane southEasttp = new TabPane();
        Tab southEastTab = new Tab();
        TextArea southEast = new TextArea();
        southEast.setText("InOut");
        southEastTab.setContent(southEast);
        southEasttp.getTabs().add(southEastTab);

        rightSplitPane.getItems().addAll(northEasttp, southEasttp);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(leftSplitPane, rightSplitPane);

        main.getChildren().add(splitPane);


        primaryStage.setTitle("Renjin Studio");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
