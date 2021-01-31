package se.alipsa.ride.code.munin;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.ExceptionAlert;

public class MuninRTab extends MuninTab {

  public MuninRTab(Ride gui, MuninReport report, MuninConnection con) {
    super(gui, report, con);
  }

  @Override
  void viewAction(ActionEvent actionEvent) {
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
}
