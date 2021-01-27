package se.alipsa.ride.code.munin;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.Ride;
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
    Task<SEXP> task = new Task<SEXP>() {
      @Override
      protected SEXP call() throws Exception {
        try {
          return gui.getConsoleComponent().runScriptSilent(getTextContent());
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
      }
    };
    task.setOnSucceeded(e -> {
      SEXP result = task.getValue();
      gui.getInoutComponent().viewHtml(result, getTitle());
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
