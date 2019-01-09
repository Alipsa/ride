package se.alipsa.ride.code.txttab;

import javafx.beans.InvalidationListener;
import org.fxmisc.richtext.CodeArea;
import se.alipsa.ride.code.TabTextArea;

import java.io.File;

public class TxtTextArea extends CodeArea implements TabTextArea {

  private File file;
  private boolean contentChanged = false;
  private InvalidationListener contentChangeListener;

  public TxtTextArea(TxtTab parent) {
    contentChangeListener = observable -> {
      if (contentChanged == false) {
        parent.contentChanged();
        contentChanged = true;
      }
    };
    this.textProperty().addListener(contentChangeListener);
  }


  @Override
  public File getFile() {
    return file;
  }

  @Override
  public void setFile(File file) {
    this.file = file;
  }

  @Override
  public String getTextContent() {
    return getText();
  }

  @Override
  public String getAllTextContent() {
    return getText();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    this.textProperty().removeListener(contentChangeListener);
    replaceText(start, end, content);
    this.textProperty().addListener(contentChangeListener);
  }
}
