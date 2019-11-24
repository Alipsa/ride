package se.alipsa.ride.code;

import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.javatab.JavaTab;
import se.alipsa.ride.code.mdtab.MdTab;
import se.alipsa.ride.code.rtab.RTab;
import se.alipsa.ride.code.sqltab.SqlTab;
import se.alipsa.ride.code.txttab.TxtTab;
import se.alipsa.ride.code.xmltab.XmlTab;
import se.alipsa.ride.utils.CharsetToolkit;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CodeComponent extends BorderPane {

  private TabPane pane;
  private Ride gui;

  private Logger log = LogManager.getLogger(CodeComponent.class);

  public CodeComponent(Ride gui) {
    this.gui = gui;

    pane = new TabPane();
    setCenter(pane);
    addCodeTab(CodeType.R);
  }

  public void addCodeTab(CodeType type) {
    //final String untitled = "Untitled";
    TextAreaTab tab;
    switch (type) {
      case R:
        tab = new RTab(type.getDisplayValue(), gui);
        break;
      case TXT:
        tab = new TxtTab(type.getDisplayValue(), gui);
        break;
      case JAVA:
        tab = new JavaTab(type.getDisplayValue(), gui);
        break;
      case XML:
        tab = new XmlTab(type.getDisplayValue(), gui);
        break;
      case SQL:
        tab = new SqlTab(type.getDisplayValue(), gui);
        break;
      case MD:
        tab = new MdTab(type.getDisplayValue(), gui);
        break;
      default:
        throw new RuntimeException("Unknown filetype " + type);
    }
    addTabAndActivate(tab);
  }

  private TextAreaTab addTabAndActivate(TextAreaTab codeTab) {
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
    SingleSelectionModel<Tab> selectionModel = pane.getSelectionModel();
    return (TextAreaTab) selectionModel.getSelectedItem();
  }

  public TextAreaTab addTab(File file, CodeType type) {
    TextAreaTab tab;
    String title = file.getName();
    switch (type) {
      case R:
        tab = new RTab(title, gui);
        break;
      case TXT:
        tab = new TxtTab(title, gui);
        break;
      case MD:
        tab = new MdTab(title, gui);
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
      byte[] textBytes = FileUtils.readFileToByteArray(file);
      String content = "";
      if (textBytes.length != 0) {
        CharsetToolkit toolkit = new CharsetToolkit(textBytes);
        toolkit.setEnforce8Bit(true);
        Charset charset = toolkit.guessEncoding();
        log.debug("Charset for {} detected as {}", file.getName(), charset);
        content = new String(textBytes, charset);
      }

      tab.setFile(file);
      tab.replaceContentText(0, 0, content);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to read content of file " + file, e);
    }
    return addTabAndActivate(tab);
  }

  public void fileSaved(File file) {
    getActiveTab().setTitle(file.getName());
    getActiveTab().setFile(file);
  }

  public boolean hasUnsavedFiles() {
    for (Tab tab : pane.getTabs()) {
      TextAreaTab taTab = (TextAreaTab) tab;
      if (taTab.isChanged()) {
        return true;
      }
    }
    return false;
  }

  public TextAreaTab getTab(File file) {
    for (Tab tab : pane.getTabs()) {
      TextAreaTab textAreaTab = (TextAreaTab)tab;
      if (file.equals(textAreaTab.getFile())) {
        return textAreaTab;
      }
    }
    return null;
  }

  public void activateTab(TextAreaTab tab) {
    pane.getSelectionModel().select(tab);
  }
}
