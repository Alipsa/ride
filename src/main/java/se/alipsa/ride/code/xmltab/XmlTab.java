package se.alipsa.ride.code.xmltab;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabType;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class XmlTab extends TextAreaTab {

  private XmlTextArea xmlTextArea;

  public XmlTab(String title, Ride gui) {
    super(gui, TabType.XML);
    setTitle(title);
    xmlTextArea = new XmlTextArea(this);
    VirtualizedScrollPane xmlPane = new VirtualizedScrollPane<>(xmlTextArea);
    pane.setCenter(xmlPane);
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
  public void replaceContentText(int start, int end, String content) {
    xmlTextArea.replaceContentText(start, end, content);
  }

  @Override
  public CodeArea getCodeArea() {
    return xmlTextArea;
  }
}
