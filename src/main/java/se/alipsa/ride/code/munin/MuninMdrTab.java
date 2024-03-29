package se.alipsa.ride.code.munin;

import javafx.event.ActionEvent;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.mdrtab.MdrUtil;
import se.alipsa.ride.model.MuninReport;

public class MuninMdrTab extends MuninTab {

  public MuninMdrTab(Ride gui, MuninReport report) {
    super(gui, report);
    getMiscTab().setReportType(ReportType.MDR);
    if (report.getDefinition() != null) {
      replaceContentText(0,0, report.getDefinition());
    }
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
    MdrUtil.viewMdr(gui, getTitle(), getTextContent());
    gui.getConsoleComponent().updateEnvironment();
  }
}
