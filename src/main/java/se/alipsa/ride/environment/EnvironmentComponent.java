package se.alipsa.ride.environment;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.environment.connections.ConnectionsTab;

import java.util.Set;

public class EnvironmentComponent extends TabPane {

  InlineCssTextArea envTa;

  ConnectionsTab connectionsTab;

  public EnvironmentComponent(Ride gui) {
    Tab environment = new Tab();
    environment.setText("Environment");
    envTa = new InlineCssTextArea();
    envTa.appendText("Environment");
    environment.setContent(envTa);
    getTabs().add(environment);

    Tab history = new Tab();
    history.setText("History");

    getTabs().add(history);

    connectionsTab = new ConnectionsTab(gui);

    getTabs().add(connectionsTab);
  }

  public void setEnvironment(Environment env, Context ctx) {
    envTa.clear();
    StringVector names = env.getNames();
    //StringBuffer buf = new StringBuffer();
    for (String varName : names) {
      int start = envTa.getContent().getLength();
      envTa.appendText(varName);
      envTa.setStyle(start, start + varName.length(), "-fx-font-weight: bold;");
      envTa.appendText("\t" + env.getVariable(ctx, varName).toString() + "\n");
      /*
      buf.append(varName);
      buf.append("\t");
      buf.append(env.getVariable(ctx, varName).toString());
      buf.append("\n");
      */
    }
    //envTa.appendText(buf.toString());
  }

  public void clearEnvironment() {
    //envTa.setText("");
    envTa.clear();
  }

  public Set<ConnectionInfo> getConnections() {
    return connectionsTab.getConnections();
  }
}
