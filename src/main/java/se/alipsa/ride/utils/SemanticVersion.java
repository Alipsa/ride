package se.alipsa.ride.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private static final Logger LOG = LogManager.getLogger();

  public final int[] numbers;
  public final String tail;

  public SemanticVersion(String version) {
    String[] parts = version.split("-");
    String versionPart = parts[0];
    if (parts.length > 1) {
      tail = parts[1];
    } else {
      tail = "";
    }

    final String[] split = versionPart.split("\\.");
    numbers = new int[split.length];
    for (int i = 0; i < split.length; i++) {
      numbers[i] = Integer.parseInt(split[i]);
    }
  }

  @Override
  public int compareTo(SemanticVersion another) {
    final int maxLength = Math.max(numbers.length, another.numbers.length);
    for (int i = 0; i < maxLength; i++) {
      final int left = i < numbers.length ? numbers[i] : 0;
      final int right = i < another.numbers.length ? another.numbers[i] : 0;
      if (left != right) {
        return left < right ? -1 : 1;
      }
    }
    if (tail.equals(another.tail)) {
      return 0;
    }
    if ("GA".equals(tail)) {
      return 1;
    }
    if ("GA".equals(another.tail)) {
      return -1;
    }
    return tail.compareTo(another.tail);
  }

  public static int compare(String first, String second) {
    if (first.startsWith("v")) {
      first = first.substring(1);
    }
    if (second.startsWith("v")) {
      second = second.substring(1);
    }
    LOG.info("Comparing " + first + " with " + second);
    return new SemanticVersion(first).compareTo(new SemanticVersion(second));
  }
}
