package se.alipsa.ride.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * We need to initialize config when using Tika otherwise we get warnings about optional dependencies not being available, e.g:
 * WARNING: J2KImageReader not loaded. JPEG2000 files will not be processed.
 */
public class TikaUtils {

  private static final Logger log = LogManager.getLogger();

  private static final TikaUtils INSTANCE = new TikaUtils();

  private org.apache.tika.Tika apacheTika;

  TikaConfig config;

  private TikaUtils() {
    /*
    <properties>
      <service-loader initializableProblemHandler="ignore"/>
    </properties>
     */
    Document doc = new DocumentImpl();
    Element root = doc.createElement("properties");
    Element serviceLoader = doc.createElement("service-loader");
    serviceLoader.setAttribute("initializableProblemHandler", "ignore");
    root.appendChild(serviceLoader);

    try {
      config = new TikaConfig(root);
    } catch (TikaException | IOException e) {
      throw new RuntimeException("Failed to configure Tika", e);
    }

    apacheTika = new org.apache.tika.Tika();
  }

  /**
   *
   * @param content the content as a byte array
   * @param context e.g. a file name
   * @return the most likely charset
   */
  public Charset detectCharset(byte[] content, String context) {
    CharsetMatch match = new CharsetDetector().setText(content).detect();
    log.debug("Charset for {} detected as {} with {}% confidence", context, match.getName(), match.getConfidence());
    return Charset.forName(match.getName());
  }

  public Charset detectCharset(File file) throws IOException {
    /* This is not as reliable as using the CharsetDetector so commenting it out
    try(InputStream is = TikaInputStream.get(Files.newInputStream(file.toPath()))) {
      return config.getEncodingDetector().detect(is, new Metadata());
    }
    */
    byte[] textBytes = FileUtils.readFileToByteArray(file);
    return detectCharset(textBytes, file.getName());
  }

  public String detectContentType(File file) throws IOException {
    return apacheTika.detect(file);
  }

  public static TikaUtils instance() {
    return INSTANCE;
  }
}
