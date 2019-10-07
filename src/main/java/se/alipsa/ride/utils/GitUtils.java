package se.alipsa.ride.utils;

import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import se.alipsa.ride.inout.FileItem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static se.alipsa.ride.Constants.GitStatus.*;

public class GitUtils {

   private static final Logger log = LogManager.getLogger();

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

   public static void colorNode(Git git, String path, TreeItem<FileItem> treeItem) {
      if (git == null || treeItem == null) {
         return;
      }
      try {
         Status status = git.status().addPath(path).call();
         if (status.getConflicting().size() > 0) {
            treeItem.getValue().setStyle(GIT_CONFLICT.getStyle());
            return;
         }
         if (status.getAdded().size() > 0) {
            treeItem.getValue().setStyle(GIT_ADDED.getStyle());
            return;
         }
         if (status.getChanged().size() > 0) {
            treeItem.getValue().setStyle(GIT_CHANGED.getStyle());
            return;
         }
         if (status.getModified().size() > 0) {
            treeItem.getValue().setStyle(GIT_MODIFIED.getStyle());
            return;
         }
         if (status.getUncommittedChanges().size() > 0) {
            treeItem.getValue().setStyle(GIT_UNCOMITTED_CHANGE.getStyle());
            return;
         }
         if (status.getUntracked().size() > 0) {
            treeItem.getValue().setStyle(GIT_UNTRACKED.getStyle());
            return;
         }
         if (status.getIgnoredNotInIndex().size() > 0) {
            treeItem.getValue().setStyle(GIT_IGNORED.getStyle());
            return;
         }
         treeItem.getValue().setStyle(GIT_NONE.getStyle());
      } catch (GitAPIException e) {
         log.warn("Failed to get git status to color node " + path, e);
         ExceptionAlert.showAlert("Failed to get git status to color node " + path, e);
      }
   }

   public static void storeCredentials(String url, String userName, String password) throws URISyntaxException, IOException {
      File gitCredentials = getCredentialsFile();

      URI remoteUri = new URI(url);
      URI credUri = new URI(remoteUri.getScheme(),
         userName + ":" + password,
         remoteUri.getHost(),
         remoteUri.getPort(),
         remoteUri.getPath(),
         remoteUri.getQuery(),
         remoteUri.getFragment());
      log.info("Storing {} to {}", credUri.toString(), gitCredentials.getAbsolutePath());
      FileUtils.appendToOrCreateFile(gitCredentials, " \n" + credUri.toString());
   }

   public static CredentialsProvider getStoredCredentials(String url) throws IOException, URISyntaxException {
      File gitCredentials = getCredentialsFile();
      if (!gitCredentials.exists()) {
         return null;
      }
      URI remoteUri = new URI(url);
      List<String> lines = Files.readAllLines(gitCredentials.toPath());
      for (String line : lines) {
         URI uri = new URI(line);
         String uriPath = uri.getPath();
         String remotePath = remoteUri.getPath();
        // if uri.getPath() == null it should always match, see https://git-scm.com/docs/gitcredentials
         if (uriPath == null) {
           uriPath = "";
           remotePath = "";
         }

         if (uri.getScheme().equals(remoteUri.getScheme())
            && uri.getHost().equals(remoteUri.getHost())
            && uriPath.equals(remotePath)) {

            String credentials = uri.getUserInfo();
            String[] credArr = credentials.split(":");
            if (credArr.length == 2) {
               return new UsernamePasswordCredentialsProvider(credArr[0], credArr[1]);
            }
         }
      }
      return null;
   }

   private static File getCredentialsFile() {
      return new File(System.getProperty("user.home"), ".git-credentials");
   }
}
