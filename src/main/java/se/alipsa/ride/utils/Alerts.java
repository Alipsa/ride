package se.alipsa.ride.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public class Alerts {


  public static void info(String title, String content) {
    showAlert(title, content, Alert.AlertType.INFORMATION);
  }

  public static void warn(String title, String content) {
    showAlert(title, content, Alert.AlertType.WARNING);
  }

  public static void showAlert(String title, String content, Alert.AlertType information) {
    Platform.runLater(() -> {

      TextArea textArea = new TextArea(content);
      textArea.setEditable(false);
      textArea.setWrapText(true);
      BorderPane pane = new BorderPane();
      pane.setCenter(textArea);

      Alert alert = new Alert(information);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.getDialogPane().setContent(pane);
      alert.setResizable(true);
      alert.showAndWait();
    });
  }

}
