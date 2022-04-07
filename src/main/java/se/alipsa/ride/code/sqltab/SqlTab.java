package se.alipsa.ride.code.sqltab;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.renjin.client.datautils.Table;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.SqlParser;
import se.alipsa.ride.utils.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SqlTab extends TextAreaTab {

  private final SqlTextArea sqlTextArea;
  private final Button executeButton;
  private final ComboBox<ConnectionInfo> connectionCombo;

  private static final Logger log = LogManager.getLogger(SqlTab.class);

  private static final int PRINT_QUERY_LENGTH = 30;

  public SqlTab(String title, Ride gui) {
    super(gui, CodeType.SQL);
    setTitle(title);

    executeButton = new Button("Run");
    executeButton.setDisable(true);
    executeButton.setOnAction(e -> executeQuery());
    buttonPane.getChildren().add(executeButton);

    connectionCombo = new ComboBox<>();
    connectionCombo.setTooltip(new Tooltip("Create connections in the Connections tab \nand select the name here"));
    connectionCombo.getSelectionModel().selectedItemProperty().addListener(
        (options, oldValue, newValue) -> executeButton.setDisable(false)
    );
    buttonPane.getChildren().add(connectionCombo);
    updateConnections();

    sqlTextArea = new SqlTextArea(this);
    VirtualizedScrollPane<SqlTextArea> scrollPane = new VirtualizedScrollPane<>(sqlTextArea);
    pane.setCenter(scrollPane);
  }

  public void updateConnections() {
    ConnectionInfo current = connectionCombo.getValue();
    Set<ConnectionInfo> connectionInfos = gui.getEnvironmentComponent().getConnections();
    connectionCombo.setItems(FXCollections.observableArrayList(connectionInfos));
    int rows = Math.min(5, connectionInfos.size());
    connectionCombo.setVisibleRowCount(rows);
    if (current != null && connectionCombo.getItems().contains(current)) {
      connectionCombo.setValue(current);
    } else if (connectionCombo.getItems().size() == 1) {
      connectionCombo.getSelectionModel().select(0);
    }
  }

  public void removeConnection(String connectionName) {
    connectionCombo.getItems().removeIf(c -> c.getName().equals(connectionName));
  }

  private void setNormalCursor() {
    gui.setNormalCursor();
    sqlTextArea.setCursor(Cursor.DEFAULT);
  }

  private void setWaitCursor() {
    gui.setWaitCursor();
    sqlTextArea.setCursor(Cursor.WAIT);
  }

  void executeQuery() {
    if (executeButton.isDisabled()) {
      Alerts.info("Cannot run SQL", "You  must select a database connection first!");
      return;
    }
    setWaitCursor();
    final ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    StringBuilder parseMessage = new StringBuilder();
    // The parser will not be able to understand more complex queries in which case
    // the whole sql code will be in batchedQry[0]
    String[] batchedQry = SqlParser.split(getTextContent(), parseMessage);
    if (parseMessage.length() > 0) {
      consoleComponent.addWarning(getTitle(), parseMessage.toString(), false);
    } else {
      consoleComponent.addOutput(getTitle(), "Query contains " + batchedQry.length + " statements", false, true);
    }

    Task<Void> updateTask = new Task<>() {
      @Override
      protected Void call() throws Exception {
        ConnectionInfo ci = connectionCombo.getValue();
        Optional<Connection> conOpt = ci.connect();
        if (conOpt.isEmpty()) {
          throw new Exception("Failed to establish a connection");
        }
        try (Connection con = conOpt.get()) {
          AtomicInteger queryCount = new AtomicInteger(1);
          try (Statement stm = con.createStatement()) {
            for (String qry : batchedQry) {
              boolean hasMoreResultSets = stm.execute(qry);

              int capLen = Math.min(qry.length(), PRINT_QUERY_LENGTH);
              String queryCapture = StringUtils.fixedLengthString(qry.substring(0, capLen).trim(), PRINT_QUERY_LENGTH);

              while (hasMoreResultSets || stm.getUpdateCount() != -1) {
                if (hasMoreResultSets) {
                  try (ResultSet rs = stm.getResultSet()) {
                    Table table = new Table(rs);
                    Platform.runLater(() ->
                        gui.getInoutComponent().showInViewer(table, SqlTab.this.getTitle() + " " + queryCount.getAndIncrement() + ".")
                    );
                  }
                } else { // if ddl/dml/...
                  int queryResult = stm.getUpdateCount();
                  if (queryResult == -1) { // no more queries processed
                    break;
                  }

                  Platform.runLater(() ->
                      consoleComponent.addOutput("", new StringBuilder()
                              .append(queryCount.getAndIncrement())
                              .append(". [")
                              .append(queryCapture)
                              .append("...], Rows affected: ")
                              .append(queryResult).toString()
                          , false, true)
                  );
                }
                hasMoreResultSets = stm.getMoreResults();
              }
            }
          }
        }
        return null;
      }
    };
    updateTask.setOnSucceeded(e -> {
      setNormalCursor();
      consoleComponent.addOutput("", "Success", true, true);
    });

    updateTask.setOnFailed(e -> {
      setNormalCursor();
      Throwable exc = updateTask.getException();
      consoleComponent.addWarning("","\nFailed to execute query\n" + exc, true);
      String clazz = exc.getClass().getName();
      String message = exc.getMessage() == null ? "" : "\n" + exc.getMessage();
      ExceptionAlert.showAlert("Query failed: " + clazz + message, exc );
    });

    Thread scriptThread = new Thread(updateTask);
    scriptThread.setContextClassLoader(gui.getConsoleComponent().getSession().getClassLoader());
    scriptThread.setDaemon(false);
    scriptThread.start();
  }

  @Override
  public File getFile() {
    return sqlTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    sqlTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return sqlTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return sqlTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    sqlTextArea.replaceContentText(start, end, content);
  }

  @Override
  public CodeTextArea getCodeArea() {
    return sqlTextArea;
  }
}
