package se.alipsa.renjinstudio.utils;

import javafx.scene.control.Alert;

public class Alerts {


  public static void info(String title, String content) {
    showAlert(title, content, Alert.AlertType.INFORMATION);
  }

  public static void warn(String title, String content) {
    showAlert(title, content, Alert.AlertType.WARNING);
  }

  public static void showAlert(String title, String content, Alert.AlertType information) {
    Alert alert = new Alert(information);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

}
