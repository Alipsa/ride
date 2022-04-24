package se.alipsa.ride.utils;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;

public class SemanticVersion implements Comparable<SemanticVersion> {

  //private static final Logger LOG = LogManager.getLogger();

  private final String versionString;

  public SemanticVersion(String version) {
   this.versionString = version;
  }

  @Override
  public int compareTo(@NotNull SemanticVersion another) {
    return new ComparableVersion(versionString).compareTo(new ComparableVersion(another.versionString));
  }

  public static int compare(@NotNull String first, String second) {
    if (first.startsWith("v")) {
      first = first.substring(1);
    }
    if (second.startsWith("v")) {
      second = second.substring(1);
    }
    if (first.contains("-jdk")) {
      first = first.substring(0, first.indexOf("-jdk"));
    }
    if (second.contains("-jdk")) {
      second = second.substring(0, second.indexOf("-jdk"));
    }
    //LOG.info("Comparing {} with {}" , first,  second);
    //return new SemanticVersion(first).compareTo(new SemanticVersion(second));
    return new ComparableVersion(first).compareTo(new ComparableVersion(second));
  }
}
