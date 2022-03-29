package se.alipsa.ride.code.mdrtab;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.TaskListener;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class MdrTab extends TextAreaTab implements TaskListener {

  private final MdrTextArea mdrTextArea;
  Button viewButton;
  Button htmlButton;
  Button pdfButton;

  public MdrTab(String title, Ride gui) {
    super(gui, CodeType.MDR);
    setTitle(title);

    mdrTextArea = new MdrTextArea(this);
    VirtualizedScrollPane<MdrTextArea> txtPane = new VirtualizedScrollPane<>(mdrTextArea);
    pane.setCenter(txtPane);

    viewButton = new Button();
    viewButton.setGraphic(new ImageView(IMG_VIEW));
    viewButton.setTooltip(new Tooltip("Render and view"));
    viewButton.setOnAction(this::viewMdr);
    buttonPane.getChildren().add(viewButton);

    htmlButton = new Button();
    htmlButton.setGraphic(new ImageView(IMG_PUBLISH));
    htmlButton.setStyle("-fx-border-color: darkgreen");
    htmlButton.setTooltip(new Tooltip("Export to html"));
    htmlButton.setOnAction(this::exportToHtml);
    buttonPane.getChildren().add(htmlButton);

    pdfButton = new Button();
    pdfButton.setGraphic(new ImageView(IMG_PUBLISH));
    pdfButton.setStyle("-fx-border-color: #70503B");
    pdfButton.setTooltip(new Tooltip("Export to pdf"));
    pdfButton.setOnAction(this::exportToPdf);
    buttonPane.getChildren().add(pdfButton);

  }

  private void viewMdr(ActionEvent actionEvent) {
    MdrUtil.viewMdr(gui, getTitle(), getTextContent());
  }

  private void exportToPdf(ActionEvent actionEvent) {
    FileChooser fc = new FileChooser();
    fc.setTitle("Save PDF File");
    String initialFileName = getTitle().replace("*", "").replace(".mdr", "");
    if (initialFileName.endsWith(".")) {
      initialFileName = initialFileName + "pdf";
    } else {
      initialFileName = initialFileName + ".pdf";
    }
    fc.setInitialDirectory(gui.getInoutComponent().getRootDir());
    fc.setInitialFileName(initialFileName);
    fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
    File outFile = fc.showSaveDialog(Ride.instance().getStage());
    if (outFile == null) {
      return;
    }
    MdrUtil.saveMdrAsPdf(gui, outFile, getTextContent());
  }

  private void exportToHtml(ActionEvent actionEvent) {
    FileChooser fc = new FileChooser();
    fc.setTitle("Save HTML File");
    String initialFileName = getTitle().replace("*", "").replace(".mdr", "");
    if (initialFileName.endsWith(".")) {
      initialFileName = initialFileName + "html";
    } else {
      initialFileName = initialFileName + ".html";
    }
    fc.setInitialDirectory(gui.getInoutComponent().getRootDir());
    fc.setInitialFileName(initialFileName);
    fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("HTML", "*.html"));
    File outFile = fc.showSaveDialog(Ride.instance().getStage());
    if (outFile == null) {
      return;
    }
    MdrUtil.saveMdrAsHtml(gui, outFile, getTextContent());
  }

  @Override
  public CodeTextArea getCodeArea() {
    return mdrTextArea;
  }

  @Override
  public File getFile() {
    return mdrTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    mdrTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return mdrTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return mdrTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    mdrTextArea.replaceContentText(start, end, content);
  }

  @Override
  public void taskStarted() {
    viewButton.setDisable(true);
    htmlButton.setDisable(true);
    pdfButton.setDisable(true);
  }

  @Override
  public void taskEnded() {
    viewButton.setDisable(false);
    htmlButton.setDisable(false);
    pdfButton.setDisable(false);
  }
}
