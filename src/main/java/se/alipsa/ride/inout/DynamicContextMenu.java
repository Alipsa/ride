package se.alipsa.ride.inout;

import static se.alipsa.ride.Constants.GitStatus.GIT_ADDED;
import static se.alipsa.ride.utils.GitUtils.asRelativePath;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamicContextMenu extends ContextMenu {

  private Git git;
  private FileTree fileTree;
  private TreeItem<FileItem> currentNode;
  private File currentFile;

  MenuItem gitInitMI = new MenuItem("Initialize root as git repo");

  private Logger log = LogManager.getLogger();

  public DynamicContextMenu(FileTree fileTree) {
    this.fileTree = fileTree;
    MenuItem copyMI = new MenuItem("Copy name");
    copyMI.setOnAction(e -> fileTree.copySelectionToClipboard());

    MenuItem createDirMI = new MenuItem("Create dir");
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

    MenuItem createFileMI = new MenuItem("Create file");
    createFileMI.setOnAction(e -> {
      File newFile = promptForFile(currentFile, "Create and add file", "Enter the file name:");
      if (newFile == null) {
        return;
      }
      try {
        Files.createFile(newFile.toPath());
        fileTree.addTreeNode(newFile);
      } catch (IOException e1) {
        ExceptionAlert.showAlert("Failed to create file", e1);
      }
    });

    MenuItem deleteMI = new MenuItem("Delete");
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
      MenuItem gitAddMI = new MenuItem("Add");
      gitAddMI.setOnAction(this::gitAdd);
      gitMenu.getItems().add(gitAddMI);

      MenuItem gitAddAllMI = new MenuItem("Add all");
      gitAddAllMI.setOnAction(this::gitAddAll);
      gitMenu.getItems().add(gitAddAllMI);

      MenuItem gitDeleteMI = new MenuItem("Delete");
      gitDeleteMI.setOnAction(this::gitRm);
      gitMenu.getItems().add(gitDeleteMI);

      MenuItem gitCommitMI = new MenuItem("Commit");
      gitCommitMI.setOnAction(this::gitCommit);
      gitMenu.getItems().add(gitCommitMI);

      MenuItem gitStatusMI = new MenuItem("Status");
      gitStatusMI.setOnAction(this::gitStatus);
      gitMenu.getItems().add(gitStatusMI);

      MenuItem gitStatusAllMI = new MenuItem("Status all");
      gitStatusAllMI.setOnAction(this::gitStatusAll);
      gitMenu.getItems().add(gitStatusAllMI);

      MenuItem gitDiffMI = new MenuItem("Diff");
      gitDiffMI.setOnAction(this::gitDiff);
      gitMenu.getItems().add(gitDiffMI);

      MenuItem gitResetMI = new MenuItem("Reset");
      gitResetMI.setOnAction(this::gitReset);
      gitMenu.getItems().add(gitResetMI);

      Menu gitRemoteMenu = new Menu("remote");
      gitMenu.getItems().add(gitRemoteMenu);

      MenuItem gitAddRemoteMI = new MenuItem("Add remote");
      gitAddRemoteMI.setOnAction(this::gitAddRemote);
      gitRemoteMenu.getItems().add(gitAddRemoteMI);

      MenuItem gitPushMI = new MenuItem("Push");
      gitPushMI.setOnAction(this::gitPush);
      gitRemoteMenu.getItems().add(gitPushMI);

      MenuItem gitPullMI = new MenuItem("Pull");
      gitPullMI.setOnAction(this::gitPull);
      gitRemoteMenu.getItems().add(gitPullMI);
    }
    getItems().addAll(copyMI, createDirMI, createFileMI, deleteMI, gitMenu);
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
      }
      for (String path : status.getUntracked()) {
        git.add().addFilepattern(path).call();
      }
      fileTree.refresh();
    } catch (GitAPIException e) {
      log.warn("Failed to add all", e);
      ExceptionAlert.showAlert("Failed to add all", e);
    }
  }

  private void gitDiff(ActionEvent actionEvent) {
    try(StringWriter writer = new StringWriter();
        OutputStream out = new WriterOutputStream(writer, StandardCharsets.UTF_8)){
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
      StringBuilder str = new StringBuilder();

      Set<String> added = status.getAdded();
      if (added.size() > 0) {
        str.append("Added: ").append(String.join(", ", added)).append("\n");
      }
      Set<String> changed = status.getChanged();
      if (changed.size() > 0) {
        str.append("Changed: ").append(String.join(", ", changed)).append("\n");
      }
      Set<String> conflicting = status.getConflicting();
      if (conflicting.size() > 0) {
        str.append("Conflicting: ").append(String.join(", ", conflicting)).append("\n");
      }
      Set<String> missing = status.getMissing();
      if (missing.size() > 0) {
        str.append("Missing: ").append(String.join(", ", missing)).append("\n");
      }
      Set<String> modified = status.getModified();
      if (modified.size() > 0) {
        str.append("Modified: ").append(String.join(", ", modified)).append("\n");
      }
      Set<String> removed = status.getRemoved();
      if (removed.size() > 0) {
        str.append("Removed: ").append(String.join(", ", removed)).append("\n");
      }
      Set<String> uncomittedChanges = status.getUncommittedChanges();
      if (uncomittedChanges.size() > 0) {
        str.append("Uncommited changes: ").append(String.join(", ", uncomittedChanges)).append("\n");
      }
      Set<String> untracked = status.getUntracked();
      if (untracked.size() > 0) {
        str.append("Untracked: ").append(String.join(", ", untracked)).append("\n");
      }
      str.append("hasUncommittedChanges: ").append(status.hasUncommittedChanges()).append("\n");
      str.append("isClean: ").append(status.isClean()).append("\n");
      Alerts.info("Status", str.toString());
    } catch (GitAPIException e) {
      log.warn("Failed to get status", e);
      ExceptionAlert.showAlert("Failed to get status", e);
    }
  }

  private void gitPull(ActionEvent actionEvent) {
    try {
      PullResult pullResult = git.pull().call();
      log.info(pullResult.toString());
    } catch (GitAPIException e) {
      log.warn("Failed to pull", e);
      ExceptionAlert.showAlert("Failed to pull", e);
    }
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
    try {
      git.push().call();
    } catch (GitAPIException e) {
      log.warn("Failed to push", e);
      ExceptionAlert.showAlert("Failed to push", e);
    }
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
      try {
        String msg = td.getEditor().getText();
        if (StringUtils.isBlank(msg)) {
          Alerts.info("Empty message", "Commit message cannot be empty");
          return;
        }
        CommitCommand commit = git.commit();
        RevCommit revCommit = commit.setMessage(msg).call();
        log.info("Commited result: {}", revCommit);
        fileTree.refresh();
      } catch (GitAPIException e) {
        log.warn("Failed to commit ", e);
        ExceptionAlert.showAlert("Failed to commit ", e);
      }
    }
  }

  private void gitAdd(ActionEvent actionEvent) {
    String currentPath = getRelativePath();
    try {
      DirCache dc = git.add().addFilepattern(currentPath).call();
      log.info("Added {} to git dir cache, node is {}", currentPath, currentNode.getValue().getText());
      currentNode.getValue().setStyle(GIT_ADDED.getStyle());
    } catch (GitAPIException e) {
      log.warn("Failed to add " + currentPath, e);
      ExceptionAlert.showAlert("Failed to add " + currentPath, e);
    }
  }

  private void gitInit(ActionEvent actionEvent) {
    try {
      git = Git.init().setDirectory(fileTree.getRootDir()).call();
      fileTree.refresh();
    } catch (GitAPIException e) {
      log.warn("Failed to initialize git in " + fileTree.getRootDir().getAbsolutePath(), e);
      ExceptionAlert.showAlert("Failed to initialize git in " + fileTree.getRootDir().getAbsolutePath(), e);
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
