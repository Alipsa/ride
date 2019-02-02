package se.alipsa.ride.code.txttab;

import org.fxmisc.richtext.CodeArea;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.TextCodeArea;

import java.io.File;

public class TxtTab extends TextAreaTab {

  private TxtTextArea txtTextArea;

  public TxtTab(String title, Ride gui) {
    super(gui);
    setTitle(title);
    txtTextArea = new TxtTextArea(this);
    setContent(txtTextArea);
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
  public CodeArea getCodeArea() {
    return txtTextArea;
  }
}
