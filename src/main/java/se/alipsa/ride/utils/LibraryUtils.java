package se.alipsa.ride.utils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import org.apache.commons.io.IOUtils;
import se.alipsa.ride.model.RenjinLibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LibraryUtils {

  public static Set<RenjinLibrary> getAvailableLibraries() {
    Set<RenjinLibrary> packageNames = new HashSet<>();
    try (ScanResult scanResult = new ClassGraph().scan()) {
      scanResult.getResourcesWithLeafName("DESCRIPTION")
          .forEachByteArrayThrowingIOException((Resource res, byte[] fileContent) -> {
            RenjinLibrary renjinLibrary = parseDescription(new String(fileContent, StandardCharsets.UTF_8));
            if (renjinLibrary != null) {
              packageNames.add(renjinLibrary);
            }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
    return packageNames;
  }

  public static RenjinLibrary parseDescription(String content) {
    String packageName = null;
    String groupName = "";
    String title = "";
    String version = "";

    try (BufferedReader reader = new BufferedReader(new StringReader(content))){
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("Package: ")) {
          packageName = line.substring("Package: ".length());
        } else if (line.startsWith("GroupId: ")) {
          groupName = line.substring("GroupId: ".length());
        } else if (line.startsWith("Title: ")) {
          title = line.substring("Title: ".length());
        } else if (line.startsWith("Version: ")) {
          version = line.substring("Version: ".length());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return packageName == null ? null : new RenjinLibrary(title, groupName, packageName, version);
  }

  public static String extractPackageName(String content) {
    String packageName = null;
    String groupName = "";
    try (BufferedReader reader = new BufferedReader(new StringReader(content))){
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("Package: ")) {
          packageName = line.substring("Package: ".length());
        } else if (line.startsWith("GroupId: ")) {
          groupName = line.substring("GroupId: ".length()) + ":";
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return packageName == null ? null : groupName + packageName;
  }

  public static String extractPackageNameFromResource(String path) {
    URL url = FileUtils.getResourceUrl(path);
    if (url == null) {
      System.err.println("Failed to find " + path);
      return null;
    }
    try (InputStream is = url.openStream()){
      List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
      for ( String line : lines) {
        if (line.startsWith("Package: ")) {
          return line.substring("Package: ".length());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getPackage(String fullPackageName) {
    if (fullPackageName.contains(":")) {
      return fullPackageName.split(":")[1];
    }
    return fullPackageName;
  }

  public static String getGroup(String fullPackageName) {
    if (fullPackageName.contains(":")) {
      return fullPackageName.split(":")[0];
    }
    return "";
  }
}
