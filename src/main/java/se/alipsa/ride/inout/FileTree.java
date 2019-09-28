package se.alipsa.ride.inout;

import static se.alipsa.ride.Constants.GitStatus.GIT_ADDED;
import static se.alipsa.ride.Constants.GitStatus.GIT_CHANGED;
import static se.alipsa.ride.Constants.GitStatus.GIT_CONFLICT;
import static se.alipsa.ride.Constants.GitStatus.GIT_IGNORED;
import static se.alipsa.ride.Constants.GitStatus.GIT_MODIFIED;
import static se.alipsa.ride.Constants.GitStatus.GIT_UNCOMITTED_CHANGE;
import static se.alipsa.ride.Constants.GitStatus.GIT_UNTRACKED;
import static se.alipsa.ride.Constants.KEY_CODE_COPY;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FileTree extends TreeView<FileItem> {

  private final String folderUrl = Objects.requireNonNull(FileUtils.getResourceUrl("image/folder.png")).toExternalForm();
  private final String fileUrl = Objects.requireNonNull(FileUtils.getResourceUrl("image/file.png")).toExternalForm();
  private final String WORKING_DIR_PREF = "FileTree.WorkingDir";
  private TreeItemComparator treeItemComparator = new TreeItemComparator();
  private Ride gui;
  private static Logger log = LogManager.getLogger(FileTree.class);
  private FileOpener fileOpener;
  private DynamicContextMenu menu;

  FileTree(Ride gui) {
    this.gui = gui;
    CodeComponent codeComponent = gui.getCodeComponent();
    fileOpener = new FileOpener(codeComponent);
    this.getStyleClass().add("fileTree");

    String currentPath = new File(getWorkingDirPref()).getAbsolutePath();
    File current = new File(currentPath);
    setWorkingDir(current);

    setRoot(createTree(current));

    gitColorTree(getRoot());

    sortTree(getRoot());

    getRoot().setExpanded(true);
    setCellFactory(treeView -> new TreeCell<FileItem>() {

      @Override
      protected void updateItem(FileItem item, boolean empty) {
        if (item != null) {
          setText(item.getText());
          setStyle(item.getStyle());
          setGraphic(getTreeItem().getGraphic());
        } else {
          setText("");
          setGraphic(null);
        }
        super.updateItem(item, empty);
      }
    });

    setOnKeyPressed(event -> {
      if (KEY_CODE_COPY.match(event)) {
        copySelectionToClipboard();
      }
    });

    setOnMouseClicked(this::handleClick);
    menu = new DynamicContextMenu(this);
    //setContextMenu(createContextMenu());
    addEventHandler(MouseEvent.MOUSE_RELEASED, e->{
      if (e.getButton() == MouseButton.SECONDARY) {
        TreeItem<FileItem> selected = getSelectionModel().getSelectedItem();
        //item is selected - this prevents fail when clicking on empty space
        if (selected!=null) {
          //open context menu on current screen position
          openContextMenu(selected, e.getScreenX(), e.getScreenY());
        }
      } else {
        //any other click cause hiding menu
        menu.hide();
      }
    });
  }

  private void openContextMenu(TreeItem<FileItem> item, double x, double y) {
    menu.setContext(item);
    menu.show(this, x, y);
  }


  File getRootDir() {
    return getRoot().getValue().getFile();
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

  private TreeItem<FileItem> createTree(File file) {
    TreeItem<FileItem> item = new TreeItem<>(new FileItem(file));
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
        item.getChildren().add(createTree(child));
      }
      item.setGraphic(new ImageView(folderUrl));
    } else {
      setLeafProperties(item);
    }
    return item;
  }

  private void gitColorTree(TreeItem<FileItem> root) {
    if (Objects.requireNonNull(getRoot().getValue().file.list((dir, name) -> name.equalsIgnoreCase(".git"))).length > 0) {
      log.info("adding git coloring...");
    } else {
      log.info("not a git repository, skipping git coloring");
      return;
    }
    Platform.runLater(() -> {
      try {
        File rootDir = root.getValue().getFile();
        Git git = Git.open(rootDir);
        Map<Constants.GitStatus, Set<File>> statusMap = new HashMap<>();
        for (Constants.GitStatus status : Constants.GitStatus.values()) {
          statusMap.put(status, new HashSet<>());
        }
        try {
          Status status = git.status().call();
          statusMap.get(GIT_ADDED).addAll(gitPathToFile(status.getAdded()));
          statusMap.get(GIT_CHANGED).addAll(gitPathToFile(status.getChanged()));
          statusMap.get(GIT_CONFLICT).addAll(gitPathToFile(status.getConflicting()));
          statusMap.get(GIT_IGNORED).addAll(gitPathToFile(status.getIgnoredNotInIndex()));
          statusMap.get(GIT_MODIFIED).addAll(gitPathToFile(status.getModified()));
          statusMap.get(GIT_UNCOMITTED_CHANGE).addAll(gitPathToFile(status.getUncommittedChanges()));
          statusMap.get(GIT_UNTRACKED).addAll(gitPathToFile(status.getUntracked()));
        } catch (GitAPIException e) {
          ExceptionAlert.showAlert("Failed to get git staus", e);
        }
        /*
        log.info("Git changes:\n {} added: {}\n {} changed: {}\n {} conflicting: {}\n {} ignored: {}\n {} modified: {}\n {} uncomitted: {}\n {} untracked: {}",
            statusMap.get(GIT_ADDED).size(), statusMap.get(GIT_ADDED),
            statusMap.get(GIT_CHANGED).size(), statusMap.get(GIT_CHANGED),
            statusMap.get(GIT_CONFLICT).size(), statusMap.get(GIT_CONFLICT),
            statusMap.get(GIT_IGNORED).size(), statusMap.get(GIT_IGNORED),
            statusMap.get(GIT_MODIFIED).size(), statusMap.get(GIT_MODIFIED),
            statusMap.get(GIT_UNCOMITTED_CHANGE).size(), statusMap.get(GIT_UNCOMITTED_CHANGE),
            statusMap.get(GIT_UNTRACKED).size(), statusMap.get(GIT_UNTRACKED));
         */
        walkAndColor(getRoot(), statusMap);
      } catch (Exception e) {
        log.error("Failed to set git colors", e);
        ExceptionAlert.showAlert("Failed to set git colors", e);
      }
    });
  }

  private void walkAndColor(TreeItem<FileItem> root,  Map<Constants.GitStatus, Set<File>> statusMap) {
      for(TreeItem<FileItem> child: root.getChildren()){

        FileItem item = child.getValue();
        File file = item.getFile();

        if(statusMap.get(GIT_CONFLICT).contains(file)) {
          item.setStyle(GIT_CONFLICT.getStyle());
          continue;
        }
        if(statusMap.get(GIT_ADDED).contains(file)) {
          item.setStyle(GIT_ADDED.getStyle());
          continue;
        }
        if(statusMap.get(GIT_CHANGED).contains(file)) {
          item.setStyle(GIT_CHANGED.getStyle());
          continue;
        }
        if(statusMap.get(GIT_MODIFIED).contains(file)) {
          item.setStyle(GIT_MODIFIED.getStyle());
          continue;
        }
        if(statusMap.get(GIT_UNCOMITTED_CHANGE).contains(file)) {
          item.setStyle(GIT_UNCOMITTED_CHANGE.getStyle());
          break;
        }
        if(statusMap.get(GIT_UNTRACKED).contains(file)) {
          item.setStyle(GIT_UNTRACKED.getStyle());
          continue;
        }
        if(statusMap.get(GIT_IGNORED).contains(file)) {
          item.setStyle(GIT_IGNORED.getStyle());
          continue;
        }
        walkAndColor(child, statusMap);
      }
  }

  private Collection<File> gitPathToFile(Set<String> gitPaths) {
    Set<File> fileSet = new HashSet<>();
    if (gitPaths == null) {
      return fileSet;
    }
    File root = getRootDir();
    for (String path : gitPaths) {
      fileSet.add(new File(root, path));
    }
    return fileSet;
  }

  private void setLeafProperties(TreeItem<FileItem> item) {
    item.setGraphic(new ImageView(fileUrl));
    ChangeListener<String> fillListener = (obs, oldName, newName) -> {
      TreeModificationEvent<FileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), item);
      //log.info("item {} changed color", item.getValue());
      Event.fireEvent(item, event);
    };
    FileItem fileItem = item.getValue();
    fileItem.addListener(fillListener);
  }

  private void handleClick(MouseEvent event) {
    if (event.getClickCount() == 2) {
      TreeItem<FileItem> item = getSelectionModel().getSelectedItem();
      if (item == null) {
        return;
      }
      File file = item.getValue().getFile();
      if (file.isDirectory()) {
        return;
      }
      fileOpener.openFile(file);
    }
  }

  void addTreeNode(File file) {
    TreeItem<FileItem> item = findTreeViewItem(this.getRoot(), file.getParentFile());
    if (item == null) {
      log.info("File saved outside of current working dir");
      return;
    }
    TreeItem<FileItem> fileItem = new TreeItem<>(new FileItem(file));
    addTreeNode(item, fileItem);
  }

  private void addTreeNode(TreeItem<FileItem> dirItem, TreeItem<FileItem> fileItem) {
    if (fileItem.getValue().getFile().isDirectory()) {
      fileItem.setGraphic(new ImageView(folderUrl));
    } else {
      setLeafProperties(fileItem);
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
    gitColorTree(getRoot());
    setWorkingDirPref(dir);
    menu = new DynamicContextMenu(this);
  }

  public void refresh() {
    File current = getRoot().getValue().getFile();
    setRoot(createTree(current));
    sortTree(getRoot());
    getRoot().setExpanded(true);
    gitColorTree(getRoot());
    menu = new DynamicContextMenu(this);
  }

  private TreeItem<FileItem> findTreeViewItem(TreeItem<FileItem> item, File value) {
    if (item != null && item.getValue().getFile().equals(value)) {
      return item;
    }
    if (item != null) {
      for (TreeItem<FileItem> child : item.getChildren()) {
        TreeItem<FileItem> s = findTreeViewItem(child, value);
        if (s != null) {
          return s;
        }
      }
    }
    return null;
  }

  private void sortTree(TreeItem<FileItem> item) {
    ObservableList<TreeItem<FileItem>> children = item.getChildren();
    children.sort(treeItemComparator);
    for (TreeItem<FileItem> child : item.getChildren()) {
      sortTree(child);
    }
  }

  private static class TreeItemComparator implements Comparator<TreeItem<FileItem>>, Serializable {

    private static final long serialVersionUID = -7749561517249799967L;

    @Override
    public int compare(TreeItem<FileItem> fileTreeItem, TreeItem<FileItem> t1) {
      return fileTreeItem.getValue().getFile().getName().compareTo(t1.getValue().getFile().getName());
    }
  }

  void copySelectionToClipboard() {
    TreeItem<FileItem> treeItem = getSelectionModel().getSelectedItem();
    final ClipboardContent clipboardContent = new ClipboardContent();
    String value = treeItem.getValue().getFile().getName();
    clipboardContent.putString(value);
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }
}
