package se.alipsa.ride.inout;

import javafx.beans.value.ChangeListener;
import javafx.scene.text.Text;

import java.io.File;

public class FileItem {

  protected File file;
  protected Text caption = new Text();

  public FileItem(File file) {
    this.file = file;
    caption.setText(file.getName());
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String getText() {
    return caption.getText();
  }

  public void addListener(ChangeListener<String> changeListener) {
    caption.styleProperty().addListener(changeListener);
  }

  public void removeListener(ChangeListener<String> changeListener) {
    caption.styleProperty().removeListener(changeListener);
  }

  public void setStyle(String style) {
    caption.setStyle(style);
  }

  public String getStyle() {
    return caption.getStyle();
  }

  public String toString() {
    return getText();
  }
}
