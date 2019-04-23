package se.alipsa.ride.environment.connections;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.utils.RQueryBuilder.baseRQueryString;
import static se.alipsa.ride.utils.RQueryBuilder.cleanupRQueryString;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.ListVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.rtab.RTextArea;
import se.alipsa.ride.model.TableMetaData;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.RDataTransformer;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectionsTab extends Tab {

  private static final String DRIVER_PREF = "ConnectionsTab.driver";
  private static final String URL_PREF = "ConnectionsTab.url";
  private static final String USER_PREF = "ConnectionsTab.user";
  private BorderPane contentPane;
  private Ride gui;
  private TextField nameText;
  private TextField driverText;
  private TextField urlText;
  private TextField userText;
  private PasswordField passwordField;
  private TableView<ConnectionInfo> connectionsTable = new TableView<>();

  private TreeItemComparator treeItemComparator = new TreeItemComparator();

  private Logger log = LoggerFactory.getLogger(ConnectionsTab.class);

  public ConnectionsTab(Ride gui) {
    setText("Connections");
    this.gui = gui;
    contentPane = new BorderPane();
    setContent(contentPane);

    VBox inputBox = new VBox();
    HBox topInputPane = new HBox();
    HBox bottomInputPane = new HBox();
    inputBox.getChildren().addAll(topInputPane, bottomInputPane);

    topInputPane.setPadding(FLOWPANE_INSETS);
    topInputPane.setSpacing(2);
    bottomInputPane.setPadding(FLOWPANE_INSETS);
    bottomInputPane.setSpacing(2);
    //inputPane.setVgap(VGAP);
    //inputPane.setHgap(HGAP);
    contentPane.setTop(inputBox);

    VBox nameBox = new VBox();
    Label nameLabel = new Label("Name:");
    //topInputPane.getChildren().add(nameLabel);
    nameText = new TextField();
    nameText.setPrefWidth(80);
    nameBox.getChildren().addAll(nameLabel, nameText);
    topInputPane.getChildren().add(nameBox);
    topInputPane.setHgrow(nameBox, Priority.SOMETIMES);

    VBox userBox = new VBox();
    Label userLabel = new Label("User:");
    //topInputPane.getChildren().add(userLabel);
    userText = new TextField(getPrefOrBlank(USER_PREF));
    //topInputPane.getChildren().add(userText);
    topInputPane.setHgrow(userBox, Priority.SOMETIMES);
    userBox.getChildren().addAll(userLabel, userText);
    topInputPane.getChildren().add(userBox);

    VBox passwordBox = new VBox();
    Label passwordLabel = new Label("Password:");
    //topInputPane.getChildren().add(passwordLabel);
    passwordField = new PasswordField();
    //topInputPane.getChildren().add(passwordField);
    topInputPane.setHgrow(passwordBox, Priority.SOMETIMES);
    passwordBox.getChildren().addAll(passwordLabel, passwordField);
    topInputPane.getChildren().add(passwordBox);

    VBox driverBox = new VBox();
    Label driverLabel = new Label("Driver:");
    //bottomInputPane.getChildren().add(driverLabel);
    driverText = new TextField(getPrefOrBlank(DRIVER_PREF));
    bottomInputPane.setHgrow(driverBox, Priority.SOMETIMES);
    driverBox.getChildren().addAll(driverLabel, driverText);
    bottomInputPane.getChildren().add(driverBox);

    VBox urlBox = new VBox();
    Label urlLabel = new Label("Url:");
    //bottomInputPane.getChildren().add(urlLabel);
    urlText = new TextField(getPrefOrBlank(URL_PREF));
    urlBox.getChildren().addAll(urlLabel, urlText);
    bottomInputPane.setHgrow(urlBox, Priority.ALWAYS);
    bottomInputPane.getChildren().add(urlBox);

    Button addButton = new Button("Add");
    createConnectionTableView();
    contentPane.setCenter(connectionsTable);

    addButton.setOnAction(e -> {
      ConnectionInfo con = new ConnectionInfo(nameText.getText(), driverText.getText(), urlText.getText(), userText.getText(), passwordField.getText());
      //connections.add(con);
      setPref(DRIVER_PREF, driverText.getText());
      setPref(URL_PREF, urlText.getText());
      setPref(USER_PREF, userText.getText());
      if (!connectionsTable.getItems().contains(con)) {
        connectionsTable.getItems().add(con);
        connectionsTable.getSelectionModel().select(con);
      }
    });
    VBox buttonBox = new VBox();
    buttonBox.setPadding(new Insets(10, 10, 0, 10));
    buttonBox.setSpacing(VGAP);
    buttonBox.getChildren().add(addButton);
    buttonBox.alignmentProperty().setValue(Pos.BOTTOM_CENTER);
    topInputPane.getChildren().add(buttonBox);
  }

  private TableView<ConnectionInfo> createConnectionTableView() {
    TableColumn<ConnectionInfo, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        new PropertyValueFactory<>("name")
    );
    nameCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.2));

    TableColumn<ConnectionInfo, String> driverCol = new TableColumn<>("Driver");
    driverCol.setCellValueFactory(
        new PropertyValueFactory<>("driver")
    );
    driverCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.3));

    TableColumn<ConnectionInfo, String> urlCol = new TableColumn<>("URL");
    urlCol.setCellValueFactory(
        new PropertyValueFactory<>("url")
    );
    urlCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.5));

    connectionsTable.getColumns().addAll(nameCol, driverCol, urlCol);
    connectionsTable.setRowFactory(tableView -> {
      final TableRow<ConnectionInfo> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem removeMenuItem = new MenuItem("delete connection");
      removeMenuItem.setOnAction(event -> {
        connectionsTable.getItems().remove(row.getItem());
      });
      final MenuItem viewMenuItem = new MenuItem("view tables");
      viewMenuItem.setOnAction(event -> {
        showConnectionMetaData(row.getItem());
      });
      final MenuItem viewDatabasesMenuItem = new MenuItem("view databases");
      viewDatabasesMenuItem.setOnAction(event -> {
        showDatabases(row.getItem());
      });

      final MenuItem viewRcodeMenuItem = new MenuItem("show R connection code");
      viewRcodeMenuItem.setOnAction(event -> {
        showRConnectionCode();
      });

      contextMenu.getItems().addAll(viewMenuItem, viewDatabasesMenuItem, removeMenuItem, viewRcodeMenuItem);
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );

      connectionsTable.getSelectionModel().selectedIndexProperty().addListener(e -> {
        ConnectionInfo info = connectionsTable.getSelectionModel().getSelectedItem();
        nameText.setText(info.getName());
        driverText.setText(info.getDriver());
        urlText.setText(info.getUrl());
        userText.setText(info.getUser());
        passwordField.setText(info.getPassword());
      });
      return row;
    });
    return connectionsTable;
  }

  private void showRConnectionCode() {
    ConnectionInfo info = connectionsTable.getSelectionModel().getSelectedItem();
    StringBuilder code = new StringBuilder();
    String con = info.getName() + "_con";
    code.append("library(\"org.renjin.cran:DBI\")\n")
        .append("library(\"se.alipsa:R2JDBC\")\n\n")
        .append(con).append(" <- dbConnect(\n")
        .append("  JDBC(\"").append(info.getDriver()).append("\")\n")
        .append("  ,url = \"").append(info.getUrl()).append("\"\n");
    if (!"".equals(userText.getText().trim())) {
      code.append("  ,user = \"").append(info.getUser()).append("\"\n");
    }
    if (!"".equals(passwordField.getText().trim())) {
      code.append("  ,password = \"").append(info.getPassword()).append("\"\n");
    }
    code.append(")\n\n")
        .append("# execute some queries...\n")
        .append("sqlDf <- dbGetQuery(").append(con).append(", \"select * from someTable\")\n")
        .append("# close the connection\n")
        .append("dbDisconnect(").append(con).append(")\n");
    displayTextInWindow("R connection code for " + info.getName(), code.toString(), CodeType.R);
  }

  private String getPrefOrBlank(String pref) {
    return gui.getPrefs().get(pref, "");
  }

  private void setPref(String pref, String val) {
    gui.getPrefs().put(pref, val);
  }

  public Set<ConnectionInfo> getConnections() {
    return new TreeSet<>(connectionsTable.getItems());
  }

  /**
   * this is consistent for at least H2 and SQl server
   */
  private void showConnectionMetaData(ConnectionInfo con) {
    setWaitCursor();
    String sql = "select col.TABLE_NAME\n" +
        ", TABLE_TYPE\n" +
        ", COLUMN_NAME\n" +
        ", ORDINAL_POSITION\n" +
        ", IS_NULLABLE\n" +
        ", DATA_TYPE\n" +
        ", CHARACTER_MAXIMUM_LENGTH\n" +
        ", NUMERIC_PRECISION\n" +
        ", NUMERIC_PRECISION_RADIX\n" +
        ", NUMERIC_SCALE\n" +
        ", COLLATION_NAME\n" +
        "from INFORMATION_SCHEMA.COLUMNS col\n" +
        "inner join INFORMATION_SCHEMA.TABLES tab " +
        "      on col.TABLE_NAME = tab.TABLE_NAME and col.TABLE_SCHEMA = tab.TABLE_SCHEMA\n" +
        "where TABLE_TYPE <> 'SYSTEM TABLE'\n" +
        "and tab.TABLE_SCHEMA not in ('SYSTEM TABLE', 'PG_CATALOG', 'INFORMATION_SCHEMA', 'pg_catalog', 'information_schema')";
    String rCode = baseRQueryString(con, "connectionsTabDf <- dbGetQuery", sql).toString();

    // runScriptSilent newer returns so have to run in a thread
    runScriptInThread(rCode, "connectionsTabDf", con.getName());
  }

  private void showDatabases(ConnectionInfo connectionInfo) {
    try (Connection con = DriverManager.getConnection(connectionInfo.getUrl(), connectionInfo.getUser(), connectionInfo.getPassword())) {
      DatabaseMetaData meta = con.getMetaData();
      ResultSet res = meta.getCatalogs();
      List<String> dbList = new ArrayList<>();
      while (res.next()) {
        dbList.add(res.getString("TABLE_CAT"));
      }
      res.close();
      String content = String.join("\n", dbList);
      String title = "Databases for connection " + connectionInfo.getName();

      displayTextInWindow(title, content, CodeType.TXT);
    } catch (SQLException e) {
      String msg = gui.getConsoleComponent().createMessageFromEvalException(e);
      ExceptionAlert.showAlert(msg + e.getMessage(), e);
    }
  }

  private void displayTextInWindow(String title, String content, CodeType codeType) {
    UnStyledCodeArea ta;
    Text text = new Text(content);
    if (CodeType.R.equals(codeType)) {
      ta = new RTextArea();
      text.setStyle("-fx-font-family: monospace;");
    } else {
      ta = new UnStyledCodeArea();
      ta.setStyle("-fx-font-family:" + Font.getDefault().getFamily());
      text.setStyle("-fx-font-family:" + Font.getDefault().getFamily());
    }

    ta.getStyleClass().add("txtarea");
    ta.setWrapText(false);
    ta.replaceText(content);

    //TODO: get the fontsize from the ta instead of the below!
    double fontSize = 8.5; // ta.getFont().getSize() does not exist

    //log.info("FontSize = {}", fontSize);

    double height = text.getLayoutBounds().getHeight() +  fontSize * 2;
    double prefHeight = height < 100.0 ? 100.0 : height;
    prefHeight = prefHeight > 640  ? 640 : prefHeight;
    ta.setPrefHeight( prefHeight );

    double maxWidth = 0;
    for (String line : content.split("\n")) {
      double length = line.length() * fontSize;
      if (maxWidth < length) {
        maxWidth = length;
      }
    }
    //log.info("maxWidth = {}", maxWidth);
    double prefWidth = maxWidth < 150 ? 150 : maxWidth;
    prefWidth = prefWidth > 800 ? 800 : prefWidth;
    ta.setPrefWidth(prefWidth);

    //log.info("PrefHeight = {}, PrefWidth = {}", ta.getPrefHeight(), ta.getPrefWidth());
    ta.autosize();

    ta.setEditable(false);
    VirtualizedScrollPane<UnStyledCodeArea> scrollPane = new VirtualizedScrollPane<>(ta);
    createAndShowWindow(title, scrollPane);
  }

  void runScriptInThread(String rCode, String varname, String connectionName) {
    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
          RenjinScriptEngine engine = factory.getScriptEngine(gui.getConsoleComponent().getSession());
          engine.eval(rCode);
        } catch (RuntimeException e) {
          // RuntimeExceptions (such as EvalExceptions is not caught so need to wrap all in an exception
          // this way we can get to the original one by extracting the cause from the thrown exception
          throw new Exception(e);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> {
      try {
        ListVector df = (ListVector) gui.getConsoleComponent().fetchVar(varname);
        List<List<Object>> rows = RDataTransformer.toRowlist(df);
        List<TableMetaData> metaDataList = new ArrayList<>();
        rows.forEach(r -> metaDataList.add(new TableMetaData(r)));
        gui.getConsoleComponent().runScriptSilent(cleanupRQueryString().append("rm(connectionsTabDf)").toString());
        setNormalCursor();
        TreeView treeView = createMetaDataTree(metaDataList, connectionName);
        createAndShowWindow(connectionName + " connection view", treeView);
      } catch (Exception ex) {
        setNormalCursor();
        ExceptionAlert.showAlert("Failed to create connection tree view", ex);
      }
    });

    task.setOnFailed(e -> {
      setNormalCursor();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      String msg = gui.getConsoleComponent().createMessageFromEvalException(ex);
      ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
    });
    Thread scriptThread = new Thread(task);
    scriptThread.setDaemon(false);
    scriptThread.start();
  }

  private void createAndShowWindow(String title, Parent view) {
    Scene dialog = new Scene(view);
    dialog.getStylesheets().addAll(gui.getStyleSheets());
    Stage stage = new Stage();
    stage.initStyle(StageStyle.DECORATED);
    stage.initModality(Modality.NONE);
    stage.setTitle(title);
    stage.setScene(dialog);
    stage.setAlwaysOnTop(false);
    stage.setResizable(true);
    stage.show();
    stage.toFront();
    stage.requestFocus();
  }

  private void setNormalCursor() {
    gui.setNormalCursor();
    contentPane.setCursor(Cursor.DEFAULT);
    connectionsTable.setCursor(Cursor.DEFAULT);
  }

  private void setWaitCursor() {
    gui.setWaitCursor();
    contentPane.setCursor(Cursor.WAIT);
    connectionsTable.setCursor(Cursor.WAIT);
  }

  private TreeView createMetaDataTree(List<TableMetaData> table, String connectionName) {
    TreeView tree = new TreeView();
    TreeItem<String> root = new TreeItem<>(connectionName);
    tree.setRoot(root);
    Map<String, List<TableMetaData>> tableMap = table.stream()
        .collect(Collectors.groupingBy(TableMetaData::getTableName));
    tableMap.forEach((k, v) -> {
      TreeItem<String> tableName = new TreeItem<>(k);
      root.getChildren().add(tableName);
      v.forEach(c -> {
        TreeItem<String> column = new TreeItem<>(c.asColumnString());
        tableName.getChildren().add(column);
      });
      tableName.getChildren().sort(treeItemComparator);
    });
    root.getChildren().sort(treeItemComparator);
    root.setExpanded(true);
    tree.setOnKeyPressed(event -> {
      if (KEY_CODE_COPY.match(event)) {
        copySelectionToClipboard(tree);
      }
    });
    return tree;
  }

  @SuppressWarnings("rawtypes")
  private void copySelectionToClipboard(final TreeView<?> treeView) {
    TreeItem treeItem = treeView.getSelectionModel().getSelectedItem();
    final ClipboardContent clipboardContent = new ClipboardContent();
    String value = treeItem.getValue().toString();
    int idx = value.indexOf(TableMetaData.COLUMN_META_START);
    if (idx > -1) {
      value = value.substring(0, idx);
    }
    clipboardContent.putString(value);
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }

  private class TreeItemComparator implements Comparator<TreeItem<String>> {

    @Override
    public int compare(TreeItem<String> fileTreeItem, TreeItem<String> t1) {
      return fileTreeItem.getValue().compareToIgnoreCase(t1.getValue());
    }
  }
}
