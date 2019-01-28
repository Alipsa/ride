package se.alipsa.ride.code.sqltab;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.environment.connections.Connection;

import java.io.File;
import java.util.List;

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
    connectionCombo.setOnMouseClicked(e -> {
      List<Connection> connections = gui.getEnvironmentComponent().getConnections();
      connectionCombo.setItems(FXCollections.observableArrayList(connections));
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
    //gui.setWaitCursor();
    String sql = getTextContent();
    Connection con = connectionCombo.getValue();
    String rcode = baseRQueryString(con);
    rcode += "sqlTabDf <- dbGetQuery(sqlTabCon, \"" + sql + "\")\n";
    rcode += "dbDisconnect(sqlTabCon)";
    gui.getConsoleComponent().runScriptSilent(rcode);
    ListVector df = (ListVector)gui.getConsoleComponent().fetchVar("sqlTabDf");
    // cleanup
    gui.getInoutComponent().view(df, getTitle());
    rcode = "rm(sqlTabDrv); rm(sqlTabCon); rm(sqlTabDf)";
    gui.getConsoleComponent().runScriptSilent(rcode);
    //gui.setNormalCursor();
  }

  private void runUpdateQuery(ActionEvent actionEvent) {
    //gui.setWaitCursor();
    String sql = getTextContent();
    Connection con = connectionCombo.getValue();
    String rcode = baseRQueryString(con);
    rcode += "sqlTabNumRows <- dbSendUpdate(sqlTabCon, \"" + sql + "\")\n";
    rcode += "dbDisconnect(sqlTabCon)";
    gui.getConsoleComponent().runScriptSilent(rcode);
    SEXP result = gui.getConsoleComponent().fetchVar("sqlTabNumRows");
    System.out.println("Update result: " + result);
    rcode = "rm(sqlTabDrv); rm(sqlTabCon); rm(sqlTabNumRows)";
    gui.getConsoleComponent().runScriptSilent(rcode);
    //gui.setNormalCursor();
  }

  private String baseRQueryString(Connection con) {
    String rcode = "library('DBI')\n library('org.renjin.cran:RJDBC')\n";
    rcode += "sqlTabDrv <- JDBC('" + con.getDriver() + "')\n";
    rcode += "sqlTabCon <- dbConnect(sqlTabDrv, url='" + con.getUrl() + "')\n";
    return rcode;
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
