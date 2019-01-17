package se.alipsa.ride.inout;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.TabType;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Optional;

public class FileTree extends TreeView {

  CodeComponent codeComponent;

  TreeItemComparator treeItemComparator = new TreeItemComparator();

  private final String WORKING_DIR_PREF = "FileTree.WorkingDir";

  Tika contentProber = new Tika();

  final String folderUrl = FileUtils.getResourceUrl("image/folder.png").toExternalForm();
  final String fileUrl = FileUtils.getResourceUrl("image/file.png").toExternalForm();

  Ride gui;
  Logger log = LoggerFactory.getLogger(FileTree.class);

  public FileTree(Ride gui) {
    this.gui = gui;
    this.codeComponent = gui.getCodeComponent();

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

    setOnMouseClicked(this::handleClick);

    setContextMenu(createContextMenu());
  }

  private ContextMenu createContextMenu() {
    ContextMenu menu = new ContextMenu();
    MenuItem createDirMI = new MenuItem("Create dir");
    createDirMI.setOnAction(e -> {
      TreeItem<File> currentNode = (TreeItem) getSelectionModel().getSelectedItem();
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
      TreeItem<File> currentNode = (TreeItem) getSelectionModel().getSelectedItem();
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
      TreeItem<File> currentNode = (TreeItem) getSelectionModel().getSelectedItem();
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

    menu.getItems().addAll(createDirMI, createFileMI, deleteMI);
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


  public File getRootDir() {
    return (File) getRoot().getValue();
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
      TreeItem<File> item = (TreeItem) getSelectionModel().getSelectedItem();
      File file = item.getValue();
      String type = guessContentType(file);
      log.debug("File ContentType for {} detected as {}", file.getName(), type);
      if (file.isFile()) {
        String fileNameLower = file.getName().toLowerCase();
        if (strEndsWith(fileNameLower, ".r", ".s") || strEquals(type, "text/x-rsrc")) {
          codeComponent.addTab(file, TabType.R);
        } else if (strEquals(type, "application/xml", "text/xml")
            || strEndsWith(type, "+xml")
          // in case an xml declaration was omitted:
          /*|| strEndsWith(fileNameLower,".xml")*/) {
          codeComponent.addTab(file, TabType.XML);
        } else if (strEndsWith(fileNameLower, ".java")) {
          codeComponent.addTab(file, TabType.JAVA);
        } else if (strEquals(type, "text/x-sql", "application/sql")) {
          codeComponent.addTab(file, TabType.SQL);
        } else if (strStartsWith(type, "text")
            || strEquals(type, "application/x-bat",
            "application/x-sh", "application/json", "application/x-sas")
          /*|| strEndsWith(fileNameLower, ".txt", ".md", ".csv")*/) {
          codeComponent.addTab(file, TabType.TXT);
        } else {
          if (isDesktopSupported()) {
              log.info("Try to open {} in associated application", file.getName());
              openApplicationExternal(file);
          } else {
            Alerts.info("Unknown file type",
                "Unknown file type, not sure what to do with " + file.getName());
          }
        }
      }
    }
  }

  private boolean isDesktopSupported() {
    try {
      return Desktop.isDesktopSupported();
    } catch (Exception e) {
      return false;
    }
  }

  private void openApplicationExternal(File file) {
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Desktop.getDesktop().open(file);
        return null;
      }
    };

    task.setOnFailed(e -> ExceptionAlert.showAlert("Failed to open " + file, task.getException()));
    Thread appthread = new Thread(task);
    appthread.start();
  }

  private boolean strStartsWith(String fileNameLower, String... strOpt) {
    for (String start : strOpt) {
      if (fileNameLower.startsWith(start.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean strEndsWith(String fileNameLower, String... strOpt) {
    for (String end : strOpt) {
      if (fileNameLower.endsWith(end.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private boolean strEquals(String fileNameLower, String... strOpt) {
    for (String str : strOpt) {
      if (fileNameLower.equalsIgnoreCase(str)) {
        return true;
      }
    }
    return false;
  }

  private String guessContentType(File file) {
    final String unknown = "unknown";
    String type;
    try {
      //type = Files.probeContentType(file.toPath());
      type = contentProber.detect(file);
    } catch (IOException e) {
      e.printStackTrace();
      return unknown;
    }
    if (type != null) {
      return type;
    } else {
      return unknown;
    }
  }

  public void addTreeNode(File file) {
    TreeItem<File> item = findTreeViewItem(this.getRoot(), file.getParentFile());
    if (item == null) {
      log.info("File saved outside of current working dir");
      return;
    }
    TreeItem<File> fileItem = new TreeItem<>(file);
    addTreeNode(item, fileItem);
  }

  public void addTreeNode(TreeItem<File> dirItem, TreeItem<File> fileItem) {
    if (fileItem.getValue().isDirectory()) {
      fileItem.setGraphic(new ImageView(folderUrl));
    } else {
      fileItem.setGraphic(new ImageView(fileUrl));
    }
    dirItem.getChildren().add(fileItem);
    dirItem.getChildren().sort(treeItemComparator);
    dirItem.setExpanded(true);
  }

  public void refresh(File dir) {
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
    File current = (File) getRoot().getValue();
    setRoot(createTree(current));
    sortTree(getRoot());
    getRoot().setExpanded(true);

  }

  private TreeItem<File> findTreeViewItem(TreeItem<File> item, File value) {
    if (item != null && item.getValue().equals(value)) {
      return item;
    }

    for (TreeItem<File> child : item.getChildren()) {
      TreeItem<File> s = findTreeViewItem(child, value);
      if (s != null) {
        return s;
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

  private class TreeItemComparator implements Comparator<TreeItem<File>> {

    @Override
    public int compare(TreeItem<File> fileTreeItem, TreeItem<File> t1) {
      return fileTreeItem.getValue().getName().compareTo(t1.getValue().getName());
    }
  }


}
