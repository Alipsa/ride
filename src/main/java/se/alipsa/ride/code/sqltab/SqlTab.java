package se.alipsa.ride.code.sqltab;

import static se.alipsa.ride.utils.RQueryBuilder.baseRQueryString;
import static se.alipsa.ride.utils.RQueryBuilder.cleanupRQueryString;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.sexp.ListVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.SqlParser;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;

public class SqlTab extends TextAreaTab {

  private SqlTextArea sqlTextArea;
  private Button runButton;
  private Button runUpdateButton;
  private ComboBox<ConnectionInfo> connectionCombo;

  private Logger log = LoggerFactory.getLogger(SqlTab.class);

  public SqlTab(String title, Ride gui) {
    super(gui, CodeType.SQL);
    setTitle(title);

    runButton = new Button("Run select");
    runButton.setDisable(true);
    runButton.setOnAction(this::runSelectQuery);
    buttonPane.getChildren().add(runButton);

    runUpdateButton = new Button("Run update");
    runUpdateButton.setDisable(true);
    runUpdateButton.setOnAction(this::runUpdateQuery);
    buttonPane.getChildren().add(runUpdateButton);

    connectionCombo = new ComboBox<>();
    connectionCombo.setTooltip(new Tooltip("Create connections in the Connections tab \nand select the name here"));
    connectionCombo.setOnMouseClicked(e -> {
      connectionCombo.hide();
      Set<ConnectionInfo> connectionInfos = gui.getEnvironmentComponent().getConnections();
      connectionCombo.setItems(FXCollections.observableArrayList(connectionInfos));
      int rows = Math.min(5, connectionInfos.size());
      connectionCombo.setVisibleRowCount(rows);
      connectionCombo.show();
    });
    connectionCombo.getSelectionModel().selectedItemProperty().addListener(
        (options, oldValue, newValue) -> {
          runButton.setDisable(false);
          runUpdateButton.setDisable(false);
        }
    );
    buttonPane.getChildren().add(connectionCombo);

    sqlTextArea = new SqlTextArea(this);
    VirtualizedScrollPane<SqlTextArea> scrollPane = new VirtualizedScrollPane<>(sqlTextArea);
    pane.setCenter(scrollPane);
  }

  private void runSelectQuery(ActionEvent actionEvent) {

    setWaitCursor();
    String[] batchedQry = getTextContent().split(";");
    ConnectionInfo connection = connectionCombo.getValue();
    for (String qry : batchedQry) {
      try {
        String rCode = baseRQueryString(connection, "sqlTabDf <- dbGetQuery", qry).toString();
        gui.getConsoleComponent().runScriptSilent(rCode);
        ListVector df = (ListVector) gui.getConsoleComponent().fetchVar("sqlTabDf");
        gui.getInoutComponent().view(df, getTitle());
      } catch(Exception e){
        setNormalCursor();
        ExceptionAlert.showAlert("Failed: " + e.getMessage(), e);
      }
      // cleanup
      try {
        gui.getConsoleComponent().runScriptSilent(cleanupRQueryString().append("if(exists(\"sqlTabDf\")) rm(sqlTabDf)").toString());
      } catch (Exception e) {
        setNormalCursor();
        ExceptionAlert.showAlert("Failed: " + e.getMessage(), e);
      }
      setNormalCursor();
    }
  }

  private void setNormalCursor() {
    gui.setNormalCursor();
    sqlTextArea.setCursor(Cursor.DEFAULT);
  }

  private void setWaitCursor() {
    gui.setWaitCursor();
    sqlTextArea.setCursor(Cursor.WAIT);
  }

  private void runUpdateQuery(ActionEvent actionEvent) {
    setWaitCursor();
    final ConsoleComponent consoleComponent = getGui().getConsoleComponent();
    StringBuilder parseMessage = new StringBuilder();
    String[] batchedQry = SqlParser.split(getTextContent(), parseMessage);
    if (parseMessage.length() > 0) {
      consoleComponent.addWarning(getTitle(), parseMessage.toString(), false);
    } else {
      consoleComponent.addOutput(getTitle(), "Query contains " + batchedQry.length + " statements", false, true);
    }

    Task<Void> updateTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Connection con = null;
        try {
          ConnectionInfo ci = connectionCombo.getValue();
          Class.forName(ci.getDriver());
          if (ci.getUser() == null) {
            con = DriverManager.getConnection(ci.getUrl());
          } else {
            con = DriverManager.getConnection(ci.getUrl(), ci.getUser(), ci.getPassword());
          }
          try (Statement stm = con.createStatement()) {
            for (int count = 0; count < batchedQry.length; count++) {
              String qry = batchedQry[count];
              int result = stm.executeUpdate(qry);
              log.info("{} : {}", qry, result);
              final int rowNum = count +1;
              Platform.runLater(() ->
                consoleComponent.addOutput("", new StringBuilder()
                  .append(rowNum)
                  .append(". Number of rows affected: ")
                  .append(result).toString()
                  , false, true)
              );
            }
          }
        } finally {
          if (con != null) {
            con.close();
          }
        }
        return null;
      }
    };
    updateTask.setOnSucceeded(e -> {
      setNormalCursor();
      consoleComponent.addOutput("", "Success", true, false);
    });

    updateTask.setOnFailed(e -> {
      setNormalCursor();
      Throwable exc = updateTask.getException();
      consoleComponent.addWarning("","Failed to run update query", true);
      ExceptionAlert.showAlert("Failed to run update query", exc );
    });

    Thread scriptThread = new Thread(updateTask);
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
