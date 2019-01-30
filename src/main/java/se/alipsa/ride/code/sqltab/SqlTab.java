package se.alipsa.ride.code.sqltab;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.sexp.ListVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.environment.connections.Connection;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.util.Set;

public class SqlTab extends TextAreaTab {

  private SqlTextArea sqlTextArea;
  private Button runButton;
  private Button runUpdateButton;
  private ComboBox<Connection> connectionCombo;

  public SqlTab(String title, Ride gui) {
    super(gui);
    setTitle(title);
    BorderPane pane = new BorderPane();

    FlowPane buttonPane = new FlowPane();
    buttonPane.setHgap(5);
    buttonPane.setPadding(new Insets(5, 10, 5, 5));
    pane.setTop(buttonPane);

    buttonPane.getChildren().add(saveButton);

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
      Set<Connection> connections = gui.getEnvironmentComponent().getConnections();
      connectionCombo.setItems(FXCollections.observableArrayList(connections));
      int rows = Math.min(5, connections.size());
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
    setContent(pane);
  }

  private void runSelectQuery(ActionEvent actionEvent) {
    setWaitCursor();
    String rCode = baseRQueryString(connectionCombo.getValue(), "sqlTabDf <- dbGetQuery", getTextContent()).toString();
    try {
      gui.getConsoleComponent().runScriptSilent(rCode);
      ListVector df = (ListVector) gui.getConsoleComponent().fetchVar("sqlTabDf");
      // cleanup
      gui.getInoutComponent().view(df, getTitle());

      setNormalCursor();
    } catch (Exception e) {
      setNormalCursor();
      ExceptionAlert.showAlert("Failed: " + e.getMessage(), e);
    }

    try {
      gui.getConsoleComponent().runScriptSilent("dbDisconnect(sqlTabCon); rm(sqlTabDrv); rm(sqlTabCon); rm(sqlTabDf)");
    } catch (Exception e) {
      setNormalCursor();
      ExceptionAlert.showAlert("Failed: " + e.getMessage(), e);
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
      gui.getConsoleComponent().runScriptSilent("dbDisconnect(sqlTabCon); rm(sqlTabDrv); rm(sqlTabCon); rm(sqlTabNumRows)");
    } catch (Exception e) {
      ExceptionAlert.showAlert("Cleanup failed: " + e.getMessage(), e);
    }
    //System.out.println("update query done!");
  }

  private StringBuilder baseRQueryString(Connection con, String command, String sql) {
    StringBuilder str = new StringBuilder();
    str.append("library('DBI')\n library('org.renjin.cran:RJDBC')\n")
        .append("sqlTabDrv <- JDBC('").append(con.getDriver()).append("')\n")
        .append("sqlTabCon <- dbConnect(sqlTabDrv, url='").append(con.getUrl()).append("')\n")
        .append(command).append("(sqlTabCon, \"").append(sql).append("\")");
    return str;
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
}
