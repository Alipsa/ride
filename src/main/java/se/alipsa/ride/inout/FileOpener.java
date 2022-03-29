package se.alipsa.ride.inout;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.model.MuninReport;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.TikaUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileOpener {

  private CodeComponent codeComponent;

  private static final Logger log = LogManager.getLogger(FileOpener.class);

  public FileOpener(CodeComponent codeComponent) {
    this.codeComponent = codeComponent;
  }

  public TextAreaTab openFile(File file, boolean... openExternalIfUnknownType) {

    final boolean allowOpenExternal = openExternalIfUnknownType.length <= 0 || openExternalIfUnknownType[0];

    String type = guessContentType(file);
    log.debug("File ContentType for {} detected as {}", file.getName(), type);
    if (file.isFile()) {
      String fileNameLower = file.getName().toLowerCase();
      if (strEndsWith(fileNameLower, ".r", ".s") || strEquals(type, "text/x-rsrc")) {
        return codeComponent.addTab(file, CodeType.R);
      }
      if (strEndsWith(fileNameLower, MuninReport.FILE_EXTENSION)) {
        return codeComponent.addTab(file, CodeType.MR);
      }
      if ( strEquals(type, "application/xml", "text/xml", "text/html")
                 || strEndsWith(type, "+xml")
                  // in case an xml declaration was omitted or empty file:
                 || strEndsWith(fileNameLower,".xml")
                 || strEndsWith(fileNameLower,".html")){
        return codeComponent.addTab(file, CodeType.XML);
      }
      if (strEndsWith(fileNameLower, ".java")) {
        return codeComponent.addTab(file, CodeType.JAVA);
      }
      if (strEquals(type, "text/x-groovy") || strEndsWith(fileNameLower, ".groovy", ".gvy", ".gy", ".gsh", ".gradle")) {
        return codeComponent.addTab(file, CodeType.GROOVY);
      }
      if (strEquals(type, "text/x-sql", "application/sql") || strEndsWith(fileNameLower, "sql")) {
        return codeComponent.addTab(file, CodeType.SQL);
      }
      if (strEndsWith(fileNameLower, ".js")
          || strEquals(type, "application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript")) {
        return codeComponent.addTab(file, CodeType.JAVA_SCRIPT);
      }
      if (strEndsWith(fileNameLower, ".md") || strEndsWith(fileNameLower, ".rmd")) {
        return codeComponent.addTab(file, CodeType.MD);
      }
      if (strEndsWith(fileNameLower, ".mdr")) {
        return codeComponent.addTab(file, CodeType.MDR);
      }
      if (strStartsWith(type, "text")
                 || strEquals(type, "application/x-bat",
          "application/x-sh", "application/json", "application/x-sas")
                 || "namespace".equals(fileNameLower)
                 || "description".equals(fileNameLower)
                 || strEndsWith(fileNameLower, ".txt", ".csv", ".gitignore", ".properties", "props")) {
        return codeComponent.addTab(file, CodeType.TXT);
      }
      if (allowOpenExternal && isDesktopSupported()) {
        log.info("Try to open {} in associated application", file.getName());
        openApplicationExternal(file);
      } else {
        Alerts.info("Unknown file type",
            "Unknown file type, not sure what to do with " + file.getName());
      }
    }
    return null;
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
      type = TikaUtils.instance().detectContentType(file);
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
    Task<Void> task = new Task<>() {
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
