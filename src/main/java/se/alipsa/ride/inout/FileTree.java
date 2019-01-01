package se.alipsa.ride.inout;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.util.Comparator;

public class FileTree extends TreeView {

    CodeComponent codeComponent;

    TreeItemComparator treeItemComparator = new TreeItemComparator();
    FileComparator fileComparator = new FileComparator();

    private final String WORKING_DIR_PREF = "FileTree.WorkingDir";


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

            if (file.isFile()) {
                String fileNameUpper = file.getName().toUpperCase();
                if (fileNameUpper.endsWith(".R") || fileNameUpper.endsWith(".S")) {
                    codeComponent.addCodeTab(file);
                } else if (fileNameUpper.endsWith(".TXT") || fileNameUpper.endsWith(".CSV")
                        || fileNameUpper.endsWith(".MD") || fileNameUpper.endsWith("RMD")
                        || fileNameUpper.equals("NAMESPACE")) {
                    codeComponent.addTxtTab(file);
                } else {
                    Alerts.info("Unknown file type",
                            "Unknown file type, not sure what to do with " + file.getName());
                }
            }
        }
    }

    public void addFile(File file) {
        TreeItem<File> item = findTreeViewItem(this.getRoot(), file.getParentFile());

        TreeItem<File> fileItem = new TreeItem<>(file);
        fileItem.setGraphic(new ImageView(fileUrl));
        item.getChildren().add(fileItem);
        item.getChildren().sort(treeItemComparator);
        item.setExpanded(true);
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


    private class FileComparator implements Comparator<File> {

        @Override
        public int compare(File file, File t1) {
            return file.getName().compareTo(t1.getName());
        }
    }

    private class TreeItemComparator implements Comparator<TreeItem<File>> {

        @Override
        public int compare(TreeItem<File> fileTreeItem, TreeItem<File> t1) {
            return fileTreeItem.getValue().getName().compareTo(t1.getValue().getName());
        }
    }


}
