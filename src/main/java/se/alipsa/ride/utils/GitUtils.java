package se.alipsa.ride.utils;

import java.io.File;

public class GitUtils {

   public static String asRelativePath(File currentFile, File rootDir) {
      String root = rootDir.getAbsolutePath();
      String nodePath = currentFile.getAbsolutePath();
      String path = nodePath.replace(root, "").replace('\\', '/');
      if (path.length() <= 1) {
         return ".";
      }
      if (path.startsWith("/")) {
         return path.substring(1);
      }
      return path;
   }
}
