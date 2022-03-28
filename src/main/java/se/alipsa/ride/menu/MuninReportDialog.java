package se.alipsa.ride.menu;

import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.munin.MuninClient;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.GuiUtils;

import java.util.List;
import java.util.Optional;

public class MuninReportDialog extends Dialog<MuninReport> {

  private final ListView<String> reportGroupsLV = new ListView<>();
  private final ListView<MuninReport> muninReportLV = new ListView<>();

  public MuninReportDialog(Ride gui) {
    getDialogPane().setPrefWidth(600);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    GuiUtils.addStyle(gui, this);
    setTitle("Load Munin report");
    BorderPane pane = new BorderPane();
    getDialogPane().setContent(pane);

    VBox leftBox = new VBox();
    leftBox.getChildren().add(new Label("Report groups"));
    leftBox.getChildren().add(reportGroupsLV);
    pane.setLeft(leftBox);

    VBox centerBox = new VBox();
    centerBox.getChildren().add(new Label("Reports"));
    centerBox.getChildren().add(muninReportLV);
    pane.setCenter(centerBox);

    MuninConnection con = (MuninConnection) gui.getSessionObject(Constants.SESSION_MUNIN_CONNECTION);
    if (con.getPassword() == null || "".equals(con.getPassword().trim())) {
      PasswordDialog dialog = new PasswordDialog(gui, "Password to munin server", con.getUserName());
      Optional<String> passwdOpt = dialog.showAndWait();
      passwdOpt.ifPresent(con::setPassword);
    }

    reportGroupsLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> populateReportsView(con, newValue));

    muninReportLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        getDialogPane().lookupButton(ButtonType.OK).setDisable(false)
    );

    setResultConverter(callback -> callback == ButtonType.OK ? muninReportLV.getSelectionModel().getSelectedItem() : null);
    if (con.getPassword() != null && !con.getPassword().trim().isEmpty()) {
      populateGroups(con);
    } else {
      getDialogPane().setContent(new Label("No password supplied, nothing to do here!"));
      getDialogPane().getButtonTypes().clear();
      getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }
  }

  private void populateReportsView(MuninConnection con, String groupName) {
    if (groupName == null) return;

    Task<List<MuninReport>> task = new Task<>() {
      @Override
      protected List<MuninReport> call() throws Exception {
        return MuninClient.fetchReports(con, groupName);
      }
    };

    task.setOnFailed(e -> {
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      ExceptionAlert.showAlert(ex.getMessage(), ex);
    });

    task.setOnSucceeded(e -> {
      List<MuninReport> reports = task.getValue();
      //System.out.println("Reports are " + reports);
      muninReportLV.getItems().clear();
      muninReportLV.getItems().addAll(reports);
    });
    new Thread(task).start();
  }

  private void populateGroups(MuninConnection con) {
    Task<List<String>> task = new Task<>() {
      @Override
      protected List<String> call() throws Exception {
        return MuninClient.fetchReportGroups(con);
      }
    };
    task.setOnFailed(e -> {
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      ExceptionAlert.showAlert(ex.getMessage(), ex);
    });

    task.setOnSucceeded(e -> {
      List<String> reportGroups = task.getValue();
      //System.out.println("Report groups are " + reportGroups);
      reportGroupsLV.getItems().clear();
      reportGroupsLV.getItems().addAll(reportGroups);
    });
    new Thread(task).start();
  }
}
