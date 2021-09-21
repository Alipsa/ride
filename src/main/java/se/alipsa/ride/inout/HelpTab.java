package se.alipsa.ride.inout;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.utils.FileUtils;

public class HelpTab extends Tab {

  private final TabPane helpPane;

  private static final Logger log = LogManager.getLogger();

  public HelpTab() {
    setText("Viewer");
    helpPane = new TabPane();
    setContent(helpPane);
    helpPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
  }

  public void display(String url, String... title) {
    if (url == null) {
      log.warn("url is null, nothing to view");
      return;
    }
    Tab tab = new Tab();
    if (title.length > 0) {
      tab.setText(title[0]);
    } else {
      tab.setText(FileUtils.baseName(url));
    }
    tab.setTooltip(new Tooltip(url));
    helpPane.getTabs().add(tab);
    WebView browser = new WebView();
    browser.setContextMenuEnabled(false);
    WebEngine webEngine = browser.getEngine();

    log.info("Opening {} in view tab", url);
    webEngine.load(url);

    tab.setContent(browser);
    helpPane.getSelectionModel().select(tab);
  }
}
