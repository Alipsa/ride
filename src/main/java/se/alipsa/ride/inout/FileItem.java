package se.alipsa.ride.inout;

import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.File;

public class FileItem {

  private File file;
  private Text caption = new Text();

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

  public Paint getTextColor() {
    return caption.getFill();
  }

  public Text getCaption() {
    return caption;
  }

  public void setTextColor(Paint color) {
    caption.setFill(color);
    //TreeItem.TreeModificationEvent<File> event = new TreeItem.TreeModificationEvent<File>(TreeItem.valueChangedEvent(), this);
    //Event.fireEvent(this, event);
  }

  public String toString() {
    return getText();
  }
}
