package se.alipsa.ride.code.munin;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.TaskListener;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.mdrtab.MdrTextArea;
import se.alipsa.ride.code.rtab.RTextArea;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.Alerts;

import java.io.File;

public abstract class MuninTab extends TextAreaTab implements TaskListener {

  private final CodeTextArea codeTextArea;
  MuninConnection muninConnection;
  MuninReport muninReport;

  protected Button viewButton;
  protected Button publishButton;

  private static Logger log = LogManager.getLogger(MuninTab.class);

  public MuninTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, "MDR".equals(report.getReportType()) ? CodeType.MDR : CodeType.R);
    muninConnection = con;
    codeTextArea = getCodeType() == CodeType.MDR ? new MdrTextArea(this) : new RTextArea(this);
    setTitle(report.getReportName());

    viewButton = new Button();
    viewButton.setGraphic(new ImageView(IMG_VIEW));
    viewButton.setTooltip(new Tooltip("Render and view"));
    viewButton.setOnAction(this::viewAction);
    buttonPane.getChildren().add(viewButton);

    publishButton = new Button();
    publishButton.setGraphic(new ImageView(IMG_PUBLISH));
    publishButton.setTooltip(new Tooltip("Publish to server"));
    publishButton.setOnAction(this::publishReport);
    buttonPane.getChildren().add(publishButton);

    VirtualizedScrollPane<CodeTextArea> vPane = new VirtualizedScrollPane<>(codeTextArea);
    pane.setCenter(vPane);
    //gui.getEnvironmentComponent().addContextFunctionsUpdateListener(codeTextArea);
    //setOnClosed(e -> gui.getEnvironmentComponent().removeContextFunctionsUpdateListener(codeTextArea));
  }

  private void publishReport(ActionEvent actionEvent) {
    Alerts.info("Not yet implemented", "publish the report to " + muninConnection.target());
  }

  abstract void viewAction(ActionEvent actionEvent);

  @Override
  public File getFile() {
    return codeTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    codeTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return codeTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return codeTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    codeTextArea.replaceContentText(start, end, content);
  }

  @Override
  public void taskStarted() {
    viewButton.setDisable(true);
  }

  @Override
  public void taskEnded() {
    viewButton.setDisable(false);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return codeTextArea;
  }
}
