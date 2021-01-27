package se.alipsa.ride.code.mdrtab;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class MdrTab extends TextAreaTab {

  private final Button viewButton;
  private MdrTextArea mdrTextArea;

  public MdrTab(String title, Ride gui) {
    super(gui, CodeType.MDR);
    setTitle(title);
    viewButton = new Button();
    viewButton.setGraphic(new ImageView(IMG_VIEW));
    viewButton.setTooltip(new Tooltip("Render and view"));
    viewButton.setOnAction(this::viewMdr);
    buttonPane.getChildren().add(viewButton);
    mdrTextArea = new MdrTextArea(this);
    VirtualizedScrollPane<MdrTextArea> txtPane = new VirtualizedScrollPane<>(mdrTextArea);
    pane.setCenter(txtPane);
  }

  private void viewMdr(ActionEvent actionEvent) {
    MdrViewerUtil.viewMdr(gui, getTitle(), getTextContent());
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
}
