package se.alipsa.ride.inout;

import javafx.concurrent.Task;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileOpener {

  private Tika contentProber = new Tika();
  private CodeComponent codeComponent;

  private Logger log = LoggerFactory.getLogger(FileOpener.class);

  public FileOpener(CodeComponent codeComponent) {
    this.codeComponent = codeComponent;
  }

  public void openFile(File file, boolean... openExternalIfUnknownType) {

    final boolean allowOpenExternal = openExternalIfUnknownType.length > 0 ? openExternalIfUnknownType[0] : true;

    String type = guessContentType(file);
    log.debug("File ContentType for {} detected as {}", file.getName(), type);
    if (file.isFile()) {
      String fileNameLower = file.getName().toLowerCase();
      if (strEndsWith(fileNameLower, ".r", ".s") || strEquals(type, "text/x-rsrc")) {
        codeComponent.addTab(file, CodeType.R);
      } else if ( strEquals(type, "application/xml", "text/xml")
                 || strEndsWith(type, "+xml")
                  // in case an xml declaration was omitted or empty file:
                 || strEndsWith(fileNameLower,".xml")) {
        codeComponent.addTab(file, CodeType.XML);
      } else if (strEndsWith(fileNameLower, ".java")) {
        codeComponent.addTab(file, CodeType.JAVA);
      } else if (strEquals(type, "text/x-sql", "application/sql") || strEndsWith(fileNameLower, "sql")) {
        codeComponent.addTab(file, CodeType.SQL);
      } else if (strStartsWith(type, "text")
                 || strEquals(type, "application/x-bat",
          "application/x-sh", "application/json", "application/x-sas")
                 || "namespace".equals(fileNameLower)
                 || strEndsWith(fileNameLower, ".txt", ".md", ".csv")) {
        codeComponent.addTab(file, CodeType.TXT);
      } else {
        if (allowOpenExternal && isDesktopSupported()) {
          log.info("Try to open {} in associated application", file.getName());
          openApplicationExternal(file);
        } else {
          Alerts.info("Unknown file type",
              "Unknown file type, not sure what to do with " + file.getName());
        }
      }
    }
  }

  private boolean isDesktopSupported() {
    try {
      return Desktop.isDesktopSupported();
    } catch (Exception e) {
      return false;
    }
  }

  private String guessContentType(File file) {
    final String unknown = "unknown";
    if (!file.exists() || file.length() == 0) {
      return unknown;
    }
    String type;
    try {
      //type = Files.probeContentType(file.toPath());
      type = contentProber.detect(file);
    } catch (IOException e) {
      e.printStackTrace();
      return unknown;
    }
    if (type != null) {
      return type;
    } else {
      return unknown;
    }
  }

  private void openApplicationExternal(File file) {
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Desktop.getDesktop().open(file);
        return null;
      }
    };

    task.setOnFailed(e -> ExceptionAlert.showAlert("Failed to open " + file, task.getException()));
    Thread appthread = new Thread(task);
    appthread.start();
  }

  private boolean strStartsWith(String fileNameLower, String... strOpt) {
    for (String start : strOpt) {
      if (fileNameLower.startsWith(start.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean strEndsWith(String fileNameLower, String... strOpt) {
    for (String end : strOpt) {
      if (fileNameLower.endsWith(end.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean strEquals(String fileNameLower, String... strOpt) {
    for (String str : strOpt) {
      if (fileNameLower.equalsIgnoreCase(str)) {
        return true;
      }
    }
    return false;
  }
}
