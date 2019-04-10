package se.alipsa.ride.environment;

import static se.alipsa.ride.Constants.INDENT;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.environment.connections.ConnectionsTab;

import java.util.Set;

public class EnvironmentComponent extends TabPane {

  StyleClassedTextArea envTa;

  ConnectionsTab connectionsTab;

  int MAX_CONTENT_LENGTH = 200;

  public EnvironmentComponent(Ride gui) {
    Tab environment = new Tab();
    environment.setText("Environment");
    envTa = new StyleClassedTextArea();
    envTa.appendText("Environment");
    VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(envTa);
    environment.setContent(scrollPane);
    getTabs().add(environment);

    Tab history = new Tab();
    history.setText("History");

    getTabs().add(history);

    connectionsTab = new ConnectionsTab(gui);

    getTabs().add(connectionsTab);
    setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
  }

  public void setEnvironment(Environment env, Context ctx) {
    envTa.clear();
    StringVector names = env.getNames();
    //StringBuffer buf = new StringBuffer();
    for (String varName : names) {
      int start = envTa.getContent().getLength();
      envTa.appendText(varName);
      int endVar = start + varName.length();
      //envTa.setStyle(start, endVar, "-fx-font-weight: bold;");
      //envTa.setStyle(start, endVar, "-fx-font-style: italic;");
      envTa.setStyleClass(start, endVar, "env-varName");
      String content = env.getVariable(ctx, varName).toString();
      if (content.length() > MAX_CONTENT_LENGTH) {
        content = content.substring(0, MAX_CONTENT_LENGTH) + "... (length = " + content.length() + ")";
      }
      content = INDENT + content;
      envTa.appendText( content + "\n");
      //envTa.setStyle(endVar + 1 , endVar + content.length(), "-fx-font-weight: normal;");
      //envTa.setStyle(endVar + 1 , endVar + content.length(), "-fx-font-style: normal;");
      envTa.setStyleClass(endVar + 1 , endVar + content.length(), "env-varValue");
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
