package se.alipsa.ride.inout.viewer;

import static se.alipsa.ride.Constants.KEY_CODE_COPY;
import static se.alipsa.ride.code.mdrtab.MdrUtil.*;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import se.alipsa.renjin.client.datautils.Table;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.code.xmltab.XmlTextArea;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.GuiUtils;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ViewTab extends Tab {

  private static final Logger log = LogManager.getLogger();

  private final TabPane viewPane;

  public ViewTab() {
    setText("Viewer");
    viewPane = new TabPane();
    setContent(viewPane);
    viewPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
  }

  public void viewTable(Table table, String... title) {
    try {
      List<String> headerList = table.getHeaderList();
      List<List<Object>> rowList = table.getRowList();
      NumberFormat numberFormatter = NumberFormat.getInstance();
      numberFormatter.setGroupingUsed(false);

      TableView<List<String>> tableView = new TableView<>();
      tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      tableView.setOnKeyPressed(event -> {
        if (KEY_CODE_COPY.match(event)) {
          // Include header if all rows are selected
          boolean includeHeader = tableView.getSelectionModel().getSelectedCells().size() == rowList.size();
          if (includeHeader) {
            copySelectionToClipboard(tableView, headerList);
          } else {
            copySelectionToClipboard(tableView, null);
          }
        }
      });

      tableView.setRowFactory(tv -> {
        final TableRow<List<String>> row = new TableRow<>();
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem copyMenuItem = new MenuItem("copy");
        copyMenuItem.setOnAction(event -> copySelectionToClipboard(tv, null));
        final MenuItem copyWithHeaderMenuItem = new MenuItem("copy with header");
        copyWithHeaderMenuItem.setOnAction(event -> copySelectionToClipboard(tv, headerList));
        final MenuItem exportToCsvMenuItem = new MenuItem("export to csv");
        exportToCsvMenuItem.setOnAction(event -> exportToCsv(tv, headerList, title));

        contextMenu.getItems().addAll(copyMenuItem, copyWithHeaderMenuItem, exportToCsvMenuItem);
        row.contextMenuProperty().bind(
            Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
        );
        return row;
      });

      for (int i = 0; i < headerList.size(); i++) {
        final int j = i;
        String colName = headerList.get(i);
        TableColumn<List<String>, String> col = new TableColumn<>();

        Label colLabel = new Label(colName);
        colLabel.setTooltip(new Tooltip(table.getColumnType(i).getRtypeName()));
        col.setGraphic(colLabel);
        col.setPrefWidth(new Text(colName).getLayoutBounds().getWidth() * 1.25 + 12.0);

        tableView.getColumns().add(col);
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
      }
      ObservableList<List<String>> data = FXCollections.observableArrayList();
      for (List<Object> row : rowList) {
        List<String> obsRow = new ArrayList<>();
        for (Object obj : row) {
          if (obj instanceof Number) {
            obsRow.add(numberFormatter.format(obj));
          } else {
            obsRow.add(obj + "");
          }
        }
        data.add(obsRow);
      }
      tableView.setItems(data);
      Tab tab = new Tab();
      String tabTitle = " (" + rowList.size() + " rows)";
      if (title.length > 0) {
        tabTitle = title[0] + tabTitle;
      }
      tab.setText(tabTitle);
      tab.setContent(tableView);
      viewPane.getTabs().add(tab);
      SingleSelectionModel<Tab> selectionModel = viewPane.getSelectionModel();
      selectionModel.select(tab);
    } catch (RuntimeException e) {
      ExceptionAlert.showAlert("Failed to view table", e);
    }
  }


  @SuppressWarnings("rawtypes")
  private void copySelectionToClipboard(final TableView<?> table, List<String> headerList) {
    final Set<Integer> rows = new TreeSet<>();
    for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
      rows.add(tablePosition.getRow());
    }
    final StringBuilder strb = new StringBuilder();
    if (headerList != null) {
      strb.append(String.join("\t", headerList)).append("\n");
    }
    boolean firstRow = true;
    for (final Integer row : rows) {
      if (!firstRow) {
        strb.append('\n');
      }
      firstRow = false;
      boolean firstCol = true;
      for (final TableColumn<?, ?> column : table.getColumns()) {
        if (!firstCol) {
          strb.append('\t');
        }
        firstCol = false;
        final Object cellData = column.getCellData(row);
        strb.append(cellData == null ? "" : cellData.toString());
      }
    }
    final ClipboardContent clipboardContent = new ClipboardContent();
    clipboardContent.putString(strb.toString());
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }

  private void exportToCsv(final TableView<?> table, List<String> headerList, String... title) {
    final Set<Integer> rows = new TreeSet<>();
    for (final TablePosition<?, ?> tablePosition : table.getSelectionModel().getSelectedCells()) {
      rows.add(tablePosition.getRow());
    }
    try {
      StringWriter sw = new StringWriter();
      CSVFormat format = CSVFormat.DEFAULT.builder()
          .setHeader(headerList.toArray(new String[0]))
          .build();
      CSVPrinter prn = new CSVPrinter(sw, format);
      List<String> rowValues = new ArrayList<>(headerList.size());
      for (final Integer row : rows) {
        for (final TableColumn<?, ?> column : table.getColumns()) {
          final Object cellData = column.getCellData(row);
          rowValues.add(cellData == null ? null : String.valueOf(cellData).trim());
        }
        prn.printRecord(rowValues);
        rowValues.clear();
      }
      FileChooser fc = new FileChooser();
      fc.setTitle("Save CSV File");
      String initialFileName = (title.length == 0 ? "rideExport" : title[0])
          .replace("*", "").replace(" ", "");
      if (initialFileName.endsWith(".")) {
        initialFileName = initialFileName + "csv";
      } else {
        initialFileName = initialFileName + ".csv";
      }
      Ride gui = Ride.instance();
      String dir = gui.getPrefs().get(Constants.PREF_LAST_EXPORT_DIR,gui.getInoutComponent().getRootDir().getAbsolutePath());
      fc.setInitialDirectory(new File(dir));
      fc.setInitialFileName(initialFileName);
      fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));
      File outFile = fc.showSaveDialog(gui.getStage());
      if (outFile == null ) {
        // Clicking cancel and still have an action performed is not very good UX
        // TODO: Consider changing this to an explicit action (export csv -> to clipboard) instead
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(sw.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
        Alerts.info("Export to CSV", "File export cancelled, CSV copied to clipboard!");
      } else {
        FileUtils.writeToFile(outFile, sw.toString());
        gui.getPrefs().put(Constants.PREF_LAST_EXPORT_DIR, outFile.getCanonicalFile().getParentFile().getAbsolutePath());
      }
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to create csv", e);
    }
  }

  public void viewHtmlWithBootstrap(String content, String... title) {
    Tab tab = new Tab();
    if (title.length > 0) {
      tab.setText(title[0]);
    }
    viewPane.getTabs().add(tab);
    WebView browser = new WebView();
    browser.setContextMenuEnabled(false);

    WebEngine webEngine = browser.getEngine();
    String html = decorate(content, true, false);
    webEngine.loadContent(html);
    createContextMenu(browser, html);
    tab.setContent(browser);
    viewPane.getSelectionModel().select(tab);
  }

  private void createContextMenu(WebView browser, String content, boolean... useLoadOpt) {
    boolean useLoad = useLoadOpt.length > 0 && useLoadOpt[0];
    ContextMenu contextMenu = new ContextMenu();
    WebEngine webEngine = browser.getEngine();

    MenuItem reloadMI = new MenuItem("Reload");
    reloadMI.setOnAction(e -> webEngine.reload());

    MenuItem originalPageMI = new MenuItem("Original page");
    // history only updates for external urls, so we add original back as a fallback
    // e.g when going from a local file to an external link
    originalPageMI.setOnAction(e -> {
      if (useLoad) {
        webEngine.load(content);
      } else {
        webEngine.loadContent(content);
      }
    });

    MenuItem goBackMI = new MenuItem("Go back");
    goBackMI.setOnAction(e -> goBack(webEngine));

    MenuItem goForwardMI = new MenuItem("Go forward");
    goForwardMI.setOnAction(a -> goForward(webEngine));

    MenuItem viewSourceMI = new MenuItem("View source");

    viewSourceMI.setOnAction(a -> viewSource(webEngine, null));

    contextMenu.getItems().addAll(reloadMI, originalPageMI, goBackMI, goForwardMI, viewSourceMI);
    browser.setOnMousePressed(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        contextMenu.show(browser, e.getScreenX(), e.getScreenY());
      } else {
        contextMenu.hide();
      }
    });
  }

  public static void viewSource(WebEngine webEngine, TextAreaTab parent) {
    Document doc = webEngine.getDocument();
    try (StringWriter writer = new StringWriter()){
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(webEngine.getTitle());
      alert.setHeaderText(null);
      XmlTextArea xmlTextArea = new XmlTextArea(parent);
      xmlTextArea.replaceContentText(0,0, writer.toString());
      alert.getDialogPane().setContent(xmlTextArea);
      alert.setResizable(true);
      alert.getDialogPane().setPrefSize(800, 600);
      GuiUtils.addStyle(Ride.instance(), alert);
      alert.showAndWait();
      //Alerts.info(webEngine.getTitle(), writer.toString());
    } catch (TransformerException | IOException e) {
      ExceptionAlert.showAlert("Failed to read DOM", e);
    }
  }

  private void goBack(WebEngine webEngine) {
    final WebHistory history = webEngine.getHistory();
    ObservableList<WebHistory.Entry> entryList = history.getEntries();
    int currentIndex = history.getCurrentIndex();
    int backOffset= entryList.size() > 1 && currentIndex > 0 ? -1 : 0;
    history.go(backOffset);
  }

  private void goForward(WebEngine webEngine) {
    final WebHistory history = webEngine.getHistory();
    ObservableList<WebHistory.Entry> entryList = history.getEntries();
    int currentIndex = history.getCurrentIndex();
    history.go(entryList.size() > 1 && currentIndex < entryList.size() - 1 ? 1 : 0);
  }

  public void viewHtml(String content, String... title) {
    Tab tab = new Tab();
    if (title.length > 0) {
      tab.setText(title[0]);
    }
    viewPane.getTabs().add(tab);
    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();
    webEngine.loadContent(content);
    tab.setContent(browser);
    browser.setContextMenuEnabled(false);
    createContextMenu(browser, content);
    viewPane.getSelectionModel().select(tab);
  }

  public void viewer(String url, String... title) {
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
    viewPane.getTabs().add(tab);
    WebView browser = new WebView();
    browser.setContextMenuEnabled(false);
    WebEngine webEngine = browser.getEngine();
    if (url.startsWith("http")) {
      log.info("Opening {} in view tab", url);
      webEngine.load(url);
      createContextMenu(browser, url, true);
    } else {
      try {
        if (Paths.get(url).toFile().exists()) {
          String path = Paths.get(url).toUri().toURL().toExternalForm();
          log.info("Opening {} in view tab", path);
          webEngine.load(path);
          createContextMenu(browser, path, true);
        } else {
          log.info("url {} is not a http url nor a local path, assuming it is content...", url);
          webEngine.loadContent(url);
          createContextMenu(browser, url);
        }
      } catch (MalformedURLException e) {
        ExceptionAlert.showAlert("Failed to transform the path to an URL", e);
      }
    }
    tab.setContent(browser);
    viewPane.getSelectionModel().select(tab);
  }
}
