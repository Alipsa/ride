package se.alipsa.ride.code.munin;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.GuiUtils;

import java.util.List;
import java.util.Map;

public class PublishDialog extends Dialog<Void> {

  private static final Logger log = LogManager.getLogger(PublishDialog.class);

  private final Ride gui;
  private final MuninConnection muninConnection;
  private final MuninReport muninReport;
  private MuninTab muninTab;
  private BorderPane borderPane;
  private TextArea ta;

  public PublishDialog(Ride gui, MuninConnection muninConnection, MuninTab muninTab) {
    this.gui = gui;
    this.muninConnection = muninConnection;
    this.muninReport = muninTab.getMuninReport();
    this.muninTab = muninTab;
    setTitle("Publish " + muninReport.getReportName() + " to " + muninConnection.target());
    getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
    borderPane = new BorderPane();
    getDialogPane().setContent(borderPane);
    ta = new TextArea();
    borderPane.setCenter(ta);
    ta.setText("Checking if the report exists....");
    GuiUtils.addStyle(gui, this);
    checkExisting();
  }

  private void checkExisting() {
    gui.setWaitCursor();
    Task<Boolean> task = new Task<>() {
      @Override
      protected Boolean call() throws Exception {
        Map<String, List<String>> reportInfo = MuninClient.fetchReportInfo(muninConnection);
        for (Map.Entry<String, List<String>> entry : reportInfo.entrySet()) {
          if (entry.getValue().contains(muninReport.getReportName())) return true;
        }
        return false;
      }
    };
    task.setOnFailed(e -> {
      gui.setNormalCursor();
      ExceptionAlert.showAlert("Failed to get report info", task.getException());
    });

    task.setOnSucceeded(e -> {
      gui.setNormalCursor();
      if (task.getValue()) {
        ta.appendText("\n" + muninReport.getReportName() + " already exists");
        confirmOverwriteAndPublish();
      } else {
        publish(true);
      }
    });
    new Thread(task).start();
  }

  private void confirmOverwriteAndPublish() {
    HBox bottomBox = new HBox();
    bottomBox.setAlignment(Pos.CENTER_LEFT);
    bottomBox.setSpacing(3);
    bottomBox.setPadding(new Insets(5));
    borderPane.setBottom(bottomBox);
    Button overwriteBtn = new Button("Overwrite");
    overwriteBtn.setOnAction(a -> {
      ta.appendText("\nOverwrite confirmed.");
      publish(false);
      overwriteBtn.setDisable(true);
    });
    bottomBox.getChildren().addAll(new Label("Please confirm: "), overwriteBtn);
  }


  private void publish(boolean isAddNew) {
    gui.setWaitCursor();
    System.out.println("Publishing report...");
    ta.appendText("\nPublishing report...");
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        MuninClient.publishReport(muninConnection, muninReport, isAddNew);
        return null;
      }
    };

    task.setOnFailed(e -> {
      System.err.println("Update report failed!");
      gui.setNormalCursor();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      ExceptionAlert.showAlert(ex.getMessage(), ex);
    });

    task.setOnSucceeded(e -> {
      log.info("Successfully published the report {}", muninReport.getReportName());
      gui.setNormalCursor();
      ta.appendText("\nSuccessfully published " + muninReport.getReportName() + " to " + muninConnection.target());
    });
    new Thread(task).start();
  }
}
