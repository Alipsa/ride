package se.alipsa.ride.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import se.alipsa.ride.Constants;

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

  public static void infoStyled(String title, String content) {
    Platform.runLater(() -> {

      WebView view = new WebView();
      view.getEngine().setUserStyleSheetLocation(FileUtils.getResourceUrl(Constants.BRIGHT_THEME).toExternalForm());
      view.getEngine().loadContent(content);

      BorderPane pane = new BorderPane();
      pane.setCenter(view);

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.getDialogPane().setContent(pane);
      alert.setResizable(true);
      alert.showAndWait();
    });
  }
}
