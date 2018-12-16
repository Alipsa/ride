package se.alipsa.renjinstudio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils {
  private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

  /**
   * Gets a reference to a file or folder in the classpath. Useful for getting test resources and
   * other similar artifacts.
   * 
   * @param name the name of the resource, use / to separate path entities.
   *             Do NOT lead with a "/" unless you know what you are doing.
   * @param encodingOpt optional encoding if something other than UTF-8 is needed.
   * @return The resource as a file.
   * @throws FileNotFoundException if the resource cannot be found.
   */
  public static File getResource(String name, String... encodingOpt) throws FileNotFoundException {
    String encoding = encodingOpt.length > 0 ? encodingOpt[0] : "UTF-8";
    URL url = getResourceUrl(name);
    File file;
    try {
      file = new File(URLDecoder.decode(url.getFile(), encoding));
    } catch (UnsupportedEncodingException e) {
      throw new FileNotFoundException("Failed to find resource " + name + ", url is " + url);
    }
    return file;
  }

  /**
   * Find a resource using available class loaders. 
   * It will also load resources/files from the 
   * absolute path of the file system (not only the classpath's).
   */
  public static URL getResourceUrl(String resource) {
    final List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
    classLoaders.add(Thread.currentThread().getContextClassLoader());
    classLoaders.add(FileUtils.class.getClassLoader());

    for (ClassLoader classLoader : classLoaders) {
      final URL url = getResourceWith(classLoader, resource);
      if (url != null) {
        return url;
      }
    }

    final URL systemResource = ClassLoader.getSystemResource(resource);
    if (systemResource != null) {
      return systemResource;
    } else {
      try {
        return new File(resource).toURI().toURL();
      } catch (MalformedURLException e) {
        return null;
      }
    }
  }

  private static URL getResourceWith(ClassLoader classLoader, String resource) {
    if (classLoader != null) {
      return classLoader.getResource(resource);
    }
    return null;
  }


  /**
   * finds the dir where files are located (recursing down the tree).
   * 
   * @param dir the starting dir
   * @param ext the file extension to look for
   * @return the dir where there are files
   */
  public static File findDirWithExt(File dir, String ext) {
    logger.debug("Looking for " + ext + " in " + dir);
    if (dir == null) {
      throw new IllegalArgumentException("dir parameter cannot be null!");
    }
    File targetDir = null;
    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().toLowerCase().endsWith(ext)) {
          targetDir = file.getAbsoluteFile().getParentFile();
          return targetDir;
        }
      } else if (file.isDirectory() && targetDir == null) {
        targetDir = findDirWithExt(file, ext);
      }
    }
    return targetDir;
  }

  /** fetch a list of files for the dir specified and the extension specified
   * extension in not case sensitive
   */
  public static List<File> findFilesWithExt(File dir, String ext) throws IOException {
    if (dir == null || ext == null) {
      return null;
    }
    File[] files = dir.listFiles();
    if (files == null) {
      return null;
    }
    return Arrays.stream(files)
        .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(ext.toLowerCase()))
        .collect(Collectors.toList());
  }

  /**
   * Copy a file to a dir.
   * 
   * @param from the file to copy
   * @param toDir the destination dir
   * @return the copied file
   * @throws IOException if an IO issue occurs
   */
  public static File copy(File from, File toDir) throws IOException {
    File destFile = new File(toDir, from.getName());
    if (!from.exists()) {
      throw new IOException("File " + from.getAbsolutePath() + " does not exist");
    }
    if (toDir.exists() && toDir.isFile()) {
      throw new IllegalArgumentException("Target must be a directory");
    }

    if (destFile.exists()) {
      logger.info("File " + destFile.getAbsolutePath() + " already exists");
      return destFile;
    }
    toDir.mkdirs();
    destFile.createNewFile();
    Files.copy(from.toPath(), new FileOutputStream(destFile));
    return destFile;
  }

  /**
   * Copy a file to a dir.
   *
   * @param from the file to copy
   * @param toFile the destination file
   * @param overwriteOpt whether to overwrite existing file or not, default true
   * @return the copied file
   * @throws IOException if an IO issue occurs
   */
  public static File copyFile(File from, File toFile, boolean... overwriteOpt) throws IOException {
    if (!from.exists()) {
      throw new IOException("File " + from.getAbsolutePath() + " does not exist");
    }
    boolean overwrite = overwriteOpt.length > 0 ? overwriteOpt[0] : true;
    if (!overwrite && toFile.exists()) {
      throw new IOException("File " + toFile.getAbsolutePath() + " already exists");
    }
    Files.copy(from.toPath(), new FileOutputStream(toFile));
    return toFile;
  }

  /**
   * Find the first file matching the pattern.
   * 
   * @param dir the dir to search
   * @param prefix the pattern to match
   * @param suffix the pattern to match
   * @return the first file matching the pattern
   */
  public static File findFirst(File dir, String prefix, String suffix) {
    if (dir == null || !dir.exists()) {
      return null;
    }
    File[] files = dir.listFiles();
    for (File file : files) {
      if (file.isFile()) {
        if (file.getName().startsWith(prefix) && file.getName().endsWith(suffix)) {
          return file;
        }
      }
    }
    return null;
  }

  /** recursive delete on exit. */
  public static void deleteOnExit(File dir) throws IOException { 
    if (dir == null) {
      return;
    }
    dir.deleteOnExit();
    Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        file.toFile().deleteOnExit();
        return FileVisitResult.CONTINUE;
      }
      
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        dir.toFile().deleteOnExit();
        return FileVisitResult.CONTINUE;
      }
    });
    
  }


}
