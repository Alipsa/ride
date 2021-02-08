package se.alipsa.ride.utils;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.REPORT_BUG;
import static se.alipsa.ride.Constants.THEME;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;

import java.net.URL;

public final class GuiUtils {

  private static final Logger log = LogManager.getLogger();

  private GuiUtils() {
    // Utility class
  }

  public static void addStyle(Ride gui, Dialog<?> dialog) {
    addStyle(gui, dialog.getDialogPane());
  }

  public static void addStyle(Ride gui, Parent dialog) {
    addStyle(gui, dialog.getStylesheets());
  }

  public static void addStyle(Ride gui, ObservableList<String> styleSheetList) {
    if (gui != null && styleSheetList != null ) {
      String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

      URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
      if (styleSheetUrl != null) {
        styleSheetList.add(styleSheetUrl.toExternalForm());
      }
    } else {
      String msg = "GuiUtils.addStyle() : Ride instance is " + gui + ", style sheet list is " + styleSheetList + ". Called from "
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
