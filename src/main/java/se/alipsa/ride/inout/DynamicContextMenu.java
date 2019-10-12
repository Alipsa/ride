package se.alipsa.ride.inout;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import se.alipsa.ride.Ride;
import se.alipsa.ride.inout.git.*;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.GitUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static se.alipsa.ride.Constants.REPORT_BUG;
import static se.alipsa.ride.utils.GitUtils.asRelativePath;

public class DynamicContextMenu extends ContextMenu {

   private Git git;
   private Ride gui;
   private FileTree fileTree;
   private TreeItem<FileItem> currentNode;
   private File currentFile;
   private CredentialsProvider credentialsProvider;

   MenuItem gitInitMI = new MenuItem("Initialize root as git repo");

   private Logger log = LogManager.getLogger();

   public DynamicContextMenu(FileTree fileTree, Ride gui) {
      this.fileTree = fileTree;
      this.gui = gui;
      credentialsProvider = null;

      MenuItem copyMI = new MenuItem("copy name");
      copyMI.setOnAction(e -> fileTree.copySelectionToClipboard());
      getItems().add(copyMI);

      MenuItem createDirMI = new MenuItem("create dir");
      createDirMI.setOnAction(e -> {
         File currentFile = currentNode.getValue().getFile();
         File newDir = promptForFile(currentFile, "Create and add dir", "Enter the dir name:");
         if (newDir == null) {
            return;
         }
         try {
            Files.createDirectory(newDir.toPath());
            fileTree.addTreeNode(newDir);
         } catch (IOException e1) {
            ExceptionAlert.showAlert("Failed to create directory", e1);
         }
      });
      getItems().add(createDirMI);

      MenuItem createFileMI = new MenuItem("create file");
      createFileMI.setOnAction(e -> {
         File newFile = promptForFile(currentFile, "Create and add file", "Enter the file name:");
         if (newFile == null) {
            return;
         }
         try {
            Files.createFile(newFile.toPath());
            if (newFile.getName().endsWith(".java")) {
               addJavaContent(newFile);
            }
            TreeItem<FileItem> node = fileTree.addTreeNode(newFile);
            GitUtils.colorNode(git, GitUtils.asRelativePath(newFile, fileTree.getRootDir()), node);
         } catch (IOException e1) {
            ExceptionAlert.showAlert("Failed to create file", e1);
         }
      });
      getItems().add(createFileMI);


      MenuItem expandAllMI = new MenuItem("expand all");
      expandAllMI.setOnAction(e -> fileTree.expandAllChildren(currentNode));
      getItems().add(expandAllMI);

      MenuItem deleteMI = new MenuItem("delete");
      deleteMI.setOnAction(e -> {
         String fileType = "file";
         try {
            if (currentFile.isDirectory()) {
               fileType = "directory";
            }
            Files.delete(currentFile.toPath());
            currentNode.getParent().getChildren().remove(currentNode);
         } catch (DirectoryNotEmptyException ex) {
            ExceptionAlert.showAlert("Directory is not empty, cannot delete ", ex);
         } catch (IOException ex) {
            ExceptionAlert.showAlert("Failed to delete " + fileType, ex);
         }
      });
      getItems().add(deleteMI);

      Menu gitMenu = new Menu("Git");

      boolean gitInitialized = false;

      if (new File(fileTree.getRootDir(), ".git").exists()) {
         try {
            git = Git.open(fileTree.getRootDir());
            gitInitMI.setVisible(false);
            gitInitialized = true;
         } catch (IOException e) {
            log.error("Failed to open git repository at {}", fileTree.getRootDir(), e);
         }
      } else {
         gitInitMI.setVisible(true);
         gitInitMI.setOnAction(this::gitInit);
         gitMenu.getItems().add(gitInitMI);
         gitInitialized = false;
      }

      if (gitInitialized) {
         MenuItem gitAddMI = new MenuItem("add");
         gitAddMI.setOnAction(this::gitAdd);
         gitMenu.getItems().add(gitAddMI);

         MenuItem gitDeleteMI = new MenuItem("delete");
         gitDeleteMI.setOnAction(this::gitRm);
         gitMenu.getItems().add(gitDeleteMI);

         MenuItem gitStatusMI = new MenuItem("status");
         gitStatusMI.setOnAction(this::gitStatus);
         gitMenu.getItems().add(gitStatusMI);

         MenuItem gitDiffMI = new MenuItem("diff");
         gitDiffMI.setOnAction(this::gitDiff);
         gitMenu.getItems().add(gitDiffMI);

         MenuItem gitResetMI = new MenuItem("reset");
         gitResetMI.setOnAction(this::gitReset);
         gitMenu.getItems().add(gitResetMI);

         // All sub menu
         Menu gitAllMenu = new Menu("All");
         gitMenu.getItems().add(gitAllMenu);

         MenuItem gitAddAllMI = new MenuItem("add all");
         gitAddAllMI.setOnAction(this::gitAddAll);
         gitAllMenu.getItems().add(gitAddAllMI);

         MenuItem gitCommitMI = new MenuItem("commit");
         gitCommitMI.setOnAction(this::gitCommit);
         gitAllMenu.getItems().add(gitCommitMI);

         MenuItem gitStatusAllMI = new MenuItem("status all");
         gitStatusAllMI.setOnAction(this::gitStatusAll);
         gitAllMenu.getItems().add(gitStatusAllMI);

         MenuItem gitLogMI = new MenuItem("show Log");
         gitLogMI.setOnAction(this::gitLog);
         gitAllMenu.getItems().add(gitLogMI);

         MenuItem gitConfigMI = new MenuItem("configure");
         gitConfigMI.setOnAction(this::gitConfig);
         gitAllMenu.getItems().add(gitConfigMI);

         // Branches sub menu
         Menu gitBranchMenu = new Menu("Branches");
         gitMenu.getItems().add(gitBranchMenu);

         MenuItem gitBranchListMI = new MenuItem("list branches");
         gitBranchListMI.setOnAction(this::gitBranchList);
         gitBranchMenu.getItems().add(gitBranchListMI);

         MenuItem gitBranchCheckoutMI = new MenuItem("checkout");
         gitBranchCheckoutMI.setOnAction(this::gitBranchCheckout);
         gitBranchMenu.getItems().add(gitBranchCheckoutMI);

         MenuItem gitBranchMergeMI = new MenuItem("merge");
         gitBranchMergeMI.setOnAction(this::gitBranchMerge);
         gitBranchMenu.getItems().add(gitBranchMergeMI);

         // Remote sub menu
         Menu gitRemoteMenu = new Menu("Remote");
         gitMenu.getItems().add(gitRemoteMenu);

         MenuItem gitAddRemoteMI = new MenuItem("add remote");
         gitAddRemoteMI.setOnAction(this::gitAddRemote);
         gitRemoteMenu.getItems().add(gitAddRemoteMI);

         MenuItem gitPushMI = new MenuItem("push");
         gitPushMI.setOnAction(this::gitPush);
         gitRemoteMenu.getItems().add(gitPushMI);

         MenuItem gitPullMI = new MenuItem("pull");
         gitPullMI.setOnAction(this::gitPull);
         gitRemoteMenu.getItems().add(gitPullMI);
      }
      getItems().add(gitMenu);
   }

   private void addJavaContent(File newFile) {
      if (newFile == null) {
         return;
      }
      String absolutePath = newFile.getAbsolutePath();
      String fileName = newFile.getName();
      int fromIndex = absolutePath.lastIndexOf("java/") + 5;
      if (fromIndex == 4) {
         fromIndex = absolutePath.lastIndexOf("src/") + 3;
      }
      if (fromIndex == 2) {
         fromIndex = absolutePath.indexOf(GitUtils.asRelativePath(newFile, fileTree.getRootDir()));
      }
      String packageName = absolutePath.substring(fromIndex, absolutePath.lastIndexOf(fileName)-1)
         .replace('/', '.');

      StringBuilder str = new StringBuilder("package ").append(packageName).append(";\n\n")
         .append("public class ").append(fileName.substring(0, fileName.indexOf(".java"))).append(" {\n\n}");
      try {
         FileUtils.writeToFile(newFile, str.toString());
      } catch (FileNotFoundException e) {
         log.warn("Failed to create java boilerplate content", e);
      }
   }

   private void gitBranchMerge(ActionEvent actionEvent) {
      try {
         if (!git.status().call().isClean()) {
            Alerts.info("Repository is not clean",
               "You have uncommitted files that must be committed before you can merge");
            return;
         }
      } catch (GitAPIException e) {
         log.warn("Failed to check status before merging with another branch", e);
         ExceptionAlert.showAlert("Failed to check status before merging with another branch", e);
         return;
      }
      String currentBranch;
      try {
         currentBranch = git.getRepository().getBranch();
      } catch (IOException e) {
         log.warn("Failed to get current branch", e);
         ExceptionAlert.showAlert("Failed to get current branch", e);
         return;
      }

      TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Merge branch");
      dialog.setHeaderText("Merge another branch into current branch (" + currentBranch + ")");
      dialog.setContentText("Branch to merge from:");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
         String branchName = result.get();
         try {
            // retrieve the objectId of the latest commit on branch
            ObjectId latestCommit = git.getRepository().resolve(branchName);
            MergeResult mergeResult = git.merge().include(latestCommit).call();
            if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
               StringBuilder str = new StringBuilder();
               mergeResult.getConflicts().forEach((k, v) -> str.append(k).append(": ").append(Arrays.deepToString(v)).append("\n"));
               Alerts.warn("Merge Conflicts detected", str.toString());
            } else if (mergeResult.getMergeStatus().isSuccessful()) {
               StringBuilder mergeContent = new StringBuilder("The following commits was merged:\n");
               for (ObjectId objectId : mergeResult.getMergedCommits()) {
                  mergeContent.append(objectId).append("\n");
               }
               Alerts.info("Merge success", mergeContent.toString());
            } else {
               StringBuilder str = new StringBuilder();
               mergeResult.getFailingPaths().forEach((k, v) -> str.append(k).append(": ").append(v.toString()).append("\n"));
               Alerts.warn("Merge failed", str.toString());
            }
            fileTree.refresh();
         } catch (Exception e) {
            log.warn("Failed to merge branch", e);
            ExceptionAlert.showAlert("Failed to merge branch", e);
         }
      }
   }

   private void gitBranchCheckout(ActionEvent actionEvent) {
      try {
         if (!git.status().call().isClean()) {
            Alerts.info("Repository is not clean",
               "You have uncommitted files that must be committed before you can checkout");
            return;
         }
      } catch (GitAPIException e) {
         log.warn("Failed to check status before checkout branch", e);
         ExceptionAlert.showAlert("Failed to check status before checkout branch", e);
      }
      TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Checkout branch");
      dialog.setHeaderText("Checkout branch");
      dialog.setContentText("Branch name:");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
         String branchName = result.get();
         try {
            List<Ref> branchList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            Ref branchExists = branchList.stream().filter(p -> p.getName().replace("refs/heads/", "").equals(branchName))
               .findAny().orElse(null);
            boolean createBranch = branchExists == null;
            git.checkout()
               .setCreateBranch(createBranch)
               .setName(branchName).call();
            fileTree.refresh();
         } catch (GitAPIException e) {
            log.warn("Failed to checkout branch", e);
            ExceptionAlert.showAlert("Failed to checkout branch", e);
         }
      }
   }

   private void gitBranchList(ActionEvent actionEvent) {
      try {
         List<Ref> branchRefs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
         StringBuilder str = new StringBuilder();
         for (Ref ref : branchRefs) {
            str.append(ref.getName().replace("refs/heads/", "")).append(": ").append(ref.getObjectId().name()).append("\n");
         }
         Alerts.info("Branches", str.toString());
      } catch (GitAPIException e) {
         log.warn("Failed to get branches", e);
         ExceptionAlert.showAlert("Failed to get branches", e);
      }
   }

   private void gitLog(ActionEvent actionEvent) {
      try {
         Iterable<RevCommit> log = git.log().call();
         StringBuilder str = new StringBuilder();
         for (RevCommit rc : log) {
            str.append(LocalDateTime.ofEpochSecond(rc.getCommitTime(), 0, ZoneOffset.UTC))
               .append(", ")
               .append(rc.toString())
               .append(": ").append(rc.getFullMessage())
               .append("\n");
         }
         Alerts.info("Git log", str.toString());
      } catch (GitAPIException e) {
         log.warn("Failed to get log", e);
         ExceptionAlert.showAlert("Failed to get log", e);
      }
   }

   private void gitReset(ActionEvent actionEvent) {
      try {
         git.reset().addPath(getRelativePath()).call();
         fileTree.refresh();
      } catch (GitAPIException e) {
         log.warn("Failed to reset", e);
         ExceptionAlert.showAlert("Failed to reset", e);
      }
   }

   private void gitAddAll(ActionEvent actionEvent) {
      try {
         StatusCommand statusCommand = git.status();
         Status status = statusCommand.call();
         for (String path : status.getUncommittedChanges()) {
            git.add().addFilepattern(path).call();
            log.info("Adding changed file " + path);
         }
         for (String path : status.getUntracked()) {
            git.add().addFilepattern(path).call();
            log.info("Adding untracked file " + path);
         }
         fileTree.refresh();
      } catch (GitAPIException e) {
         log.warn("Failed to add all", e);
         ExceptionAlert.showAlert("Failed to add all", e);
      }
   }

   private void gitDiff(ActionEvent actionEvent) {
      try (StringWriter writer = new StringWriter();
           OutputStream out = new WriterOutputStream(writer, StandardCharsets.UTF_8)) {
         DiffCommand diffCommand = git.diff();
         diffCommand.setOutputStream(out);
         String path = getRelativePath();
         diffCommand.setPathFilter(PathFilter.create(path));
         List<DiffEntry> diffs = diffCommand.call();
         StringBuilder str = new StringBuilder();
         if (diffs.size() > 0) {
            diffs.forEach(e -> str.append(e.toString()).append("\n"));
            str.append(writer.toString());
         } else {
            str.append("No differences detected for ").append(path);
         }
         Alerts.info("Diff against repo for " + path, str.toString());
      } catch (GitAPIException | IOException e) {
         log.warn("Failed to diff", e);
         ExceptionAlert.showAlert("Failed to execute diff", e);
      }
   }

   private String getRelativePath() {
      return asRelativePath(currentFile, fileTree.getRootDir());
   }

   private void gitStatus(ActionEvent actionEvent) {
      try {
         StatusCommand statusCommand = git.status();
         String path = getRelativePath();
         statusCommand.addPath(path);
         Status status = statusCommand.call();
         GitStatusDialog statusDialog = new GitStatusDialog(status, path);
         statusDialog.show();
      } catch (GitAPIException e) {
         log.warn("Failed to get status", e);
         ExceptionAlert.showAlert("Failed to get status", e);
      }
   }

   private void gitStatusAll(ActionEvent actionEvent) {
      try {
         StatusCommand statusCommand = git.status();
         Status status = statusCommand.call();
         StringBuilder str = new StringBuilder("<h2>Git Status</h2>");

         Set<String> added = status.getAdded();
         if (added.size() > 0) {
            str.append("<b>Added:</b> ").append(String.join(", ", added)).append("<br/>");
         }
         Set<String> changed = status.getChanged();
         if (changed.size() > 0) {
            str.append("<br/><b>Changed:</b> ").append(String.join(", ", changed)).append("<br/>");
         }
         Set<String> conflicting = status.getConflicting();
         if (conflicting.size() > 0) {
            str.append("<br/><b>Conflicting:</b> ").append(String.join(", ", conflicting)).append("<br/>");
         }
         Set<String> missing = status.getMissing();
         if (missing.size() > 0) {
            str.append("<br/><b>Missing:</b> ").append(String.join(", ", missing)).append("<br/>");
         }
         Set<String> modified = status.getModified();
         if (modified.size() > 0) {
            str.append("<br/><b>Modified:</b> ").append(String.join(", ", modified)).append("<br/>");
         }
         Set<String> removed = status.getRemoved();
         if (removed.size() > 0) {
            str.append("<br/><b>Removed:</b> ").append(String.join(", ", removed)).append("<br/>");
         }
         Set<String> uncomittedChanges = status.getUncommittedChanges();
         if (uncomittedChanges.size() > 0) {
            str.append("\n<b>Uncommited changes:</b> ").append(String.join(", ", uncomittedChanges)).append("<br/>");
         }
         Set<String> untracked = status.getUntracked();
         if (untracked.size() > 0) {
            str.append("<br/><b>Untracked:</b> ").append(String.join(", ", untracked)).append("<br/>");
         }
         str.append("<br/><b>hasUncommittedChanges:</b> ").append(status.hasUncommittedChanges()).append("<br/>");
         str.append("<b>isClean:</b> ").append(status.isClean()).append("<br/>");
         Alerts.infoStyled("Status", str.toString());
      } catch (GitAPIException e) {
         log.warn("Failed to get status", e);
         ExceptionAlert.showAlert("Failed to get status", e);
      }
   }

   private void gitPull(ActionEvent actionEvent) {
      gui.setWaitCursor();
      Platform.runLater(() -> {
         try {
            String url = getRemoteGitUrl();
            credentialsProvider = GitUtils.getStoredCredentials(url);
            PullResult pullResult = git.pull().setCredentialsProvider(credentialsProvider).call();
            log.info(pullResult.toString());
            gui.setNormalCursor();
            Alerts.info("Git pull", pullResult.toString());
         } catch (TransportException e) {
            handleTransportException(e, "pull");
         } catch (Exception e) {
            log.warn("Failed to pull", e);
            gui.setNormalCursor();
            ExceptionAlert.showAlert("Failed to pull", e);
         }
      });
   }

   private void gitAddRemote(ActionEvent actionEvent) {
      AddRemoteDialog ard = new AddRemoteDialog();
      Optional<Map<AddRemoteDialog.KEY, String>> result = ard.showAndWait();
      if (result.isPresent()) {
         String name = result.get().get(AddRemoteDialog.KEY.NAME);
         URIish uri;
         try {
            uri = new URIish(result.get().get(AddRemoteDialog.KEY.URI));
         } catch (URISyntaxException e) {
            ExceptionAlert.showAlert("Invalid uri", e);
            return;
         }
         try {
            git.remoteAdd()
               .setName(name)
               .setUri(uri).call();
         } catch (GitAPIException e) {
            log.warn("Failed to add remote", e);
            ExceptionAlert.showAlert("Failed to add remote", e);
         }
      }
   }

   private void gitPush(ActionEvent actionEvent) {
      gui.setWaitCursor();
      Platform.runLater(() -> {
         try {
            credentialsProvider = GitUtils.getStoredCredentials(getRemoteGitUrl());
            Iterable<PushResult> result = git.push().setCredentialsProvider(credentialsProvider).call();
            log.info("Git push was successful: {}", result);
            StringBuilder str = new StringBuilder();
            for (PushResult pushResult : result) {
               pushResult.getRemoteUpdates().forEach(u ->
                  str.append(u.toString()).append("\n"));
            }
            gui.setNormalCursor();
            Alerts.info("Git push", "Git push was successful!\n" + str.toString());
         } catch (TransportException e) {
            handleTransportException(e, "push");
         } catch (Exception e) {
            log.warn("Failed to push", e);
            gui.setNormalCursor();
            ExceptionAlert.showAlert("Failed to push", e);
         }
      });
   }

   private void handleTransportException(TransportException e, String operation) {
      gui.setNormalCursor();
      log.info("Error pulling from remote");
      // TODO: check if it is an ssl problem
      List<Class> causes = new ArrayList<>();
      Throwable cause = e.getCause();
      while (cause != null) {
         log.debug("Cause is {}", cause.toString());
         causes.add(cause.getClass());
         cause = cause.getCause();
      }
      if (causes.contains(javax.net.ssl.SSLHandshakeException.class)) {
         handleSslValiationProblem(e, operation);
      } else if (e.getMessage().contains("Authentication is required but no CredentialsProvider has been registered")) {

         CredentialsDialog credentialsDialog = new CredentialsDialog();
         Optional<Map<CredentialsDialog.KEY, String>> res = credentialsDialog.showAndWait();
         if (res.isPresent()) {
            Map<CredentialsDialog.KEY, String> creds = res.get();
            String userName = creds.get(CredentialsDialog.KEY.NAME);
            String password = creds.get(CredentialsDialog.KEY.PASSWORD);
            credentialsProvider = new UsernamePasswordCredentialsProvider(userName, password);
            boolean store = Boolean.parseBoolean(creds.get(CredentialsDialog.KEY.STORE_CREDENTIALS));
            if (store) {
               String url = getRemoteGitUrl();
               try {
                  GitUtils.storeCredentials(url, userName, password);
               } catch (Exception ex) {
                  ExceptionAlert.showAlert("Failed to store credentials", ex);
               }
            }
            //Alerts.info("Credentials set", "Credentials set, please try again!");
         }
      } else {
         ExceptionAlert.showAlert("An unrecognized remote exception occurred. " + REPORT_BUG, e);
         return;
      }
      if ("push".equals(operation)) {
         gitPush(null);
      } else if ("pull".equals(operation)) {
         gitPull(null);
      } else {
         Alerts.warn(
            "Unknown operation when calling handleTransportException",
            operation + " is an unknown operation. " + REPORT_BUG
         );
      }

   }

   private void handleSslValiationProblem(TransportException e, String operation) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Failed to " + operation);
      alert.setContentText(e.toString() + "\n\nDo you want to disable ssl verification?");
      alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
      Optional<ButtonType> result = alert.showAndWait();
      result.ifPresent(type -> {
         log.info("promptForDisablingSslValidation, choice was {}", result.get());
         if (ButtonType.YES == type) {
            String url = getRemoteGitUrl();
            log.info("disabling sslVerify for {}...", url);

            try {
               StoredConfig config = git.getRepository().getConfig();
               config.setBoolean("http", url, "sslVerify", false);
               config.save();
               //Alerts.info("sslVerify set to false", "OK, try again!");
            } catch (Exception ex) {
               ExceptionAlert.showAlert("Failed to save config", ex);
            }
         }
      });
   }

   private String getRemoteGitUrl() {
      return git.getRepository().getConfig().getString("remote", "origin", "url");
   }

   private void gitRm(ActionEvent actionEvent) {
      String currentPath = getRelativePath();
      log.info("Deleting {}", currentPath);
      try {
         DirCache dc = git.rm().addFilepattern(currentPath).call();
         log.info("Removed {} from git dir cache", currentPath);
      } catch (GitAPIException e) {
         log.warn("Failed to delete " + currentPath, e);
         ExceptionAlert.showAlert("Failed to delete " + currentPath, e);
      }
   }

   private void gitCommit(ActionEvent actionEvent) {
      TextInputDialog td = new TextInputDialog();
      td.setHeaderText("Enter commit message");
      final Optional<String> result = td.showAndWait();
      if (result.isPresent()) {
         String msg = td.getEditor().getText();
         if (StringUtils.isBlank(msg)) {
            Alerts.info("Empty message", "Commit message cannot be empty");
            return;
         }
         gui.setWaitCursor();
         Platform.runLater(() -> {
            try {
               CommitCommand commit = git.commit();
               RevCommit revCommit = commit.setMessage(msg).call();
               log.info("Commited result: {}", revCommit);
               fileTree.refresh();
               gui.setNormalCursor();
            } catch (GitAPIException e) {
               log.warn("Failed to commit ", e);
               gui.setNormalCursor();
               ExceptionAlert.showAlert("Failed to commit ", e);
            }
         });
      }
   }

   private void gitAdd(ActionEvent actionEvent) {
      String currentPath = getRelativePath();
      try {
         DirCache dc = git.add().addFilepattern(currentPath).call();
         log.info("Added {} to git dir cache, node is {}", currentPath, currentNode.getValue().getText());
         GitUtils.colorNode(git, currentPath, currentNode);
      } catch (GitAPIException e) {
         log.warn("Failed to add " + currentPath, e);
         ExceptionAlert.showAlert("Failed to add " + currentPath, e);
      }
   }

   private void gitInit(ActionEvent actionEvent) {
      try {
         git = Git.init().setDirectory(fileTree.getRootDir()).call();
         StoredConfig config = git.getRepository().getConfig();
         // Use input as the default
         config.setEnum(ConfigConstants.CONFIG_CORE_SECTION, null,
            ConfigConstants.CONFIG_KEY_AUTOCRLF, CoreConfig.AutoCRLF.INPUT);
         config.save();
         String gitIgnoreTemplate = "templates/.gitignore";
         File gitIgnore = new File(fileTree.getRootDir(), ".gitignore");
         if (gitIgnore.exists()) {
            String content = FileUtils.readContent(gitIgnore);
            if (!content.contains("/target")) {
               FileUtils.writeToFile(gitIgnore, content + "\n" + FileUtils.readContent(gitIgnoreTemplate));
            }
         } else {
            FileUtils.copy(gitIgnoreTemplate, fileTree.getRootDir());
         }
         fileTree.refresh();
      } catch (GitAPIException | IOException e) {
         log.warn("Failed to initialize git in " + fileTree.getRootDir().getAbsolutePath(), e);
         ExceptionAlert.showAlert("Failed to initialize git in " + fileTree.getRootDir().getAbsolutePath(), e);
      }
   }


   private void gitConfig(ActionEvent actionEvent) {
      GitConfigureDialog dialog = new GitConfigureDialog(git);
      Optional<ConfigResult> result = dialog.showAndWait();
      if (result.isPresent()) {
         ConfigResult res = result.get();
         StoredConfig config = git.getRepository().getConfig();
         config.setEnum(ConfigConstants.CONFIG_CORE_SECTION, null,
            ConfigConstants.CONFIG_KEY_AUTOCRLF, res.autoCRLF);

         log.info("Storing config {}", res);
         try {
            config.save();
         } catch (Exception e) {
            ExceptionAlert.showAlert("Failed to store configuration", e);
         }
      }
   }

   private File promptForFile(File currentFile, String title, String content) {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle(title);
      dialog.setContentText(content);
      Optional<String> result = dialog.showAndWait();
      if (!result.isPresent()) {
         return null;
      }
      String file = result.get();
      File newFile;
      if (currentFile.isDirectory()) {
         newFile = new File(currentFile, file);
      } else {
         newFile = new File(currentFile.getParentFile(), file);
      }
      return newFile;
   }

   public void setContext(TreeItem<FileItem> item) {
      currentNode = item;
      currentFile = currentNode.getValue().getFile();
   }
}
