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
      int endVar = start + varName.length();
      envTa.setStyle(start, endVar, "-fx-font-weight: bold;");
      String content = env.getVariable(ctx, varName).toString();
      envTa.appendText("\t" + content + "\n");
      envTa.setStyle(endVar + 1 , endVar + content.length(), "-fx-font-weight: normal;");
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
