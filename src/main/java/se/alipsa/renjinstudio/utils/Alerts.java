package se.alipsa.renjinstudio.utils;

import javafx.scene.control.Alert;

public class Alerts {


  public static void info(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
