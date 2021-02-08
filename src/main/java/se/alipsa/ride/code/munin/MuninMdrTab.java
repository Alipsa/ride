package se.alipsa.ride.code.munin;

import javafx.event.ActionEvent;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.mdrtab.MdrViewerUtil;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;

public class MuninMdrTab extends MuninTab {

  public MuninMdrTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, report, con);
    getMiscTab().setReportType(ReportType.MDR);
    if (report.getDefinition() != null) {
      replaceContentText(0,0, report.getDefinition());
    }
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
    MdrViewerUtil.viewMdr(gui, getTitle(), getTextContent());
    gui.getConsoleComponent().updateEnvironment();
  }
}
