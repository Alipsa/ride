package se.alipsa.ride.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

public class ExceptionAlert extends Alert {

  public ExceptionAlert() {
    super(AlertType.ERROR);
  }

  /**
   * display the exception.
   */
  public static Optional<ButtonType> showAlert(String message, Throwable throwable) {
    throwable.printStackTrace();
    Alert alert = new ExceptionAlert();
    alert.setTitle("Exception Dialog");
    alert.setHeaderText("An Exception Occurred");
    alert.setContentText(message);

    // Create expandable Exception.
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    String exceptionText = sw.toString();

    UnStyledCodeArea textArea = new UnStyledCodeArea();
    textArea.getStyleClass().add("txtarea");
    textArea.replaceText(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    textArea.setMinHeight(Region.USE_PREF_SIZE);


    Label label = new Label("The exception stacktrace was:");
    GridPane expContent = new GridPane();
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);
    expContent.setMinHeight(Region.USE_PREF_SIZE);

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);

    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

    Ride gui = Ride.instance();
    if (gui != null) {
      String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

      URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
      if (styleSheetUrl != null) {
        alert.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
      }
    }

    return alert.showAndWait();
  }

}
