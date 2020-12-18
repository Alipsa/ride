package se.alipsa.ride.utils;

import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;

import javax.management.RuntimeErrorException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import static se.alipsa.ride.Constants.*;

public final class GuiUtils {

  private static final Logger log = LogManager.getLogger();

  private GuiUtils() {
    // Utility class
  }

  public static void addStyle(Ride gui, Dialog<?> dialog) {
    if (gui != null && dialog != null ) {
      String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

      URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
      if (styleSheetUrl != null) {
        dialog.getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
      }
    } else {
      String msg = "GuiUtils.addStyle() : Ride instance is " + gui + ", dialog is " + dialog + ". Called from "
          + InvocationUtils.callingMethod(3) + ". " + REPORT_BUG;
      log.error(msg);

      if (Platform.isFxApplicationThread()) {
        Alerts.warn("Unexpected error", msg);
      } else {
        throw new RuntimeException(msg);
      }
    }
  }
}
