package se.alipsa.ride.environment.connections;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsTab extends Tab {

  BorderPane contentPane;

  List<Connection> connections;

  public ConnectionsTab() {
    setText("Connections");
    connections = new ArrayList<>();
    contentPane = new BorderPane();
    setContent(contentPane);

    FlowPane inputPane = new FlowPane();
    contentPane.setTop(inputPane);

    Label nameLabel = new Label("Name:");
    inputPane.getChildren().add(nameLabel);
    TextField nameText = new TextField();
    inputPane.getChildren().add(nameText);

    Label driverLabel = new Label("Driver:");
    inputPane.getChildren().add(driverLabel);
    TextField driverText = new TextField();
    inputPane.getChildren().add(driverText);

    Label urlLabel = new Label("Url:");
    inputPane.getChildren().add(urlLabel);
    TextField urlText = new TextField();
    inputPane.getChildren().add(urlText);

    Button addButton = new Button("Add");
    TextArea connectionsTable = new TextArea(); // TODO make this a tableView
    contentPane.setCenter(connectionsTable);

    addButton.setOnAction(e -> {
      connections.add(new Connection(nameText.getText(), driverText.getText(), urlText.getText()));
      StringBuilder builder = new StringBuilder();
      for (Connection con : connections) {
        builder.append(con.getName());
        builder.append(", \t");
        builder.append(con.getDriver());
        builder.append(", \t");
        builder.append(con.getUrl());
        builder.append(", \t");
      }
      connectionsTable.setText(builder.toString());
    });
    inputPane.getChildren().add(addButton);
  }

  public List<Connection> getConnections() {
    return connections;
  }
}
