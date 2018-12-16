package se.alipsa.renjinstudio.inout;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import se.alipsa.renjinstudio.utils.FileUtils;

import java.io.File;
import java.net.URL;

public class FileTree extends TreeView {

    public FileTree() {

        String currentPath = new File(".").getAbsolutePath();
        File current = new File(currentPath).getParentFile();

        setRoot(createTree(current));
        getRoot().setExpanded(true);
        setCellFactory((e) -> new TreeCell<File>(){
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(item.getName());
                    setGraphic(getTreeItem().getGraphic());
                } else {
                    setText("");
                    setGraphic(null);
                }
            }
        });
    }

    private TreeItem<File> createTree(File file) {
        TreeItem<File> item = new TreeItem<>(file);
        File[] childs = file.listFiles();
        if (childs != null) {
            for (File child : childs) {
                item.getChildren().add(createTree(child));
            }
            URL url = FileUtils.getResourceUrl("image/folder.png");
            item.setGraphic(new ImageView(url.toExternalForm()));
        } else {
            URL url = FileUtils.getResourceUrl("image/file.png");
            item.setGraphic(new ImageView(url.toExternalForm()));
        }
        return item;
    }
}
