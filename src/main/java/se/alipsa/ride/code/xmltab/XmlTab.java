package se.alipsa.ride.code.xmltab;

import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class XmlTab extends TextAreaTab {

  XmlTextArea xmlTextArea;

  public XmlTab(String title) {
    setText(title);
    xmlTextArea = new XmlTextArea();
    VirtualizedScrollPane pane = new VirtualizedScrollPane<>(xmlTextArea);
    setContent(pane);
  }

  @Override
  public File getFile() {
    return xmlTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    xmlTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return xmlTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return xmlTextArea.getAllTextContent();
  }

  @Override
  public void replaceText(int start, int end, String content) {
    xmlTextArea.replaceText(start, end, content);
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
