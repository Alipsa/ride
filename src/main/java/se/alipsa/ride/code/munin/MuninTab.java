package se.alipsa.ride.code.munin;

import static se.alipsa.ride.Constants.DEFAULT_MDR_REPORT_NAME;
import static se.alipsa.ride.Constants.DEFAULT_R_REPORT_NAME;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
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
import se.alipsa.ride.utils.git.GitUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class MuninTab extends TextAreaTab implements TaskListener {

  private final CodeTextArea codeTextArea;
  private final MiscTab miscTab;
  private MuninConnection muninConnection;
  private MuninReport muninReport;
  private TabPane tabPane = new TabPane();

  protected Button viewButton;
  protected Button publishButton;

  private static final Logger log = LogManager.getLogger(MuninTab.class);

  public static MuninTab fromFile(File file) {

    try {
      JAXBContext context = JAXBContext.newInstance(MuninReport.class);
      XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
      try(Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
        XMLStreamReader xmlReader = xmlInFact.createXMLStreamReader(reader);
        MuninReport report = context.createUnmarshaller().unmarshal(xmlReader, MuninReport.class).getValue();
        if (ReportType.MDR.equals(report.getReportType())) {
          return new MuninMdrTab(Ride.instance(), report, null);
        } else if (ReportType.UNMANAGED.equals(report.getReportType())) {
          return new MuninRTab(Ride.instance(), report, null);
        } else {
          Alerts.warn("Unknown report type", "Dont know how to process " + report.getReportType());
          throw new IllegalArgumentException("Unknown report type " + report.getReportType());
        }
      }
    } catch (IOException | XMLStreamException | JAXBException e) {
      ExceptionAlert.showAlert("Error reading mr file", e);
    }
    return null;
  }

  public MuninTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, ReportType.MDR.equals(report.getReportType()) ? CodeType.MDR : CodeType.R);
    muninConnection = con;
    muninReport = report;
    codeTextArea = getCodeType() == CodeType.MDR ? new MdrTextArea(this) : new RTextArea(this);
    miscTab = new MiscTab(this);
    setTitle(report.getReportName());

    saveButton.setOnAction(a -> saveContent());

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
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    Tab codeTab = new Tab("code");
    codeTab.setContent(vPane);
    tabPane.getTabs().add(codeTab);
    tabPane.getTabs().add(miscTab);
    pane.setCenter(tabPane);
    //gui.getEnvironmentComponent().addContextFunctionsUpdateListener(codeTextArea);
    //setOnClosed(e -> gui.getEnvironmentComponent().removeContextFunctionsUpdateListener(codeTextArea));
  }

  private void saveContent() {
    muninReport = updateAndGetMuninReport();
    File file = getFile();
    if (file == null) {
      file = gui.getMainMenu().promptForFile("Munin report file", MuninReport.FILE_EXTENSION);
      if (file == null) {
        return;
      }
    }
    try {
      saveFile(muninReport, file);
      Git git = gui.getInoutComponent().getGit();
      if(getTreeItem() != null && git != null) {
        String path = GitUtils.asRelativePath(getFile(), gui.getInoutComponent().getRootDir());
        GitUtils.colorNode(git, path, getTreeItem());
      }
    } catch (IOException | JAXBException e) {
      ExceptionAlert.showAlert("Failed to save file " + file, e);
    }
  }

  protected void saveFile(MuninReport report, File file) throws IOException, JAXBException {
    boolean fileExisted = file.exists();
    JAXBContext context = JAXBContext.newInstance(MuninReport.class);
    Marshaller mar= context.createMarshaller();
    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    mar.marshal(report, file);
    setTitle(file.getName());
    if (!fileExisted) {
      gui.getInoutComponent().fileAdded(file);
    }
    gui.getCodeComponent().fileSaved(file);
    contentSaved();
  }

  private void publishReport(ActionEvent actionEvent) {
    muninReport = updateAndGetMuninReport();
    String reportName = muninReport.getReportName();
    if (reportName == null || reportName.trim().length() == 0
        || DEFAULT_R_REPORT_NAME.equals(reportName) || DEFAULT_MDR_REPORT_NAME.equals(reportName)) {
      Alerts.warn("Missing report information", "You must specify a report name before it can be published");
      tabPane.getSelectionModel().select(miscTab);
      return;
    }
    if (muninConnection == null) {
      muninConnection = gui.getMainMenu().configureMuninConnection();
    }
    PublishDialog dialog = new PublishDialog(gui, muninConnection, this);
    dialog.showAndWait();
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

  public MiscTab getMiscTab() {
    return miscTab;
  }
}
