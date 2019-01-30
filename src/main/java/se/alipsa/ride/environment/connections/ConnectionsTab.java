package se.alipsa.ride.environment.connections;

import static se.alipsa.ride.Constants.*;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.Alerts;

import java.util.*;

public class ConnectionsTab extends Tab {

  private BorderPane contentPane;
  private Ride gui;

  private static final String DRIVER_PREF = "ConnectionsTab.driver";
  private static final String URL_PREF = "ConnectionsTab.url";

  private TextField nameText;
  private TextField driverText;
  private TextField urlText;
  private TableView<Connection> connectionsTable = new TableView<>();

  public ConnectionsTab(Ride gui) {
    setText("Connections");
    this.gui = gui;
    contentPane = new BorderPane();
    setContent(contentPane);

    FlowPane inputPane = new FlowPane();
    inputPane.setPadding(FLOWPANE_INSETS);
    inputPane.setVgap(VGAP);
    inputPane.setHgap(HGAP);
    contentPane.setTop(inputPane);

    Label nameLabel = new Label("Name:");
    inputPane.getChildren().add(nameLabel);
    nameText = new TextField();
    nameText.setPrefWidth(80);
    inputPane.getChildren().add(nameText);

    Label driverLabel = new Label("Driver:");
    inputPane.getChildren().add(driverLabel);
    driverText = new TextField(getPrefOrBlank(DRIVER_PREF));
    inputPane.getChildren().add(driverText);

    Label urlLabel = new Label("Url:");
    inputPane.getChildren().add(urlLabel);
    urlText = new TextField(getPrefOrBlank(URL_PREF));
    inputPane.getChildren().add(urlText);

    Button addButton = new Button("Add");
    createConnectionTableView();
    contentPane.setCenter(connectionsTable);

    addButton.setOnAction(e -> {
      Connection con = new Connection(nameText.getText(), driverText.getText(), urlText.getText());
      //connections.add(con);
      setPref(DRIVER_PREF, driverText.getText());
      setPref(URL_PREF, urlText.getText());
      if (!connectionsTable.getItems().contains(con)) {
        connectionsTable.getItems().add(con);
      }
    });
    inputPane.getChildren().add(addButton);
  }

  private TableView<Connection> createConnectionTableView() {
    TableColumn<Connection, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(
        new PropertyValueFactory<>("name")
    );
    TableColumn<Connection,String> driverCol = new TableColumn<>("Driver");
    driverCol.setCellValueFactory(
        new PropertyValueFactory<>("driver")
    );
    TableColumn<Connection,String> urlCol = new TableColumn<>("URL");
    urlCol.setCellValueFactory(
        new PropertyValueFactory<>("url")
    );

    connectionsTable.getColumns().addAll(nameCol, driverCol, urlCol);
    connectionsTable.setRowFactory(tableView -> {
      final TableRow<Connection> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem removeMenuItem = new MenuItem("delete connection");
      removeMenuItem.setOnAction(event -> {
        connectionsTable.getItems().remove(row.getItem());
      });
      final MenuItem viewMenuItem = new MenuItem("view connection");
      viewMenuItem.setOnAction(event -> {
        // TODO: fixme!
        Alerts.info("Not yet implemented",
            "Viewing connection meta data is not yet implemented");
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

  public Set<Connection> getConnections() {
    return new TreeSet<>(connectionsTable.getItems());
  }

  /**
   * this is consistent for at least H2 and SQl server
   * select col.TABLE_NAME
   * , TABLE_TYPE
   * , COLUMN_NAME
   * , ORDINAL_POSITION
   * , IS_NULLABLE
   * , DATA_TYPE
   * , CHARACTER_MAXIMUM_LENGTH
   * , NUMERIC_PRECISION_RADIX
   * , NUMERIC_SCALE
   * , COLLATION_NAME
   *  from INFORMATION_SCHEMA.COLUMNS col
   * inner join INFORMATION_SCHEMA.TABLES tab on col.TABLE_NAME = tab.TABLE_NAME and col.TABLE_SCHEMA = tab.TABLE_SCHEMA
   * where TABLE_TYPE <> 'SYSTEM TABLE'
   */
}
