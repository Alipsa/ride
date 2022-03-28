package utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import se.alipsa.ride.utils.FileUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class FileUtilsTest {

  @Test
  public void testBaseName() {
    assertEquals("Foo.bat", FileUtils.baseName("C:\\jabaa\\dabba\\Foo.bat"));
    assertEquals("README.md", FileUtils.baseName("https://github.com/Alipsa/r2md/blob/main/README.md"));
    assertEquals("xing-yi-quan", FileUtils.baseName("https://sancai.se/?incsub_wiki=wikipages/xing-yi-quan"));
    assertEquals("https://sancai.se/?incsub_wiki=xing-yi-quan", FileUtils.baseName("https://sancai.se/?incsub_wiki=xing-yi-quan"));
  }
}
