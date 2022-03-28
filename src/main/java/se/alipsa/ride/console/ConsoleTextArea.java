package se.alipsa.ride.console;

import static se.alipsa.ride.menu.GlobalOptions.CONSOLE_MAX_LENGTH_PREF;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;

public class ConsoleTextArea extends UnStyledCodeArea {

  private static final String WARN_SIZE_MSG = "\nMaximum size for console reached, printing to standard out until console is cleared\n";

  public static final int CONSOLE_MAX_LENGTH_DEFAULT = 1_500_000;
  private int consoleMaxLength = CONSOLE_MAX_LENGTH_DEFAULT;
  static Logger log = LogManager.getLogger(ConsoleTextArea.class);
  private final StringBuilder buffer = new StringBuilder();
  private final StringBuilder warnBuffer = new StringBuilder();

  private boolean sizeWWarningPrinted = false;


  private ConsoleTextArea() {
    getStyleClass().add("console");
    //setUseInitialStyleForInsertion(false);
    /*
    System.out.println("Stylesheets for " + getClass().getSimpleName());
    for (String sheet : getStylesheets()) {
      System.out.println(sheet);
    }
    System.out.println("Style classes for " + getClass().getSimpleName());
    for (String styleClass : getStyleClass()) {
      System.out.println(styleClass);
    }
     */
  }

  public ConsoleTextArea(Ride gui) {
    this();
    consoleMaxLength = gui.getPrefs().getInt(CONSOLE_MAX_LENGTH_PREF, CONSOLE_MAX_LENGTH_DEFAULT);
  }

  @Override
  public void clear() {
    sizeWWarningPrinted = false;
    super.clear();
  }

  @Override
  public void appendText(String text) {
    int textSize = getText().length();
    if (textSize > consoleMaxLength) {
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

  public void append(String text, boolean... addNewline) {
    boolean addLf = addNewline.length > 0 && addNewline[0];
    if (addLf) {
      appendText(text + "\n");
    } else {
      appendText(text);
    }
  }

  public void appendFx(String text, boolean... addNewline) {
    Platform.runLater(() -> append(text, addNewline));
  }

  public void appendWarning(String text, boolean... addNewline) {
    if (text != null && text.trim().length() != 0) {
      appendWithStyle(text, "warning", addNewline);
    }
  }

  public void appendError(String text, boolean... addNewline) {
    if (text != null && text.trim().length() != 0) {
      appendWithStyle(text, "error", addNewline);
    }
  }

  private void appendWithStyle(String text, String styleClass, boolean... addNewline) {
    //log.info("Appending warning lenght = {} for {}", text.length(), text);
    int start = getCaretPosition();
    int end = getCaretPosition() + text.length();
    append(text, addNewline);
    //log.info("start was {}, end was {}, now at {}", start, end, getCaretPosition());
    setStyleClass(start, end, styleClass);
  }


  public void appendChar(char b) {
    buffer.append(b);
    if (b == '\n') {
      String text = buffer.toString();
      appendFx(text);
      buffer.setLength(0);
    }
  }

  public void appendChar(char[] b) {
    buffer.append(b);
    if ("\n".equals(String.valueOf(b))) {
      String text = buffer.toString();
      appendFx(text, true);
      buffer.setLength(0);
    }
  }


  public void appendWarningFx(String text) {
    Platform.runLater(() -> appendWarning(text, true));
  }

  public void appendWarnChar(char b) {
    warnBuffer.append(Character.toChars(b));
    if (b == '\n') {
      String text = warnBuffer.toString();
      appendWarningFx(text);
      warnBuffer.setLength(0);
    }
  }

  public void appendWarnChar(char[] b) {
    warnBuffer.append(b);
    if ("\n".equals(String.valueOf(b))) {
      String text = warnBuffer.toString();
      appendWarningFx(text);
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
    consoleMaxLength = size;
  }

  public int getConsoleMaxSize() {
    return consoleMaxLength;
  }

  public void appendNewlineIfNeeded() {
    String lastLine = getText(getCurrentParagraph());
    if (!lastLine.trim().isEmpty()) {
      appendText("\n");
    }
  }
}
