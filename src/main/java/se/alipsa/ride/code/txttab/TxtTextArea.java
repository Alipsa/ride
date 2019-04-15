package se.alipsa.ride.code.txttab;

import javafx.beans.InvalidationListener;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.model.StyleSpans;
import se.alipsa.ride.code.TabTextArea;
import se.alipsa.ride.code.CodeTextArea;

import java.io.File;
import java.util.Collection;

public class TxtTextArea extends CodeTextArea implements TabTextArea {

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
    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown() && KeyCode.F.equals(e.getCode())) {
        parent.getGui().getMainMenu().displayFind();
      }
    });
    this.textProperty().addListener(contentChangeListener);
  }

  @Override
  protected StyleSpans<Collection<String>> computeHighlighting(String text) {
    return null;
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
