package se.alipsa.ride.console;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleTextArea extends CodeArea {

  Logger log = LoggerFactory.getLogger(ConsoleTextArea.class);

  public ConsoleTextArea() {
    setStyle("-fx-font-family: consolas; -fx-font-size: 10pt;");
    //setFont(Font.font("Courier New"));
  }

  public void append(String text, boolean... skipNewline) {
    boolean skip = skipNewline.length > 0 ? skipNewline[0] : false;
    if (skip) {
      appendText(text);
    } else {
      appendText(text + "\n");
    }
  }

  public void appendWarning(String text, boolean... skipNewline) {
    appendWithStyle(text,"warning", skipNewline);
  }

  public void appendError(String text, boolean... skipNewline) {
    appendWithStyle(text,"error", skipNewline);
  }

  private void appendWithStyle(String text, String styleClass, boolean... skipNewline) {
    //log.info("Appending warning lenght = {} for {}", text.length(), text);
    int start = getCaretPosition();
    int end = getCaretPosition() + text.length();
    append(text, skipNewline);
    //log.info("start was {}, end was {}, now at {}", start, end, getCaretPosition());
    setStyleClass(start, end, styleClass);
  }


}
