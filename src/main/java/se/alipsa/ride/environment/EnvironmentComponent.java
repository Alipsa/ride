package se.alipsa.ride.environment;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.environment.connections.ConnectionsTab;

import java.util.Set;

public class EnvironmentComponent extends TabPane {

  TextArea envTA;

  ConnectionsTab connectionsTab;

  public EnvironmentComponent(Ride gui) {
    Tab environment = new Tab();
    environment.setText("Environment");
    envTA = new TextArea();
    envTA.setText("Environment");
    environment.setContent(envTA);
    getTabs().add(environment);

    Tab history = new Tab();
    history.setText("History");

    getTabs().add(history);

    connectionsTab = new ConnectionsTab(gui);

    getTabs().add(connectionsTab);
  }

  public void setEnvironment(Environment env, Context ctx) {
    StringVector names = env.getNames();
    StringBuffer buf = new StringBuffer();
    for (String varName : names) {
      buf.append(varName);
      buf.append("\t");
      buf.append(env.getVariable(ctx, varName).toString());
      buf.append("\n");
    }
    envTA.setText(buf.toString());
  }

  public void clearEnvironment() {
    envTA.setText("");
  }

  public Set<ConnectionInfo> getConnections() {
    return connectionsTab.getConnections();
  }
}
