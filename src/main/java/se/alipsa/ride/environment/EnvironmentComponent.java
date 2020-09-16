package se.alipsa.ride.environment;

import static se.alipsa.ride.Constants.INDENT;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.environment.connections.ConnectionsTab;
import se.alipsa.ride.utils.UniqueList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EnvironmentComponent extends TabPane {

  private static final Logger LOG = LogManager.getLogger();

  private UnStyledCodeArea envTa;

  ConnectionsTab connectionsTab;
  HistoryTab historyTab;

  List<ContextFunctionsUpdateListener> contextFunctionsUpdateListeners = new ArrayList<>();

  int MAX_CONTENT_LENGTH = 200;

  public EnvironmentComponent(Ride gui) {
    Tab environment = new Tab();
    environment.setText("Environment");
    envTa = new UnStyledCodeArea();
    envTa.setEditable(false);
    envTa.getStyleClass().add("environment");
    envTa.replaceText("Environment");
    VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(envTa);
    environment.setContent(scrollPane);
    getTabs().add(environment);

    historyTab = new HistoryTab();
    getTabs().add(historyTab);

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
    envTa.clear();
  }

  public Set<ConnectionInfo> getConnections() {
    return connectionsTab.getConnections();
  }

  public void addInputHistory(String text) {
    historyTab.addInputHistory(text);
  }

  public void addOutputHistory(String text) {
    historyTab.addOutputHistory(text);
  }

  public void rRestarted() {
    historyTab.rRestarted();
  }

  public void updateContextFunctions(StringVector functions, StringVector objects) {
    final UniqueList<String> contextFunctions = new UniqueList<>();
    final UniqueList<String> contextObjects = new UniqueList<>();
    for (String fun : functions) {
      contextFunctions.add(fun);
    }
    for (String obj : objects) {
      contextObjects.add(obj);
    }
    Platform.runLater(() ->
      contextFunctionsUpdateListeners.forEach(l -> l.updateContextFunctions(contextFunctions, contextObjects))
    );
  }

  public void addContextFunctionsUpdateListener(ContextFunctionsUpdateListener listener) {
    contextFunctionsUpdateListeners.add(listener);
  }

  public void removeContextFunctionsUpdateListener(ContextFunctionsUpdateListener listener) {
    contextFunctionsUpdateListeners.remove(listener);
  }
}
