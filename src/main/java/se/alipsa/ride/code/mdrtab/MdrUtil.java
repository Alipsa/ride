package se.alipsa.ride.code.mdrtab;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.inout.InoutComponent;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class MdrUtil {

  private static final Logger log = LogManager.getLogger();

  private static final String HIGHLIGHT_JS_CSS_PATH = "highlightJs/default.css";
  private static final String HIGHLIGHT_JS_SCRIPT_PATH = "highlightJs/highlight.pack.js";
  private static final String BOOTSTRAP_CSS_PATH = "META-INF/resources/webjars/bootstrap/5.1.3/css/bootstrap.css";
  private static final String HIGHLIGHT_JS_INIT = "\n<script>hljs.initHighlightingOnLoad();</script>\n";
  // The highlightJs stuff is in mdr
  public static final String HIGHLIGHT_JS_CSS = "<link rel='stylesheet' href='" + resourceUrlExternalForm(HIGHLIGHT_JS_CSS_PATH) + "'>";
  public static final String HIGHLIGHT_JS_SCRIPT = "<script src='" + resourceUrlExternalForm(HIGHLIGHT_JS_SCRIPT_PATH) + "'></script>";
  public static final String BOOTSTRAP_CSS = resourceUrlExternalForm(BOOTSTRAP_CSS_PATH);

  private static String resourceUrlExternalForm(String resource) {
    URL url = FileUtils.getResourceUrl(resource);
    return url == null ? "" : url.toExternalForm();
  }

  public static String getBootstrapStyle(boolean embed) {
    if (embed) {
      try {
        String css = FileUtils.readContent(BOOTSTRAP_CSS_PATH);
        return "<style>" + css + "</style>";
      } catch (IOException e) {
        log.warn("Failed to read content to embed, resort to external ref instead", e);
      }
    }
    return "<link rel='stylesheet' href='" + BOOTSTRAP_CSS + "'>";
  }

  public static String getHighlightStyle(boolean embed) {
    if (embed) {
      try {
        return "<style>" + FileUtils.readContent(HIGHLIGHT_JS_CSS_PATH) + "</style>\n\n";
      } catch (IOException e) {
        log.warn("Failed to get content of highlight css, falling back to external link.", e);
      }
    }
    return HIGHLIGHT_JS_CSS;
  }

  public static String getHighlightJs(boolean embed) {
    if (embed) {
      try {
        return "<script>" + FileUtils.readContent(HIGHLIGHT_JS_SCRIPT_PATH) + "</script>\n\n";
      } catch (IOException e) {
        log.warn("Failed to get content of highlight js, falling back to external link.", e);
      }
    }
    return HIGHLIGHT_JS_SCRIPT;
  }

  public static String getHighlightInitScript() {
    return HIGHLIGHT_JS_INIT;
  }

  public static String getHighlightCustomStyle() {
    return "\n<style>code { color: black } .hljs-string { color: DarkGreen } .hljs-number { color: MidnightBlue } "
        + ".hljs-built_in { color: Maroon } .hljs-literal { color: MidnightBlue }</style>\n";
  }

  public static void viewMdr(Ride gui, String title, String textContent) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();
    InoutComponent inout = gui.getInoutComponent();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          SEXP htmlContent = consoleComponent
              .runScript("library('se.alipsa:mdr')\n renderMdr(mdrContent)",
                  Collections.singletonMap("mdrContent", textContent));
          if (htmlContent != null) {
            inout.viewHtmlWithBootstrap(htmlContent, title);
          }
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
    consoleComponent.startThreadWhenOthersAreFinished(thread, "viewMdr");
  }

  public static String convertMdrToHtml(Ride gui, String textContent) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();

    Task<String> task = new Task<String>() {
      @Override
      public String call() throws Exception {
        try {
          SEXP htmlContent = consoleComponent
              .runScript("library('se.alipsa:mdr')\n renderMdr(mdrContent)",
                  Collections.singletonMap("mdrContent", textContent));
          if (htmlContent != null) {
            String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">\n"
                + getHighlightStyle(true)
                + getBootstrapStyle(true)
                + getHighlightCustomStyle()
                + "</head> <body>\n"
                + htmlContent.asString()
                + "\n</body>\n"
                + getHighlightJs(true)
                + getHighlightInitScript()
                + "</html>";
            return html;
          }
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
    consoleComponent.startThreadWhenOthersAreFinished(thread, "saveMdrAsPdf");
    try {
      return task.get();
    } catch (InterruptedException | ExecutionException e) {
      log.warn(e);
      return null;
    }
  }

  public static void saveMdrAsPdf(Ride gui, File target, String textContent) {
    se.alipsa.r2md.Md2Pdf.render(convertMdrToHtml(gui, textContent), target.getAbsolutePath());
  }

  public static void saveMdrAsHtml(Ride gui, File target, String textContent) {
    try {
      FileUtils.writeToFile(target, convertMdrToHtml(gui, textContent));
    } catch (FileNotFoundException e) {
      ExceptionAlert.showAlert(e.getMessage(), e);
    }
  }
}
