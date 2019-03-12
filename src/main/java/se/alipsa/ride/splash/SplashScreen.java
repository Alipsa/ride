package se.alipsa.ride.splash;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.alipsa.ride.utils.FileUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();
    Label altToImage = new Label("Loading Ride, please wait...");
    root.setTop(altToImage);

    Image logo = new Image(FileUtils.getResourceUrl("image/logo.png").toExternalForm());
    ImageView imageView = new ImageView(logo);
    root.setCenter(imageView);

    primaryStage.setTitle("Ride, a Renjin IDE");
    primaryStage.getIcons().add(new Image(FileUtils.getResourceUrl("image/logo.png").toExternalForm()));
    Scene scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.show();

    PauseTransition delay = new PauseTransition(Duration.seconds(5));
    delay.setOnFinished( event -> {
      primaryStage.close();
      Platform.exit();
      // Allow some time before calling system exist so stop() can be used to do stuff if neeed
      Timer timer = new Timer();
      TimerTask task = new TimerTask() {
        public void run() {
          System.exit(0);
        }
      };
      timer.schedule(task, 250);
    } );
    delay.play();
  }
}
