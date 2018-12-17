package se.alipsa.renjinstudio.inout;

import javafx.scene.control.Alert;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.renjinstudio.code.CodeComponent;
import se.alipsa.renjinstudio.utils.Alerts;
import se.alipsa.renjinstudio.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class FileTree extends TreeView {

    CodeComponent codeComponent;

    Logger log = LoggerFactory.getLogger(FileTree.class);

    public FileTree(CodeComponent codeComponent) {
        this.codeComponent = codeComponent;

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

        setOnMouseClicked(this::handleClick);
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

    private void handleClick(MouseEvent event) {
        if(event.getClickCount() == 2) {
            TreeItem<File> item = (TreeItem) getSelectionModel().getSelectedItem();
            File file = item.getValue();

            if (file.isFile()) {
                String fileNameUpper = file.getName().toUpperCase();
                if (fileNameUpper.endsWith(".R") || fileNameUpper.endsWith(".S")) {
                    codeComponent.addTab(file);
                } else {
                    Alerts.info("Unknown file type",
                        "Unknown file type, not sure what to do with " + file.getName());
                }
            }
        }
    }
}
