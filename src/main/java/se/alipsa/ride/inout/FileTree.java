package se.alipsa.ride.inout;

import static se.alipsa.ride.Constants.KEY_CODE_COPY;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class FileTree extends TreeView<File> {

  private final String folderUrl = Objects.requireNonNull(FileUtils.getResourceUrl("image/folder.png")).toExternalForm();
  private final String fileUrl = Objects.requireNonNull(FileUtils.getResourceUrl("image/file.png")).toExternalForm();
  private final String WORKING_DIR_PREF = "FileTree.WorkingDir";
  private TreeItemComparator treeItemComparator = new TreeItemComparator();
  private Ride gui;
  private static Logger log = LogManager.getLogger(FileTree.class);
  private FileOpener fileOpener;

  FileTree(Ride gui) {
    this.gui = gui;
    CodeComponent codeComponent = gui.getCodeComponent();
    fileOpener = new FileOpener(codeComponent);
    this.getStyleClass().add("fileTree");

    String currentPath = new File(getWorkingDirPref()).getAbsolutePath();
    File current = new File(currentPath);
    setWorkingDir(current);

    setRoot(createTree(current));

    sortTree(getRoot());

    getRoot().setExpanded(true);
    setCellFactory((e) -> new TreeCell<File>() {
      @Override
      protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
          setText(item.getName());
          setGraphic(getTreeItem().getGraphic());
        } else {
          setText("");
          setGraphic(null);
        }
      }
    });

    setOnKeyPressed(event -> {
      if (KEY_CODE_COPY.match(event)) {
        copySelectionToClipboard();
      }
    });

    setOnMouseClicked(this::handleClick);

    setContextMenu(createContextMenu());
  }

  private ContextMenu createContextMenu() {
    ContextMenu menu = new ContextMenu();

    MenuItem copyMI = new MenuItem("Copy name");
    copyMI.setOnAction(e -> copySelectionToClipboard());

    MenuItem createDirMI = new MenuItem("Create dir");
    createDirMI.setOnAction(e -> {
      TreeItem<File> currentNode = getSelectionModel().getSelectedItem();
      File currentFile = currentNode.getValue();
      File newDir = promptForFile(currentFile, "Create and add dir", "Enter the dir name:");
      if (newDir == null) {
        return;
      }
      try {
        Files.createDirectory(newDir.toPath());
        addTreeNode(newDir);
      } catch (IOException e1) {
        ExceptionAlert.showAlert("Failed to create directory", e1);
      }
    });

    MenuItem createFileMI = new MenuItem("Create file");
    createFileMI.setOnAction(e -> {
      TreeItem<File> currentNode = getSelectionModel().getSelectedItem();
      File currentFile = currentNode.getValue();
      File newFile = promptForFile(currentFile, "Create and add file", "Enter the file name:");
      if (newFile == null) {
        return;
      }
      try {
        Files.createFile(newFile.toPath());
        addTreeNode(newFile);
      } catch (IOException e1) {
        ExceptionAlert.showAlert("Failed to create file", e1);
      }
    });

    MenuItem deleteMI = new MenuItem("Delete");
    deleteMI.setOnAction(e -> {
      TreeItem<File> currentNode = getSelectionModel().getSelectedItem();
      File currentFile = currentNode.getValue();
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

    menu.getItems().addAll(copyMI, createDirMI, createFileMI, deleteMI);
    return menu;
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


  File getRootDir() {
    return getRoot().getValue();
  }

  private String getWorkingDirPref() {
    return gui.getPrefs().get(WORKING_DIR_PREF, ".");
  }

  private void setWorkingDirPref(File dir) {
    gui.getPrefs().put(WORKING_DIR_PREF, dir.getAbsolutePath());
    setWorkingDir(dir);
  }

  private void setWorkingDir(File dir) {
    gui.getConsoleComponent().setWorkingDir(dir);
    System.setProperty("user.dir", dir.getAbsolutePath());
  }

  private TreeItem<File> createTree(File file) {
    TreeItem<File> item = new TreeItem<>(file);
    File[] childs = file.listFiles();
    if (childs != null) {
      for (File child : childs) {
        item.getChildren().add(createTree(child));
      }
      item.setGraphic(new ImageView(folderUrl));
    } else {
      item.setGraphic(new ImageView(fileUrl));
    }
    return item;
  }

  private void handleClick(MouseEvent event) {
    if (event.getClickCount() == 2) {
      TreeItem<File> item = getSelectionModel().getSelectedItem();
      if (item == null) {
        return;
      }
      File file = item.getValue();
      if (file.isDirectory()) {
        return;
      }
      fileOpener.openFile(file);
    }
  }

  void addTreeNode(File file) {
    TreeItem<File> item = findTreeViewItem(this.getRoot(), file.getParentFile());
    if (item == null) {
      log.info("File saved outside of current working dir");
      return;
    }
    TreeItem<File> fileItem = new TreeItem<>(file);
    addTreeNode(item, fileItem);
  }

  private void addTreeNode(TreeItem<File> dirItem, TreeItem<File> fileItem) {
    if (fileItem.getValue().isDirectory()) {
      fileItem.setGraphic(new ImageView(folderUrl));
    } else {
      fileItem.setGraphic(new ImageView(fileUrl));
    }
    dirItem.getChildren().add(fileItem);
    dirItem.getChildren().sort(treeItemComparator);
    dirItem.setExpanded(true);
  }

  void refresh(File dir) {
    if (dir == null) {
      Alerts.warn("Dir is missing (null)", "Cannot refresh file tree when dir specified is missing");
      return;
    }
    if (dir.isFile()) {
      dir = dir.getParentFile();
    }
    setRoot(createTree(dir));
    sortTree(getRoot());
    getRoot().setExpanded(true);
    setWorkingDirPref(dir);
  }

  public void refresh() {
    File current = getRoot().getValue();
    setRoot(createTree(current));
    sortTree(getRoot());
    getRoot().setExpanded(true);

  }

  private TreeItem<File> findTreeViewItem(TreeItem<File> item, File value) {
    if (item != null && item.getValue().equals(value)) {
      return item;
    }
    if (item != null) {
      for (TreeItem<File> child : item.getChildren()) {
        TreeItem<File> s = findTreeViewItem(child, value);
        if (s != null) {
          return s;
        }
      }
    }
    return null;
  }

  private void sortTree(TreeItem<File> item) {
    ObservableList<TreeItem<File>> children = item.getChildren();
    children.sort(treeItemComparator);
    for (TreeItem<File> child : item.getChildren()) {
      sortTree(child);
    }
  }

  private static class TreeItemComparator implements Comparator<TreeItem<File>>, Serializable {

    private static final long serialVersionUID = -7749561517249799967L;

    @Override
    public int compare(TreeItem<File> fileTreeItem, TreeItem<File> t1) {
      return fileTreeItem.getValue().getName().compareTo(t1.getValue().getName());
    }
  }

  private void copySelectionToClipboard() {
    TreeItem<File> treeItem = getSelectionModel().getSelectedItem();
    final ClipboardContent clipboardContent = new ClipboardContent();
    String value = treeItem.getValue().getName();
    clipboardContent.putString(value);
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }
}
