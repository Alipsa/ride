package se.alipsa.ride.code.sqltab;

import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.javatab.JavaTextArea;

import java.io.File;

public class SqlTab extends TextAreaTab {

  SqlTextArea sqlTextArea;

  public SqlTab(String title, Ride gui) {
    super(gui);
    setTitle(title);
    sqlTextArea = new SqlTextArea(this);
    VirtualizedScrollPane<SqlTextArea> pane = new VirtualizedScrollPane<>(sqlTextArea);
    setContent(pane);
  }

  @Override
  public File getFile() {
    return sqlTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    sqlTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return sqlTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return sqlTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    sqlTextArea.replaceContentText(start, end, content);
  }
}
