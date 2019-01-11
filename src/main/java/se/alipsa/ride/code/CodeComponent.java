package se.alipsa.ride.code;

import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.codetab.CodeTab;
import se.alipsa.ride.code.javatab.JavaTab;
import se.alipsa.ride.code.sqltab.SqlTab;
import se.alipsa.ride.code.txttab.TxtTab;
import se.alipsa.ride.code.xmltab.XmlTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.utils.CharsetDetector;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class CodeComponent extends BorderPane {

  private ConsoleComponent console;
  private TabPane pane;
  private Ride gui;

  private Logger log = LoggerFactory.getLogger(CodeComponent.class);

  public CodeComponent(Ride gui) {
    this.gui = gui;
    this.console = gui.getConsoleComponent();

    pane = new TabPane();
    setCenter(pane);
    addCodeTab(TabType.R);
  }

  public void addCodeTab(TabType type) {
    final String untitled = "Untitled";
    TextAreaTab tab;
    switch (type) {
      case R:
        tab = createCodeTab(untitled);
        break;
      case TXT:
        tab = new TxtTab(untitled, gui);
        break;
      case JAVA:
        tab = new JavaTab(untitled, gui);
        break;
      case XML:
        tab = new XmlTab(untitled, gui);
        break;
      case SQL:
        tab = new SqlTab(untitled, gui);
        break;
      default:
        throw new RuntimeException("Unknown filetype " + type);
    }
    addTabAndActivate(tab);
  }

  private TextAreaTab createCodeTab(String title) {
    CodeTab codeTab = new CodeTab(title, gui);
    return codeTab;
  }

  private TabTextArea addTabAndActivate(TextAreaTab codeTab) {
    pane.getTabs().add(codeTab);
    SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
    selectionModel.select(codeTab);
    return codeTab;
  }

  public String getActiveScriptName() {
    return getActiveTab().getTitle();

  }

  public String getTextFromActiveTab() {
    TabTextArea ta = getActiveTab();
    return ta.getTextContent();
  }


  public TextAreaTab getActiveTab() {
    SingleSelectionModel selectionModel = pane.getSelectionModel();
    return (TextAreaTab) selectionModel.getSelectedItem();
  }

  public void addTab(File file, TabType type) {
    List<String> lines;
    TextAreaTab tab;
    String title = file.getName();
    switch (type) {
      case R:
        tab = new CodeTab(title, gui);
        break;
      case TXT:
        tab = new TxtTab(title, gui);
        break;
      case XML:
        tab = new XmlTab(title, gui);
        break;
      case JAVA:
        tab = new JavaTab(title, gui);
        break;
      case SQL:
        tab = new SqlTab(title, gui);
        break;
      default:
        tab = new TxtTab(title, gui);
    }
    try {
      //lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
      //String content = String.join("\n", lines);
      byte[] textBytes = FileUtils.readFileToByteArray(file);
      /*
      org.apache.tika.parser.txt.CharsetDetector detector = new CharsetDetector();
      detector.setText(textBytes);

      org.apache.tika.parser.txt.CharsetMatch match = detector.detect();
      log.info("Content detected as charset {}", match.getName());
      String content = detector.getString(textBytes, match.getName());
      */
      Charset charset = CharsetDetector.detect(textBytes);
      log.info("Charset detected as {}", charset);
      String content = new String(textBytes, charset);

      tab.setFile(file);
      tab.replaceContentText(0, 0, content);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read content of file " + file, e);
    }
    addTabAndActivate(tab);
  }

  public void fileSaved(File file) {
    getActiveTab().setTitle(file.getName());
    getActiveTab().setFile(file);
  }
}
