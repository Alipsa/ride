package utils;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.utils.TikaUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContentTypeDetectionTest {

  @Test
  public void testContentTypeDetection() throws IOException, URISyntaxException {
    TikaUtils tikaUtils = TikaUtils.instance();
    String png = tikaUtils.detectContentType(getResource("plot.png"));
    assertEquals("image/png", png, "plot.png");
    String svg = tikaUtils.detectContentType(getResource("plot.svg"));
    assertEquals("image/svg+xml", svg, "plot.svg");
    String sql = tikaUtils.detectContentType(getResource("UTF16LE.sql"));
    assertEquals("text/x-sql", sql, "UTF16LE.sql");
    String txt = tikaUtils.detectContentType(getResource("utils/enc-utf8.txt"));
    assertEquals("text/plain", txt, "enc-utf8.txt");
  }

  private File getResource(String fileName) throws IOException, URISyntaxException {
    URL url = this.getClass().getClassLoader().getResource(fileName);
    assertNotNull(url, "Failed to find " + fileName);
    return new File(url.toURI().getPath());
  }
}
