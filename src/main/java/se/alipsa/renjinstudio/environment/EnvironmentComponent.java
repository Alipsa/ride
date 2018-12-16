package se.alipsa.renjinstudio.environment;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import se.alipsa.renjinstudio.RenjinStudio;

public class EnvironmentComponent extends TabPane {

    public EnvironmentComponent(RenjinStudio gui) {
        Tab environment = new Tab();
        environment.setText("Environment");
        TextArea northEast = new TextArea();
        northEast.setText("Environment");
        environment.setContent(northEast);
        getTabs().add(environment);

        Tab history = new Tab();
        history.setText("History");

        getTabs().add(history);

        Tab connections = new Tab();
        connections.setText("Connections");

        getTabs().add(connections);
    }
}
