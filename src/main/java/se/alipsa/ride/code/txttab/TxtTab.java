package se.alipsa.ride.code.txttab;

import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class TxtTab extends TextAreaTab {

  TxtTextArea txtTextArea;

  public TxtTab(String title, Ride gui) {
    super(gui);
    setText(title);
    txtTextArea = new TxtTextArea(this);
    setContent(txtTextArea);
  }

  public TxtTab(String title, String content, Ride gui) {
    super(gui);
    setText(title);
    txtTextArea = new TxtTextArea(this);
    setContent(txtTextArea);
    txtTextArea.replaceText(0, 0, content);
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
  public String getTitle() {
    return getText();
  }

  @Override
  public void setTitle(String title) {
    setText(title);
  }

}
