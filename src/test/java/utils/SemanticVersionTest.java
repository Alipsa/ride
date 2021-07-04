package utils;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.utils.SemanticVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SemanticVersionTest {

  @Test
  public void previousVersion() {
    assertEquals(-1, SemanticVersion.compare("1.2.3", "1.2.4"));
    assertEquals(-1, SemanticVersion.compare("1.2.4-beta1", "1.2.4-beta2"));
    assertEquals(-1, SemanticVersion.compare("1.2.4-beta", "1.2.4-beta1"));
    assertEquals(-1, SemanticVersion.compare("1.2.4-beta3", "1.2.4-GA"));
  }

  @Test
  public void sameVersion() {
    assertEquals(0, SemanticVersion.compare("1.2.4", "1.2.4"));
    assertEquals(0, SemanticVersion.compare("1.2.4-beta1", "1.2.4-beta1"));
    assertEquals(0, SemanticVersion.compare("1.2.4-beta", "1.2.4-beta"));
    assertEquals(0, SemanticVersion.compare("1.2.4-GA", "1.2.4-GA"));
  }

  @Test
  public void laterVersion() {
    assertEquals(1, SemanticVersion.compare("1.2.5", "1.2.4"));
    assertEquals(1, SemanticVersion.compare("1.2.4-beta3", "1.2.4-beta2"));
    assertEquals(1, SemanticVersion.compare("1.2.4-beta1", "1.2.4-beta"));
    assertEquals(1, SemanticVersion.compare("1.2.4-GA", "1.2.4-beta2"));
  }
}
