package se.alipsa.ride.environment.connections;

import static se.alipsa.ride.Constants.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import se.alipsa.ride.Ride;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsTab extends Tab {

  private BorderPane contentPane;
  private List<Connection> connections;
  private Ride gui;

  private static final String DRIVER_PREF = "ConnectionsTab.driver";
  private static final String URL_PREF = "ConnectionsTab.url";

  public ConnectionsTab(Ride gui) {
    setText("Connections");
    this.gui = gui;
    connections = new ArrayList<>();
    contentPane = new BorderPane();
    setContent(contentPane);

    FlowPane inputPane = new FlowPane();
    inputPane.setPadding(FLOWPANE_INSETS);
    inputPane.setVgap(VGAP);
    inputPane.setHgap(HGAP);
    contentPane.setTop(inputPane);

    Label nameLabel = new Label("Name:");
    inputPane.getChildren().add(nameLabel);
    TextField nameText = new TextField();
    nameText.setPrefWidth(80);
    inputPane.getChildren().add(nameText);

    Label driverLabel = new Label("Driver:");
    inputPane.getChildren().add(driverLabel);
    TextField driverText = new TextField(getPrefOrBlank(DRIVER_PREF));
    inputPane.getChildren().add(driverText);

    Label urlLabel = new Label("Url:");
    inputPane.getChildren().add(urlLabel);
    TextField urlText = new TextField(getPrefOrBlank(URL_PREF));
    inputPane.getChildren().add(urlText);

    Button addButton = new Button("Add");
    TextArea connectionsTable = new TextArea(); // TODO make this a tableView
    contentPane.setCenter(connectionsTable);

    addButton.setOnAction(e -> {
      connections.add(new Connection(nameText.getText(), driverText.getText(), urlText.getText()));
      StringBuilder builder = new StringBuilder();
      for (Connection con : connections) {
        builder.append(con.getName());
        builder.append(" \t");
        builder.append(con.getDriver());
        builder.append(" \t");
        builder.append(con.getUrl());
        builder.append(" \n");
      }
      setPref(DRIVER_PREF, driverText.getText());
      setPref(URL_PREF, urlText.getText());
      connectionsTable.setText(builder.toString());
    });
    inputPane.getChildren().add(addButton);
  }

  private String getPrefOrBlank(String pref) {
    return gui.getPrefs().get(pref, "");
  }

  private void setPref(String pref, String val) {
    gui.getPrefs().put(pref, val);
  }

  public List<Connection> getConnections() {
    return connections;
  }
}
