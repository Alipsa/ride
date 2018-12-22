package se.alipsa.ride.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionAlert extends Alert {
  
  public ExceptionAlert() {
    super(AlertType.ERROR);
  }

  /** display the exception. */
  public static void showAlert(String message, Throwable throwable) {
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
  
    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);
  
    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    Label label = new Label("The exception stacktrace was:");
    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);
  
    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);
  
    alert.showAndWait();
  }

}
