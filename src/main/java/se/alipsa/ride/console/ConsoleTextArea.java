package se.alipsa.ride.console;

import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class ConsoleTextArea extends TextArea {

  public ConsoleTextArea() {
    setFont(Font.font("Courier New"));
  }
  public void append(String text, boolean... skipNewline) {

    boolean skip = skipNewline.length > 0 ? skipNewline[0]  : false;
    String sep = "\n";
    if (skip) {
      sep = "";
    }
    setText(getText() + sep + text);
  }

}
