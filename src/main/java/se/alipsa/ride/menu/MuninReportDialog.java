package se.alipsa.ride.menu;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.GuiUtils;

import java.util.List;
import java.util.Optional;

public class MuninReportDialog extends Dialog<MuninReport> {

  private MuninConnection con;
  private final ListView<String> reportGroupsLV = new ListView<>();
  private final ListView<MuninReport> muninReportLV = new ListView<>();

  public MuninReportDialog(Ride gui) {
    getDialogPane().setPrefWidth(600);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    GuiUtils.addStyle(gui, this);
    setTitle("Load Munin report");
    BorderPane pane = new BorderPane();
    getDialogPane().setContent(pane);

    pane.setLeft(reportGroupsLV);
    pane.setCenter(muninReportLV);

    MuninConnection con = (MuninConnection) gui.getSessionObject(Constants.SESSION_MUNIN_CONNECTION);
    if (con.getPassword() == null || "".equals(con.getPassword().trim())) {
      PasswordDialog dialog = new PasswordDialog(gui, "Password to munin server", con.getUserName());
      Optional<String> passwdOpt = dialog.showAndWait();
      if (passwdOpt.isPresent()) {
        con.setPassword(passwdOpt.get());
      } else {
        return;
      }
    }

    reportGroupsLV.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      System.out.println("Selected item: " + newValue);
      populateReportsView(con, newValue);
    });

    populateGroups(con);
    setResultConverter(callback -> muninReportLV.getSelectionModel().getSelectedItem());


  }

  private void populateReportsView(MuninConnection con, String groupName) {
    if (groupName == null) return;

    Task<List<MuninReport>> task = new Task<List<MuninReport>>() {
      @Override
      protected List<MuninReport> call() throws Exception {
        Client client = ClientBuilder.newClient();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(con.getUserName(), con.getPassword());
        client.register(feature);
        WebTarget target = client.target(con.target()).path("/api/getReports").queryParam("groupName", groupName);
        Response response = null;
        try {
          response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        } catch (ProcessingException e) {
          throw new Exception("Failed to fetch reports on Munin server: " + con.target(), e);
        }
        if (response.getStatus() != 200) {
          throw new Exception("Failed to fetch reports on Munin server: " + con.target()
              + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
          );
        }
        return response.readEntity(new GenericType<List<MuninReport>>() {});
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
      System.out.println("Reports are " + reports);
      muninReportLV.getItems().clear();
      muninReportLV.getItems().addAll(reports);
    });
    new Thread(task).start();
  }

  private void populateGroups(MuninConnection con) {
    Task<List<String>> task = new Task<List<String>>() {
      @Override
      protected List<String> call() throws Exception {
        Client client = ClientBuilder.newClient();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(con.getUserName(), con.getPassword());
        client.register(feature);
        WebTarget target = client.target(con.target()).path("/api/getReportGroups");
        Response response = null;
        try {
          response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        } catch (ProcessingException e) {
          throw new Exception("Failed to fetch report groups on Munin server: " + con.target(), e);
        }
        if (response.getStatus() != 200) {
          throw new Exception("Failed to fetch report groups on Munin server: " + con.target()
              + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
        }
        return response.readEntity(new GenericType<List<String>>() {});
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
      System.out.println("Report groups are " + reportGroups);
      reportGroupsLV.getItems().clear();
      reportGroupsLV.getItems().addAll(reportGroups);
    });
    new Thread(task).start();
  }
}
