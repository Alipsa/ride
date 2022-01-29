package se.alipsa.ride.environment.connections;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.utils.RQueryBuilder.*;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.renjin.client.datautils.Table;
import se.alipsa.renjin.client.datautils.RDataTransformer;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.rtab.RTextArea;
import se.alipsa.ride.model.TableMetaData;
import se.alipsa.ride.utils.*;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ConnectionsTab extends Tab {

  private static final String NAME_PREF = "ConnectionsTab.name";
  private static final String DRIVER_PREF = "ConnectionsTab.driver";
  private static final String URL_PREF = "ConnectionsTab.url";
  private static final String USER_PREF = "ConnectionsTab.user";
  private static final String CONNECTIONS_PREF = "ConnectionsTab.Connections";
  private final BorderPane contentPane;
  private final Ride gui;
  private final ComboBox<String> name = new ComboBox<>();
  private TextField driverText;
  private TextField urlText;
  private TextField userText;
  private PasswordField passwordField;
  private final TableView<ConnectionInfo> connectionsTable = new TableView<>();

  private final TreeItemComparator treeItemComparator = new TreeItemComparator();

  private static final Logger log = LogManager.getLogger(ConnectionsTab.class);

  public ConnectionsTab(Ride gui) {
    setText("Connections");
    this.gui = gui;
    contentPane = new BorderPane();
    setContent(contentPane);

    VBox inputBox = new VBox();
    HBox topInputPane = new HBox();
    HBox middleInputPane = new HBox();
    HBox bottomInputPane = new HBox();
    inputBox.getChildren().addAll(topInputPane, middleInputPane, bottomInputPane);

    topInputPane.setPadding(FLOWPANE_INSETS);
    topInputPane.setSpacing(2);
    middleInputPane.setPadding(FLOWPANE_INSETS);
    middleInputPane.setSpacing(2);
    contentPane.setTop(inputBox);

    VBox nameBox = new VBox();
    Label nameLabel = new Label("Name:");

    name.getItems().addAll(getSavedConnectionNames());
    String lastUsedName = getPrefOrBlank(NAME_PREF);
    name.setEditable(true);
    if (!"".equals(lastUsedName)) {
      if (name.getItems().contains(lastUsedName)) {
        name.getSelectionModel().select(lastUsedName);
      } else {
        name.getItems().add(lastUsedName);
      }
    }
    name.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (! isNowFocused) {
        name.setValue(name.getEditor().getText());
      }
    });
    name.setPrefWidth(300);
    name.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
      ConnectionInfo ci = getSavedConnection(name.getValue());
      userText.setText(ci.getUser());
      driverText.setText(ci.getDriver());
      urlText.setText(ci.getUrl());
      passwordField.clear();
      passwordField.requestFocus();
    });
    nameBox.getChildren().addAll(nameLabel, name);
    topInputPane.getChildren().add(nameBox);
    HBox.setHgrow(nameBox, Priority.SOMETIMES);

    VBox userBox = new VBox();
    Label userLabel = new Label("User:");
    userText = new TextField(getPrefOrBlank(USER_PREF));
    HBox.setHgrow(userBox, Priority.SOMETIMES);
    userBox.getChildren().addAll(userLabel, userText);
    topInputPane.getChildren().add(userBox);

    VBox passwordBox = new VBox();
    Label passwordLabel = new Label("Password:");
    passwordField = new PasswordField();
    HBox.setHgrow(passwordBox, Priority.SOMETIMES);
    passwordBox.getChildren().addAll(passwordLabel, passwordField);
    topInputPane.getChildren().add(passwordBox);

    VBox driverBox = new VBox();
    Label driverLabel = new Label("Driver:");
    driverText = new TextField(getPrefOrBlank(DRIVER_PREF));
    HBox.setHgrow(driverBox, Priority.SOMETIMES);
    driverBox.getChildren().addAll(driverLabel, driverText);
    middleInputPane.getChildren().add(driverBox);

    VBox urlBox = new VBox();
    Label urlLabel = new Label("Url:");
    urlText = new TextField(getPrefOrBlank(URL_PREF));
    urlBox.getChildren().addAll(urlLabel, urlText);
    HBox.setHgrow(urlBox, Priority.ALWAYS);
    middleInputPane.getChildren().add(urlBox);

    Button newButton = new Button("New");
    newButton.setPadding(new Insets(7, 10, 7, 10));
    newButton.setOnAction(a -> {
      name.setValue("");
      userText.clear();
      passwordField.clear();
      driverText.clear();
      urlText.clear();
    });

    Button deleteButton = new Button("Delete");
    deleteButton.setPadding(new Insets(7, 10, 7, 10));
    deleteButton.setOnAction(a -> {
      String connectionName = name.getValue();
      Preferences pref = gui.getPrefs().node(CONNECTIONS_PREF).node(connectionName);
      try {
        pref.removeNode();
      } catch (BackingStoreException e) {
        ExceptionAlert.showAlert("Failed to remove the connection from preferences", e);
      }
      gui.getCodeComponent().removeConnectionFromTabs(connectionName);
      connectionsTable.getItems().removeIf(c -> c.getName().equals(connectionName));
      name.getItems().remove(connectionName);
      name.setValue("");
      userText.clear();
      passwordField.clear();
      driverText.clear();
      urlText.clear();
      name.requestFocus();
    });

    Button addButton = new Button("Add / Update Connection");
    addButton.setPadding(new Insets(7, 10, 7, 10));
    createConnectionTableView();
    contentPane.setCenter(connectionsTable);
    connectionsTable.setPlaceholder(new Label("No connections defined"));

    addButton.setOnAction(e -> {
      if (name.getValue() == null || name.getValue().isBlank()) {
        return;
      }
      String urlString = urlText.getText().toLowerCase();
      if (urlString.contains("mysql") && !urlString.contains("allowmultiqueries=true")) {
        String msg = "In MySQL you should set allowMultiQueries=true in the connection string to be able to execute multiple queries";
        log.warn(msg);
        Alerts.info("MySQL and multiple query statements", msg);
      }
      ConnectionInfo con = new ConnectionInfo(name.getValue(), driverText.getText(), urlText.getText(), userText.getText(), passwordField.getText());
      addConnection(con);
      saveConnection(con);
      try {
        Connection connection = con.connect();
        connection.close();
        log.info("Connection created successfully, all good!");
      } catch (SQLException ex) {
        Exception exceptionToShow = ex;
        try {
          JdbcUrlParser.validate(driverText.getText(), urlText.getText());
        } catch (MalformedURLException exc) {
          exceptionToShow = exc;
        }
        ExceptionAlert.showAlert("Failed to connect to database: " + exceptionToShow, ex);
      }
    });
    /*VBox buttonBox = new VBox();
    buttonBox.setPadding(new Insets(10, 10, 0, 10));
    buttonBox.setSpacing(VGAP);
    buttonBox.getChildren().add(addButton);
    buttonBox.alignmentProperty().setValue(Pos.BOTTOM_CENTER);*/
    Image wizIMage = new Image("image/wizard.png", ICON_WIDTH, ICON_HEIGHT, true, true);
    ImageView wizImg =  new ImageView(wizIMage);
    Button wizardButton = new Button("Url Wizard", wizImg);
    wizardButton.setOnAction(this::openUrlWizard);
    wizardButton.setTooltip(new Tooltip("create/update the url using the wizard"));
    bottomInputPane.setAlignment(Pos.CENTER);
    Insets btnInsets = new Insets(5, 10, 5, 10);
    wizardButton.setPadding(btnInsets);
    bottomInputPane.setSpacing(10);
    bottomInputPane.getChildren().addAll(newButton, addButton, wizardButton, deleteButton);
  }

  private void addConnection(ConnectionInfo con) {
    log.debug("Add or update connection for {}", con.asJson());
    setPref(NAME_PREF, name.getValue());
    setPref(DRIVER_PREF, driverText.getText());
    setPref(URL_PREF, urlText.getText());
    setPref(USER_PREF, userText.getText());
    ConnectionInfo existing = connectionsTable.getItems().stream()
       .filter(c -> con.getName().equals(c.getName()))
       .findAny().orElse(null);
    if (existing == null) {
      connectionsTable.getItems().add(con);
    } else {
      existing.setUser(con.getUser());
      existing.setPassword(con.getPassword());
      existing.setDriver(con.getDriver());
      existing.setUrl(con.getUrl());

    }
    if (name.getItems().stream().filter(c -> c.equals(con.getName())).findAny().orElse(null) == null) {
      name.getItems().add(con.getName());
      userText.setText(con.getUser());
      passwordField.setText(con.getPassword());
      driverText.setText(con.getDriver());
      urlText.setText(con.getUrl());
    }
    connectionsTable.refresh();
  }

  private void openUrlWizard(ActionEvent actionEvent) {

    JdbcUrlWizardDialog dialog = new JdbcUrlWizardDialog(gui);
    Optional<ConnectionInfo> result = dialog.showAndWait();
    if (!result.isPresent()) {
      return;
    }
    ConnectionInfo ci = result.get();
    driverText.setText(ci.getDriver());
    urlText.setText(ci.getUrl());
  }

  private void createConnectionTableView() {
    TableColumn<ConnectionInfo, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        new PropertyValueFactory<>("name")
    );
    nameCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.2));
    connectionsTable.getColumns().add(nameCol);

    TableColumn<ConnectionInfo, String> driverCol = new TableColumn<>("Driver");
    driverCol.setCellValueFactory(
        new PropertyValueFactory<>("driver")
    );
    driverCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.3));
    connectionsTable.getColumns().add(driverCol);

    TableColumn<ConnectionInfo, String> urlCol = new TableColumn<>("URL");
    urlCol.setCellValueFactory(
        new PropertyValueFactory<>("url")
    );
    urlCol.prefWidthProperty().bind(connectionsTable.widthProperty().multiply(0.5));
    connectionsTable.getColumns().add(urlCol);

    connectionsTable.setRowFactory(tableView -> {
      final TableRow<ConnectionInfo> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem removeMenuItem = new MenuItem("remove connection");
      removeMenuItem.setOnAction(event -> tableView.getItems().remove(row.getItem()));
      final MenuItem deleteMenuItem = new MenuItem("delete connection permanently");
      deleteMenuItem.setOnAction(event -> {
        ConnectionInfo item = row.getItem();
        tableView.getItems().remove(item);
        deleteSavedConnection(item);
        name.getItems().remove(item.getName());
        tableView.refresh();
      });
      final MenuItem viewMenuItem = new MenuItem("view tables");
      viewMenuItem.setOnAction(event -> showConnectionMetaData(row.getItem()));
      final MenuItem viewDatabasesMenuItem = new MenuItem("view databases");
      viewDatabasesMenuItem.setOnAction(event -> showDatabases(row.getItem()));

      final MenuItem viewRcodeMenuItem = new MenuItem("show R connection code");
      viewRcodeMenuItem.setOnAction(event -> showRConnectionCode());

      contextMenu.getItems().addAll(viewMenuItem, viewDatabasesMenuItem, removeMenuItem, deleteMenuItem, viewRcodeMenuItem);
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );

      tableView.getSelectionModel().selectedIndexProperty().addListener(e -> {
        ConnectionInfo info = tableView.getSelectionModel().getSelectedItem();
        if (info != null) {
          name.setValue(info.getName());
          driverText.setText(info.getDriver());
          urlText.setText(info.getUrl());
          userText.setText(info.getUser());
          passwordField.setText(info.getPassword());
        }
      });
      return row;
    });
  }

  private void showRConnectionCode() {
    ConnectionInfo info = connectionsTable.getSelectionModel().getSelectedItem();
    String rCode = createConnectionCode(info);
    displayTextInWindow("R connection code for " + info.getName(), rCode, CodeType.R);
  }

  private String createConnectionCode(ConnectionInfo info) {
    StringBuilder code = RQueryBuilder.baseRQueryString(info, "sqlDf <- dbGetQuery", "select * from someTable")
        .append("\n# close the connection\n")
        .append("dbDisconnect(").append(CONNECTION_VAR_NAME).append(")\n");
    String rCode = code.toString();
    rCode = rCode.replace(DRIVER_VAR_NAME, "drv");
    rCode = rCode.replace(CONNECTION_VAR_NAME, "con");
    return rCode;
  }

  private String getPrefOrBlank(String pref) {
    return gui.getPrefs().get(pref, "");
  }

  private String[] getSavedConnectionNames() {
    try {
      return gui.getPrefs().node(CONNECTIONS_PREF).childrenNames();
    } catch (BackingStoreException e) {
      ExceptionAlert.showAlert("Failed to get saved connections", e);
    }
    return new String[]{};
  }

  private ConnectionInfo getSavedConnection(String name) {
    Preferences pref = gui.getPrefs().node(CONNECTIONS_PREF).node(name);
    ConnectionInfo c = new ConnectionInfo();
    c.setName(name);
    c.setDriver(pref.get(DRIVER_PREF, ""));
    c.setUrl(pref.get(URL_PREF, ""));
    c.setUser(pref.get(USER_PREF, ""));
    return c;
  }

  private void saveConnection(ConnectionInfo c) {
    Preferences pref = gui.getPrefs().node(CONNECTIONS_PREF).node(c.getName());
    pref.put(DRIVER_PREF, c.getDriver());
    pref.put(URL_PREF, c.getUrl());
    if (c.getUser() != null) {
      pref.put(USER_PREF, c.getUser());
    }
  }

  private void deleteSavedConnection(ConnectionInfo c) {
    Preferences pref = gui.getPrefs().node(CONNECTIONS_PREF).node(c.getName());
    try {
      pref.removeNode();
    } catch (BackingStoreException e) {
      ExceptionAlert.showAlert("Failed to remove saved connection", e);
    }
  }

  private void setPref(String pref, String val) {
    gui.getPrefs().put(pref, val);
  }

  public Set<ConnectionInfo> getConnections() {
    return new TreeSet<>(connectionsTable.getItems());
  }

  /**
   * this is consistent for at least postgres, H2, sqlite and SQl server
   */
  private void showConnectionMetaData(ConnectionInfo con) {
    setWaitCursor();
    String sql;
    boolean addNaWhenBlank = false;
    if (con.getDriver().equals(DRV_SQLLITE)) {
      boolean hasTables = false;
      try {
        Connection jdbcCon = con.connect();
        ResultSet rs = jdbcCon.createStatement().executeQuery("select * from sqlite_master");
        if (rs.next()) hasTables = true;
        jdbcCon.close();
      } catch (SQLException e) {
        ExceptionAlert.showAlert("Failed to query sqlite_master", e);
      }
      if (hasTables) {
        sql = "SELECT \n" +
           "  m.name as TABLE_NAME \n" +
           ", m.type as TABLE_TYPE \n" +
           ", p.name as COLUMN_NAME\n" +
           ", p.cid as ORDINAL_POSITION\n" +
           ", case when p.[notnull] = 0 then 1 else 0 end as IS_NULLABLE\n" +
           ", p.type as DATA_TYPE\n" +
           ", 0 as CHARACTER_MAXIMUM_LENGTH\n" +
           ", 0 as NUMERIC_PRECISION\n" +
           ", 0 as NUMERIC_SCALE\n" +
           ", '' as COLLATION_NAME\n" +
           "FROM \n" +
           "  sqlite_master AS m\n" +
           "JOIN \n" +
           "  pragma_table_info(m.name) AS p";
        addNaWhenBlank = false;
      } else {
        setNormalCursor();
        Alerts.info("Empty database", "This sqlite database has no tables yet");
        return;
      }
    } else {
      sql = "select col.TABLE_NAME\n" +
         ", TABLE_TYPE\n" +
         ", COLUMN_NAME\n" +
         ", ORDINAL_POSITION\n" +
         ", IS_NULLABLE\n" +
         ", DATA_TYPE\n" +
         ", CHARACTER_MAXIMUM_LENGTH\n" +
         ", NUMERIC_PRECISION\n" +
         ", NUMERIC_SCALE\n" +
         ", COLLATION_NAME\n" +
         "from INFORMATION_SCHEMA.COLUMNS col\n" +
         "inner join INFORMATION_SCHEMA.TABLES tab " +
         "      on col.TABLE_NAME = tab.TABLE_NAME and col.TABLE_SCHEMA = tab.TABLE_SCHEMA\n" +
         "where TABLE_TYPE <> 'SYSTEM TABLE'\n" +
         "and tab.TABLE_SCHEMA not in ('SYSTEM TABLE', 'PG_CATALOG', 'INFORMATION_SCHEMA', 'pg_catalog', 'information_schema')";
    }
    if (con.getDriver().equals(DRV_SQLSERVER)) {
      addNaWhenBlank = true;
    }
    String rCode = baseRQueryString(con, "connectionsTabDf <- dbGetQuery", sql, addNaWhenBlank).toString();

    // runScriptSilent newer returns so have to run in a thread
    runScriptInThread(rCode, con);
  }

  private void showDatabases(ConnectionInfo connectionInfo) {
    try (Connection con = connectionInfo.connect()) {
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

    double height = text.getLayoutBounds().getHeight() +  fontSize * 2;
    double prefHeight = Math.max(height, 100.0);
    prefHeight = prefHeight > 640  ? 640 : prefHeight;
    ta.setPrefHeight( prefHeight );

    double maxWidth = 0;
    for (String line : content.split("\n")) {
      double length = line.length() * fontSize;
      if (maxWidth < length) {
        maxWidth = length;
      }
    }

    double prefWidth = maxWidth < 150 ? 150 : maxWidth;
    prefWidth = prefWidth > 800 ? 800 : prefWidth;
    ta.setPrefWidth(prefWidth);

    ta.autosize();

    ta.setEditable(false);
    VirtualizedScrollPane<UnStyledCodeArea> scrollPane = new VirtualizedScrollPane<>(ta);
    createAndShowWindow(title, scrollPane);
  }

  void runScriptInThread(String rCode, ConnectionInfo con) {
    String connectionName = con.getName();
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
        ListVector df = (ListVector) gui.getConsoleComponent().fetchVar("connectionsTabDf");
        List<List<Object>> rows = RDataTransformer.toRowlist(df);
        List<TableMetaData> metaDataList = new ArrayList<>();
        rows.forEach(r -> metaDataList.add(new TableMetaData(r)));
        gui.getConsoleComponent().runScriptSilent(cleanupRQueryString().append("rm(connectionsTabDf)").toString());
        setNormalCursor();
        TreeView<String> treeView = createMetaDataTree(metaDataList, con);
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
      log.warn("Exception when running R code {}", rCode);
      ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
    });
    Thread scriptThread = new Thread(task);
    scriptThread.setDaemon(false);
    scriptThread.start();
  }

  private void createAndShowWindow(String title, Parent view) {
    Scene dialog = new Scene(view);
    Stage stage = new Stage();
    stage.initStyle(StageStyle.DECORATED);
    stage.initModality(Modality.NONE);
    stage.setTitle(title);
    stage.setScene(dialog);
    stage.setAlwaysOnTop(true);
    stage.setResizable(true);
    GuiUtils.addStyle(gui, stage);
    stage.show();
    stage.requestFocus();
    stage.toFront();
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

  private TreeView<String> createMetaDataTree(List<TableMetaData> table, ConnectionInfo con) {
    String connectionName = con.getName();
    TreeView<String> tree = new TreeView<>();
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
    tree.setCellFactory(p -> new TableNameTreeCell(con));
    return tree;
  }

  private void copySelectionToClipboard(final TreeView<String> treeView) {
    TreeItem<String> treeItem = treeView.getSelectionModel().getSelectedItem();
    copySelectionToClipboard(treeItem);
  }

  private void copySelectionToClipboard(final TreeItem<String> treeItem) {
    final ClipboardContent clipboardContent = new ClipboardContent();
    String value = treeItem.getValue();
    int idx = value.indexOf(TableMetaData.COLUMN_META_START);
    if (idx > -1) {
      value = value.substring(0, idx);
    }
    clipboardContent.putString(value);
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }

  private static class TreeItemComparator implements Comparator<TreeItem<String>>, Serializable {

    private static final long serialVersionUID = -7997376258097396238L;

    @Override
    public int compare(TreeItem<String> fileTreeItem, TreeItem<String> t1) {
      return fileTreeItem.getValue().compareToIgnoreCase(t1.getValue());
    }
  }

  private final class TableNameTreeCell extends TreeCell<String> {
    private final ContextMenu tableRightClickMenu = new ContextMenu();
    private final ContextMenu columnRightClickMenu = new ContextMenu();

    TableNameTreeCell(ConnectionInfo con) {
      MenuItem copyItem = new MenuItem("copy");
      tableRightClickMenu.getItems().add(copyItem);
      copyItem.setOnAction( event -> copySelectionToClipboard(getTreeItem()) );

      MenuItem copyItem2 = new MenuItem("copy");
      columnRightClickMenu.getItems().add(copyItem2);
      copyItem2.setOnAction( event -> copySelectionToClipboard(getTreeItem()) );

      MenuItem sampleContent = new MenuItem("View 200 rows");
      tableRightClickMenu.getItems().add(sampleContent);
      sampleContent.setOnAction(event -> {
        String tableName = getTreeItem().getValue();
        try (Connection connection = con.connect();
             Statement stm = connection.createStatement()) {
          stm.setMaxRows(200);
          Table table;
          try(ResultSet rs = stm.executeQuery("SELECT * from " + tableName)){
            rs.setFetchSize(200);
            table = new Table(rs);
          }
          gui.getInoutComponent().showInViewer(table, tableName);

        } catch (SQLException e) {
          ExceptionAlert.showAlert("Failed to sample table", e);
        }
      });
    }


    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setText(null);
        setGraphic(null);
      } else {
        setText(item);
        setGraphic(getTreeItem().getGraphic());
        if ( (!getTreeItem().isLeaf()) && (getTreeItem().getParent() != null) ) {
          setContextMenu(tableRightClickMenu);
        } else if (getTreeItem().isLeaf()) {
          setContextMenu(columnRightClickMenu);
        }
      }
    }
  }
}
