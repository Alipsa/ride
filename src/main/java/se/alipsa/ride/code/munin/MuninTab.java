package se.alipsa.ride.code.munin;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;

public abstract class MuninTab extends TextAreaTab implements TaskListener {

  private final CodeTextArea codeTextArea;
  private final MiscTab miscTab;
  MuninConnection muninConnection;
  MuninReport muninReport;

  protected Button viewButton;
  protected Button publishButton;

  private static Logger log = LogManager.getLogger(MuninTab.class);

  public MuninTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, "MDR".equals(report.getReportType()) ? CodeType.MDR : CodeType.R);
    muninConnection = con;
    muninReport = report;
    codeTextArea = getCodeType() == CodeType.MDR ? new MdrTextArea(this) : new RTextArea(this);
    miscTab = new MiscTab(this);
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
    TabPane tabPane = new TabPane();
    Tab codeTab = new Tab("code");
    codeTab.setContent(vPane);
    tabPane.getTabs().add(codeTab);
    tabPane.getTabs().add(miscTab);
    pane.setCenter(tabPane);
    //gui.getEnvironmentComponent().addContextFunctionsUpdateListener(codeTextArea);
    //setOnClosed(e -> gui.getEnvironmentComponent().removeContextFunctionsUpdateListener(codeTextArea));
  }

  private void publishReport(ActionEvent actionEvent) {
    muninReport = updateAndGetMuninReport();
    gui.setWaitCursor();
    System.out.println("Publishing report...");
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        MuninClient.updateReport(muninConnection, muninReport);
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
      System.out.println("Update report succeeded!");
      gui.setNormalCursor();
      Alerts.info("Publish successful",
          "Successfully published " + muninReport.getReportName() + " to " + muninConnection.target());
    });
    new Thread(task).start();
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

  public MuninReport getMuninReport() {
    return muninReport;
  }

  public MuninReport updateAndGetMuninReport() {
    muninReport.setDefinition(codeTextArea.getAllTextContent());
    muninReport.setReportName(miscTab.getReportName());
    muninReport.setDescription(miscTab.getDescription());
    muninReport.setReportGroup(miscTab.getReportGroup());
    muninReport.setReportType(miscTab.getReportType());
    muninReport.setInputContent(miscTab.getInputContent());
    return muninReport;
  }
}
