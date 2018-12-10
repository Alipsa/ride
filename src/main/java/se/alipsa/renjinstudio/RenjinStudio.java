package se.alipsa.renjinstudio;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RenjinStudio extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setTitle("Renjin Studio");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
