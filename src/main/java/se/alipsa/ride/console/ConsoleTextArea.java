package se.alipsa.ride.console;

import javafx.application.Platform;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;

import static se.alipsa.ride.menu.GlobalOptions.CONSOLE_MAX_LENGTH_PREF;

public class ConsoleTextArea extends CodeArea {

  private static final String WARN_SIZE_MSG = "\nMaximum size for console reached, printing to standard out until console is cleared\n";
  public static int MAX_LENGTH = 900_000;
  Logger log = LoggerFactory.getLogger(ConsoleTextArea.class);
  private StringBuilder buffer = new StringBuilder();
  private StringBuilder warnBuffer = new StringBuilder();

  private boolean sizeWWarningPrinted = false;

  public ConsoleTextArea(Ride gui) {
    getStyleClass().add("console");
    MAX_LENGTH = gui.getPrefs().getInt(CONSOLE_MAX_LENGTH_PREF, MAX_LENGTH);
  }

  @Override
  public void clear() {
    sizeWWarningPrinted = false;
    super.clear();
  }

  @Override
  public void appendText(String text) {
    int textSize = getText().length();
    if (textSize > MAX_LENGTH) {
      if (!sizeWWarningPrinted) {
        printSizeWarning();
      }
      System.out.print(text);
    } else {
      super.appendText(text);
    }

  }

  private void printSizeWarning() {
    int start = getCaretPosition();
    int end = getCaretPosition() + WARN_SIZE_MSG.length();
    super.appendText(WARN_SIZE_MSG);
    setStyleClass(start, end, "warning");
    sizeWWarningPrinted = true;
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
    appendWithStyle(text, "warning", skipNewline);
  }

  public void appendError(String text, boolean... skipNewline) {
    appendWithStyle(text, "error", skipNewline);
  }

  private void appendWithStyle(String text, String styleClass, boolean... skipNewline) {
    //log.info("Appending warning lenght = {} for {}", text.length(), text);
    int start = getCaretPosition();
    int end = getCaretPosition() + text.length();
    append(text, skipNewline);
    //log.info("start was {}, end was {}, now at {}", start, end, getCaretPosition());
    setStyleClass(start, end, styleClass);
  }


  public void appendChar(char b) {
    buffer.append(b);
    if (b == '\n') {
      String text = buffer.toString();
      appendToFxThread(text);
      buffer.setLength(0);
    }
  }

  private void appendToFxThread(String text) {
    Platform.runLater(() -> {
      appendText(text);
    });
  }

  private void appendWarnToFxThread(String text) {
    Platform.runLater(() -> {
      appendWarning(text, true);
    });
  }

  public void appendWarnChar(char b) {
    warnBuffer.append(b);
    if (b == '\n') {
      String text = warnBuffer.toString();
      appendWarnToFxThread(text);
      warnBuffer.setLength(0);
    }
  }

  public void flush() {
    Platform.runLater(() -> {
      appendText(buffer.toString());
      buffer.setLength(0);
      appendWarning(warnBuffer.toString());
      warnBuffer.setLength(0);
    });
  }

  public void setConsoleMaxSize(int size) {
    MAX_LENGTH = size;
  }
}
