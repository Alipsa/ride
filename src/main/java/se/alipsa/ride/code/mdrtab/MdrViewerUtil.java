package se.alipsa.ride.code.mdrtab;

import javafx.concurrent.Task;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.inout.InoutComponent;
import se.alipsa.ride.utils.ExceptionAlert;

import java.util.Collections;

public class MdrViewerUtil {


 public static void viewMdr(Ride gui, String title, String textContent) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();
   InoutComponent inout = gui.getInoutComponent();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          SEXP htmlContent = consoleComponent
              .runScript("library('se.alipsa:mdr2html')\n renderMdr(mdrContent)",
                  Collections.singletonMap("mdrContent", textContent));
          inout.viewHtmlWithBootstrap(htmlContent, title);
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      consoleComponent.waiting();
    });

    task.setOnFailed(e -> {
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      consoleComponent.waiting();
      ExceptionAlert.showAlert(ex.getMessage(), ex);
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    consoleComponent.startThreadWhenOthersAreFinished(thread, "mdrFile");
  }
}
