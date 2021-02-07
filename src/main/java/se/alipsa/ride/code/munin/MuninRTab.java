package se.alipsa.ride.code.munin;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.ExceptionAlert;

import java.util.Map;

public class MuninRTab extends MuninTab {

  public MuninRTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, report, con);
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
    if (muninReport.getInputContent() != null && muninReport.getInputContent().trim().length() > 0) {
      System.out.println("Report has parameters");
      Map<String, Object> inputParams = promptForInputParams();
    }
    gui.setWaitCursor();
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    Task<SEXP> task = new Task<SEXP>() {
      @Override
      protected SEXP call() throws Exception {
        try {
          SEXP result = consoleComponent.runScript(getTextContent());
          if (!(result instanceof StringArrayVector)) {
            String varName = ".muninUnmanagedReportResult";
            consoleComponent.addVariableToSession(varName, result);
            // If the last statement is html.add() SEXP.asString() does not work (will not call as.character)
            // so we check and convert it if needed. This has the added benefit of enabling partial view of marked text
            result = consoleComponent.runScript("as.character(.muninUnmanagedReportResult)");
            consoleComponent.removeVariableFromSession(varName);
          }
          return result;

        } catch (RuntimeException e) {
          throw new Exception(e);
        }
      }
    };
    task.setOnSucceeded(e -> {
      SEXP result = task.getValue();
      gui.getInoutComponent().viewHtmlWithBootstrap(result, getTitle());
      gui.getConsoleComponent().updateEnvironment();
      gui.setNormalCursor();
    });
    task.setOnFailed(e -> {
      gui.setNormalCursor();
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      ExceptionAlert.showAlert(ex.getMessage(), ex);
    });

    Thread thread = new Thread(task);
    thread.setDaemon(false);
    gui.getConsoleComponent().startThreadWhenOthersAreFinished(thread, "muninReport");
  }

  private final Console console = new Console();

  private Map<String, Object> promptForInputParams() {

    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();
    ReportInput reportInput = new ReportInput();
    webEngine.getLoadWorker().stateProperty().addListener(
        (ov, oldState, newState) -> {

          if (newState == State.SUCCEEDED) {
            JSObject win =
                (JSObject) webEngine.executeScript("window");
            win.setMember("app", reportInput);
            win.setMember("console", console);
          }
        }
    );

    String form = "<form>" + muninReport.getInputContent() + "<button type='button' onclick='submitForm()'>Submit</button></form>" +
        "<script>\n" +
        "function submitForm() {\n" +
        "  console.log('Form submitted'); \n" +
        "  app.addParams();\n" +
        "}\n" +
        "</script>";
    webEngine.loadContent(form);
    Scene dialog = new Scene(browser, 640, 480);
    Stage stage = new Stage();
    stage.setWidth(640);
    stage.setHeight(480);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initOwner(gui.getStage());
    stage.setTitle(muninReport.getReportName() + " input");
    stage.setScene(dialog);

    stage.showAndWait();

    System.out.println("promptForInputParams: params = " + reportInput.params);
    return null;
  }

  public class Console {
    public void log(String text) {
      System.out.println(text);
    }
  }
}
