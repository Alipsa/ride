package se.alipsa.ride.utils;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;

import java.net.URL;
import java.util.Optional;

public class Alerts {


  public static  Optional<ButtonType> info(String title, String content) {
    return showAlert(title, content, Alert.AlertType.INFORMATION);
  }

  public static void infoFx(String title, String content) {
    showAlertFx(title, content, Alert.AlertType.INFORMATION);
  }

  public static  Optional<ButtonType> warn(String title, String content) {
    return showAlert(title, content, Alert.AlertType.WARNING);
  }

  public static void warnFx(String title, String content) {
    showAlertFx(title, content, Alert.AlertType.WARNING);
  }

  public static boolean confirm(String title, String headerText, String contentText) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.YES, ButtonType.NO);
    Ride gui = Ride.instance();
    alert.setTitle(title);
    alert.setHeaderText(headerText);

    alert.initOwner(gui.getStage());
    String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

    URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
    if (styleSheetUrl != null) {
      alert.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
    }

    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    stage.getIcons().addAll(gui.getStage().getIcons());

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.YES) {
      return true;
    }
    return false;
  }
  public static Optional<ButtonType> showAlert(String title, String content, Alert.AlertType information) {

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

      Ride gui = Ride.instance();
      if (gui != null) {
         String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

         URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
         if (styleSheetUrl != null) {
            alert.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
         }

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().addAll(gui.getStage().getIcons());
      }

      return alert.showAndWait();
  }

  public static void showAlertFx(String title, String content, Alert.AlertType information) {
    Platform.runLater(() -> showAlert(title, content, information));
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
      Ride gui = Ride.instance();
      String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);
      URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
      if (styleSheetUrl != null) {
         alert.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
      }
      Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
      stage.getIcons().addAll(gui.getStage().getIcons());
      alert.setResizable(true);
      alert.showAndWait();
    });
  }
}
