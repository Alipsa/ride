package utils;

import org.apache.commons.io.FileUtils;
import org.apache.tika.parser.txt.CharsetMatch;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.tika.parser.txt.CharsetDetector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EncodingTest {

  @Test
  public void testFileEncodingDetection() throws IOException {
    assertEquals(StandardCharsets.ISO_8859_1, tika(file("enc-iso8859-1.txt")));
    assertEquals(Charset.forName("windows-1252"), tika(file("enc_cp1252.txt")));
    assertEquals(StandardCharsets.UTF_16LE, tika(file("enc-utf16LE.txt")));
    assertEquals(StandardCharsets.UTF_16BE, tika(file("enc-utf16BE.txt")));
    assertEquals(StandardCharsets.UTF_8, tika(file("enc-utf8NoBOM.txt")));
    assertEquals(StandardCharsets.UTF_8, tika(file("enc-utf8.txt")));
  }


  private Charset tika(File file) throws IOException {
    byte[] textBytes = FileUtils.readFileToByteArray(file);
    CharsetDetector detector = new CharsetDetector().setText(textBytes);
    CharsetMatch match = detector.detect();
    System.out.println("Detected file " + file.getName() + " as " + match.getName() + " with " + match.getConfidence() + "% certainty");
    if (match.getConfidence() < 100) {
      StringBuilder sb = new StringBuilder();
      for (CharsetMatch m : detector.detectAll()) {
        if (!m.getName().equals(match.getName())) {
          sb.append("\n\t  ").append(m.getName())
              .append(" (").append(m.getConfidence()).append("%)");
        }
      }
      System.out.println("\tOther possible matches are: " + sb);
    }
    return Charset.forName(match.getName());
  }

  private File file(String resourceName) {
    URL url = getClass().getResource(resourceName);
    assertNotNull(url, "File " + resourceName + " cannot be found");
    return new File(url.getFile());
  }
}
