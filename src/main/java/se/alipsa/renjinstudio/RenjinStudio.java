package se.alipsa.renjinstudio;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class RenjinStudio extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        HBox root = new HBox();
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1024, 768);

        SplitPane leftSplitPane = new SplitPane();
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        TabPane codetp = new TabPane();
        Tab codeTab = new Tab();
        TextArea code = new TextArea();
        code.setText("Code");
        codeTab.setContent(code);
        codetp.getTabs().add(codeTab);

        TextArea console = new TextArea();
        console.setText("Console");

        leftSplitPane.getItems().addAll(codetp, console);


        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(Orientation.VERTICAL);

        TabPane northEasttp = new TabPane();
        Tab northEastTab = new Tab();
        TextArea northEast = new TextArea();
        northEast.setText("North East");
        northEastTab.setContent(northEast);
        northEasttp.getTabs().add(northEastTab);

        TabPane southEasttp = new TabPane();
        Tab southEastTab = new Tab();
        TextArea southEast = new TextArea();
        southEast.setText("South east");
        southEastTab.setContent(southEast);
        southEasttp.getTabs().add(southEastTab);

        rightSplitPane.getItems().addAll(northEasttp, southEasttp);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(leftSplitPane, rightSplitPane);

        root.getChildren().add(splitPane);


        primaryStage.setTitle("Renjin Studio");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
