package se.alipsa.ride.code.mdrtab;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Entities;
import org.renjin.sexp.SEXP;
import org.w3c.dom.Document;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class MdrUtil {

  private static final Logger log = LogManager.getLogger();

  private static final String HIGHLIGHT_JS_CSS_PATH = "highlightJs/default.css";
  private static final String HIGHLIGHT_JS_SCRIPT_PATH = "highlightJs/highlight.pack.js";
  private static final String BOOTSTRAP_CSS_PATH = "META-INF/resources/webjars/bootstrap/5.1.3/css/bootstrap.css";
  private static final String HIGHLIGHT_JS_INIT = "\n<script>hljs.initHighlightingOnLoad();</script>\n";
  // The highlightJs stuff is in the mdr package
  public static final String HIGHLIGHT_JS_CSS = "\n<link rel='stylesheet' href='" + resourceUrlExternalForm(HIGHLIGHT_JS_CSS_PATH) + "'>\n";
  public static final String HIGHLIGHT_JS_SCRIPT = "\n<script src='" + resourceUrlExternalForm(HIGHLIGHT_JS_SCRIPT_PATH) + "'></script>\n";
  public static final String BOOTSTRAP_CSS = resourceUrlExternalForm(BOOTSTRAP_CSS_PATH);

  private static String resourceUrlExternalForm(String resource) {
    URL url = FileUtils.getResourceUrl(resource);
    return url == null ? "" : url.toExternalForm();
  }

  public static String getBootstrapStyle(boolean embed) {
    if (embed) {
      try {
        // @charset directive is not allowed when embedding the stylesheet
        String css = FileUtils.readContent(BOOTSTRAP_CSS_PATH).replace("@charset \"UTF-8\";", "\n");
        return "\n<style>\n" + css + "\n</style>\n";
      } catch (IOException e) {
        log.warn("Failed to read content to embed, resort to external ref instead", e);
      }
    }
    return "<link rel='stylesheet' href='" + BOOTSTRAP_CSS + "'>";
  }

  public static String getHighlightStyle(boolean embed) {
    if (embed) {
      try {
        return "\n<style>\n" + FileUtils.readContent(HIGHLIGHT_JS_CSS_PATH) + "\n</style>\n";
      } catch (IOException e) {
        log.warn("Failed to get content of highlight css, falling back to external link.", e);
      }
    }
    return HIGHLIGHT_JS_CSS;
  }

  public static String getHighlightJs(boolean embed) {
    if (embed) {
      try {
        return "\n<script>" + FileUtils.readContent(HIGHLIGHT_JS_SCRIPT_PATH) + "</script>\n";
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
    gui.getInoutComponent().viewHtmlWithBootstrap(convertMdrToHtml(gui, textContent), title);
  }

  private static String convertMdrToHtml(Ride gui, String textContent) {
    final ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();
    Task<String> task = new Task<>() {
      @Override
      public String call() throws Exception {
        try {
          SEXP htmlContent = consoleComponent
              .runScript("library('se.alipsa:mdr')\n renderMdr(mdrContent)",
                  Collections.singletonMap("mdrContent", textContent));
          if (htmlContent != null) {
            return htmlContent.asString();
          }
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
        return null;
      }
    };
    task.setOnSucceeded(e -> consoleComponent.waiting());

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
    consoleComponent.startThreadWhenOthersAreFinished(thread, "convertMdrToHtml");
    try {
      return task.get();
    } catch (InterruptedException | ExecutionException e) {
      log.warn(e);
      return null;
    }
  }

  public static String decorate(String html, boolean withMargin, boolean embed) {
    return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n"
        + getHighlightStyle(true)
        + getBootstrapStyle(true)
        + getHighlightCustomStyle()
        + (withMargin ? "\n</head>\n<body style='margin-left: 15px; margin-right: 15px'>\n" : "\n</head>\n<body>\n")
        + html
        + "\n</body>\n"
        + getHighlightJs(true)
        + getHighlightInitScript()
        + "\n</html>";
  }

  public static void saveMdrAsPdf2(Ride gui, File target, String textContent) {
    String html = decorate(convertMdrToHtml(gui, textContent), false, true);
    se.alipsa.r2md.Md2Pdf.render(html, target.getAbsolutePath());
    try {
      FileUtils.writeToFile(new File(target.getParent(), target.getName() + ".html"), html);
    } catch (FileNotFoundException e) {
      log.warn("Failed to save html", e);
    }
    gui.getConsoleComponent().addWarning("saveMdrAsPdf", "\nPDF rendering is not faithful to the html\n", true);
  }

  public static void saveMdrAsPdf(Ride gui, File target, String textContent) {
    String html = decorate(convertMdrToHtml(gui, textContent), true, true);

    // We load the html into a web view so that the highlight javascript properly add classes to code parts
    // then we extract the DOM from the web view and use that to produce the PDF
    WebView webview = new WebView();
    final WebEngine webEngine = webview.getEngine();
    webEngine.setJavaScriptEnabled(true);
    webEngine.setUserStyleSheetLocation(BOOTSTRAP_CSS);
    webEngine.getLoadWorker().stateProperty().addListener(
        (ov, oldState, newState) -> {
          if (newState == Worker.State.SUCCEEDED) {
            Document doc = webEngine.getDocument();

            try(OutputStream os = Files.newOutputStream(target.toPath()))  {
              String viewContent = toString(doc);

              // For some reason the raw DOM document does not work, we have to parse it again with jsoup to get
              // something that the PdfRendererBuilder understands
              org.jsoup.nodes.Document doc2 = Jsoup.parse(viewContent);
              doc2.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
                  .escapeMode(Entities.EscapeMode.extended)
                  .charset(StandardCharsets.UTF_8)
                  .prettyPrint(false);
              Document doc3 = new W3CDom().fromJsoup(doc2);
              PdfRendererBuilder builder = new PdfRendererBuilder()
                  .withW3cDocument(doc3, new File(".").toURI().toString())
                  .toStream(os);
              builder.run();

              FileUtils.writeToFile(new File(target.getParent(), target.getName() + ".html"), toString(doc3));
              gui.getConsoleComponent().addWarning("saveMdrAsPdf", "\nNote: PDF rendering has issues with non-latin1 characters and margins in code blocks\n", true);
            } catch (Exception e) {
              ExceptionAlert.showAlert("Failed to create PDF", e);
            }
          }
        });
    webEngine.loadContent(html);
  }

  @NotNull
  private static String toString(Document doc) throws TransformerException {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    StringWriter sw = new StringWriter();
    transformer.transform(new DOMSource(doc), new StreamResult(sw));
    return sw.toString();
  }


  public static void saveMdrAsHtml(Ride gui, File target, String textContent) {
    try {
      String html = convertMdrToHtml(gui, textContent);
      FileUtils.writeToFile(target, decorate(html, true, true));
    } catch (FileNotFoundException e) {
      ExceptionAlert.showAlert(e.getMessage(), e);
    }
  }
}
