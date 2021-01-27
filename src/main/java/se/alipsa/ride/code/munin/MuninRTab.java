package se.alipsa.ride.code.munin;

import javafx.event.ActionEvent;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.Alerts;

public class MuninRTab extends MuninTab {

  public MuninRTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, report, con);
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
    Alerts.info("Not implemented", "Should run and view as html");
  }
}
