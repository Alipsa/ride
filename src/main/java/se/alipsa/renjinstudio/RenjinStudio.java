package se.alipsa.renjinstudio;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RenjinStudio extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1024, 768);

        SplitPane spTop = new SplitPane();

        TabPane codetp = new TabPane();
        Tab codeTab = new Tab();
        TextArea code = new TextArea();
        code.setText("Code");
        codeTab.setContent(code);
        codetp.getTabs().add(codeTab);

        TabPane northEasttp = new TabPane();
        Tab northEastTab = new Tab();
        TextArea northEast = new TextArea();
        northEast.setText("North East");
        northEastTab.setContent(northEast);
        northEasttp.getTabs().add(northEastTab);
        root.add(codetp, 1,0);
        spTop.getItems().addAll(codetp, northEasttp);

        SplitPane spBottom = new SplitPane();
        TextArea console = new TextArea();
        console.setText("Console");

        TabPane southEasttp = new TabPane();
        Tab southEastTab = new Tab();
        TextArea southEast = new TextArea();
        southEast.setText("South east");
        southEastTab.setContent(southEast);
        southEasttp.getTabs().add(southEastTab);

        spBottom.getItems().addAll(console, southEasttp);

        root.add(spTop, 0,0);
        root.add(spBottom, 0,1);


        primaryStage.setTitle("Renjin Studio");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
