package se.alipsa.ride.code.sqltab;

import static se.alipsa.ride.utils.RQueryBuilder.baseRQueryString;
import static se.alipsa.ride.utils.RQueryBuilder.cleanupRQueryString;

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
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    /*
    setWaitCursor();
    //System.out.println("Runing update:\n" + sql);
    String rCode = baseRQueryString(connectionCombo.getValue(), "dbSendUpdate", getTextContent()).toString();
    try {
      //System.out.println("  calling scriptengine");
      gui.getConsoleComponent().runScriptSilent(rCode);
      //System.out.println("  cleanup");
      setNormalCursor();
    } catch (Exception e) {
      setNormalCursor();
      ExceptionAlert.showAlert("Failed: " + e.getMessage(), e);
    }
    try {
      gui.getConsoleComponent().runScriptSilent(cleanupRQueryString().toString());
    } catch (Exception e) {
      ExceptionAlert.showAlert("Cleanup failed: " + e.getMessage(), e);
    }
    //System.out.println("update query done!");
     */
    setWaitCursor();
    String[] batchedQry = getTextContent().split(";");
    Task<int[]> updateTask = new Task<int[]>() {
      @Override
      protected int[] call() throws Exception {
        Connection con = null;
        List<Integer> numRows = new ArrayList<>();
        try {
          ConnectionInfo ci = connectionCombo.getValue();
          Class.forName(ci.getDriver());
          if (ci.getUser() == null) {
            con = DriverManager.getConnection(ci.getUrl());
          } else {
            con = DriverManager.getConnection(ci.getUrl(), ci.getUser(), ci.getPassword());
          }
          try (Statement stm = con.createStatement()) {
            for (String qry : batchedQry) {
              int result = stm.executeUpdate(qry);
              log.info("{} : {}", qry, result);
              numRows.add(result);
            }
          }
        } finally {
          if (con != null) {
            con.close();
          }
        }
        return numRows.stream().mapToInt(i->i).toArray();
      }
    };
    updateTask.setOnSucceeded(e -> {
      setNormalCursor();
      int[] numRows = updateTask.getValue();
      StringBuilder buf = new StringBuilder();
      int count = 1;
      for (int num : numRows) {
        buf.append(count++).append(". Number of rows affected: ").append(num).append("\n");
      }
      getGui().getConsoleComponent().addOutput(getTitle(), buf.toString());
    });

    updateTask.setOnFailed(e -> {
      setNormalCursor();
      Throwable exc = updateTask.getException();
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
