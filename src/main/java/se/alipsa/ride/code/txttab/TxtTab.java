package se.alipsa.ride.code.txttab;

import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.CodeTextArea;

import java.io.File;

public class TxtTab extends TextAreaTab {

  private final TxtTextArea txtTextArea;

  public TxtTab(String title, Ride gui) {
    super(gui, CodeType.TXT);
    setTitle(title);
    txtTextArea = new TxtTextArea(this);
    VirtualizedScrollPane<TxtTextArea> txtPane = new VirtualizedScrollPane<>(txtTextArea);
    pane.setCenter(txtPane);
  }

  @Override
  public File getFile() {
    return txtTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    txtTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return txtTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return txtTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    txtTextArea.replaceContentText(start, end, content);
  }

  @Override
  public void replaceContentText(String content) {
    txtTextArea.replaceContentText(content);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return txtTextArea;
  }
}
