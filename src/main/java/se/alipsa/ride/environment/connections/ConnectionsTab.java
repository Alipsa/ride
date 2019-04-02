package se.alipsa.ride.environment.connections;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.ListVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.TableMetaData;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.RDataTransformer;

import java.util.*;
import java.util.stream.Collectors;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.utils.RQueryBuilder.baseRQueryString;
import static se.alipsa.ride.utils.RQueryBuilder.cleanupRQueryString;

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
    topInputPane.setSpacing(HGAP);
    bottomInputPane.setPadding(FLOWPANE_INSETS);
    bottomInputPane.setSpacing(HGAP);
    //inputPane.setVgap(VGAP);
    //inputPane.setHgap(HGAP);
    contentPane.setTop(inputBox);

    Label nameLabel = new Label("Name:");
    topInputPane.getChildren().add(nameLabel);
    nameText = new TextField();
    nameText.setPrefWidth(80);
    topInputPane.getChildren().add(nameText);

    Label userLabel = new Label("User:");
    topInputPane.getChildren().add(userLabel);
    userText = new TextField(getPrefOrBlank(USER_PREF));
    topInputPane.getChildren().add(userText);

    Label passwordLabel = new Label("Password:");
    topInputPane.getChildren().add(passwordLabel);
    passwordField = new PasswordField();
    topInputPane.getChildren().add(passwordField);

    Label driverLabel = new Label("Driver:");
    bottomInputPane.getChildren().add(driverLabel);
    driverText = new TextField(getPrefOrBlank(DRIVER_PREF));
    bottomInputPane.getChildren().add(driverText);

    Label urlLabel = new Label("Url:");
    bottomInputPane.getChildren().add(urlLabel);
    urlText = new TextField(getPrefOrBlank(URL_PREF));
    bottomInputPane.getChildren().add(urlText);
    bottomInputPane.setHgrow(urlText, Priority.ALWAYS);

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
      }
    });
    topInputPane.getChildren().add(addButton);
  }

  private TableView<ConnectionInfo> createConnectionTableView() {
    TableColumn<ConnectionInfo, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        new PropertyValueFactory<>("name")
    );
    TableColumn<ConnectionInfo, String> driverCol = new TableColumn<>("Driver");
    driverCol.setCellValueFactory(
        new PropertyValueFactory<>("driver")
    );
    TableColumn<ConnectionInfo, String> urlCol = new TableColumn<>("URL");
    urlCol.setCellValueFactory(
        new PropertyValueFactory<>("url")
    );

    connectionsTable.getColumns().addAll(nameCol, driverCol, urlCol);
    connectionsTable.setRowFactory(tableView -> {
      final TableRow<ConnectionInfo> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem removeMenuItem = new MenuItem("delete connection");
      removeMenuItem.setOnAction(event -> {
        connectionsTable.getItems().remove(row.getItem());
      });
      final MenuItem viewMenuItem = new MenuItem("view connection");
      viewMenuItem.setOnAction(event -> {
        showConnectionMetaData(row.getItem());
        //Alerts.info("Not yet implemented","Viewing connection meta data is not yet implemented");
      });
      contextMenu.getItems().addAll(viewMenuItem, removeMenuItem);
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );
      return row;
    });
    return connectionsTable;
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
        System.out.println("creating treeview");
        TreeView treeView = createMetaDataTree(metaDataList, connectionName);
        Scene dialog = new Scene(treeView);
        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initOwner(gui.getStage());
        stage.setTitle(connectionName + " connection view");
        stage.setScene(dialog);
        setNormalCursor();
        stage.show();
        gui.getConsoleComponent().runScriptSilent(cleanupRQueryString().append("rm(connectionsTabDf)").toString());
        stage.toFront();
        stage.requestFocus();
        stage.setAlwaysOnTop(false);
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
