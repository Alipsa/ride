package se.alipsa.ride.code.munin;

import static se.alipsa.ride.inout.viewer.ViewTab.viewSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.inout.viewer.ViewTab;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.ExceptionAlert;

import java.util.Map;

public class MuninRTab extends MuninTab {

  public MuninRTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, report, con);
    getMiscTab().setReportType(ReportType.UNMANAGED);
    if (report.getDefinition() != null) {
      replaceContentText(0,0, report.getDefinition());
    }
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    if (getMuninReport().getInputContent() != null && getMuninReport().getInputContent().trim().length() > 0) {
      //System.out.println("Report has parameters");
      Map<String, Object> inputParams = promptForInputParams();
      if (inputParams != null) {
        for (Map.Entry<String, Object> entry : inputParams.entrySet()) {
          consoleComponent.addVariableToSession(entry.getKey(), entry.getValue());
        }
      }
    }
    gui.setWaitCursor();
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

    Stage stage = new Stage();
    WebView browser = new WebView();
    WebEngine webEngine = browser.getEngine();
    webEngine.setUserStyleSheetLocation(ViewTab.BOOTSTRAP_CSS);
    ReportInput reportInput = new ReportInput(stage);
    browser.setContextMenuEnabled(false);
    createContextMenu(browser);
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

    String form = "<div class='container pt-2'><form><div class='form-group'>" + getMuninReport().getInputContent() + "</div><button type='submit'>Submit</button></form></div>\n" +
        "<script>\n" +
        "function submitForm(event) {\n" +
        "  event.preventDefault();\n" +
        "  const data = new FormData(event.target);\n" +
        "  const value = Object.fromEntries(data.entries());\n" +
        "  const json = JSON.stringify(value);\n" +
        "  // console.log('Form submitted'); \n" +
        "  app.addParams(json);\n" +
        "}\n" +
        "const form = document.querySelector('form');\n" +
        "form.addEventListener('submit', submitForm);\n" +
        "</script>";
    webEngine.loadContent(form);
    //Scene dialog = new Scene(browser, 640, 480);
    Scene dialog = new Scene(browser);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initOwner(gui.getStage());
    stage.setTitle(getMuninReport().getReportName() + ", input parameters");
    //GuiUtils.addStyle(gui, dialog.getStylesheets());
    stage.setScene(dialog);
    stage.sizeToScene();
    stage.showAndWait();

    try {
      //System.out.println("promptForInputParams: params = " + reportInput.asMap());
      return reportInput.asMap();
    } catch (JsonProcessingException e) {
      ExceptionAlert.showAlert("Failed to retrieve params from input form", e);
      return null;
    }
  }

  private void createContextMenu(WebView browser) {
    ContextMenu contextMenu = new ContextMenu();
    WebEngine webEngine = browser.getEngine();
    MenuItem viewSourceMI = new MenuItem("View source");
    viewSourceMI.setOnAction(a -> viewSource(webEngine, this));
    contextMenu.getItems().add(viewSourceMI);
    browser.setOnMousePressed(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        contextMenu.show(browser, e.getScreenX(), e.getScreenY());
      } else {
        contextMenu.hide();
      }
    });
  }

  public class Console {
    public void log(String text) {
      System.out.println(text);
    }
  }
}
