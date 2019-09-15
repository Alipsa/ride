package se.alipsa.ride.inout;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.transport.URIish;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

public class DynamicContextMenu extends ContextMenu {

  static final String GIT_ADDED = "-fx-text-fill: rgba(115, 155, 105, 255);";
  static final String GIT_UNTRACKED = "-fx-text-fill: sienna";
  static final String GIT_CHANGED= "-fx-text-fill: royalblue";
  static final String GIT_NONE= "";

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

      MenuItem gitDeleteMI = new MenuItem("Delete");
      gitDeleteMI.setOnAction(this::gitRm);
      gitMenu.getItems().add(gitDeleteMI);

      MenuItem gitCommitMI = new MenuItem("Commit");
      gitCommitMI.setOnAction(this::gitCommit);
      gitMenu.getItems().add(gitCommitMI);

      MenuItem gitStatusMI = new MenuItem("Status");
      gitStatusMI.setOnAction(this::gitStatus);
      gitMenu.getItems().add(gitStatusMI);

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

  private void gitStatus(ActionEvent actionEvent) {
    try {
      StatusCommand statusCommand = git.status();
      statusCommand.addPath(asRelativePath(currentFile));
      System.out.println("---------------- Status ------------------------");
      System.out.println("Paths = " +  statusCommand.getPaths());
      Status status = statusCommand.call();
      System.out.println("Added: " + status.getAdded());
      System.out.println("Changed" + status.getChanged());
      System.out.println("Conflicting: " + status.getConflicting());
      System.out.println("Missing: " + status.getMissing());
      System.out.println("Modified: " + status.getModified());
      System.out.println("Removed: " + status.getRemoved());
      System.out.println("Uncommited changes" + status.getUncommittedChanges());
      System.out.println("Untracked: " + status.getUntracked());
      System.out.println("hasUncommittedChanges: " + status.hasUncommittedChanges());
      System.out.println("isClean: " + status.isClean());
      System.out.println("---------------- /Status -----------------------");
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
    String currentPath = asRelativePath(currentFile);
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
        commit.setMessage(msg).call();
        fileTree.refresh();
      } catch (GitAPIException e) {
        log.warn("Failed to commit ", e);
        ExceptionAlert.showAlert("Failed to commit ", e);
      }
    }
  }

  private void gitAdd(ActionEvent actionEvent) {
    String currentPath = asRelativePath(currentFile);
    try {
      DirCache dc = git.add().addFilepattern(currentPath).call();
      log.info("Added {} to git dir cache, node is {}", currentPath, currentNode.getValue().getText());
      currentNode.getValue().setStyle(GIT_ADDED);
    } catch (GitAPIException e) {
      log.warn("Failed to add " + currentPath, e);
      ExceptionAlert.showAlert("Failed to add " + currentPath, e);
    }
  }

  private String asRelativePath(File currentFile) {
    String root = fileTree.getRootDir().getAbsolutePath();
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
