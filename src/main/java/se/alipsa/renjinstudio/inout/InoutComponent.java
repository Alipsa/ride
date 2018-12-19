package se.alipsa.renjinstudio.inout;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.renjinstudio.RenjinStudio;

import java.io.File;

public class InoutComponent extends TabPane {

    FileTree fileTree;

    RenjinStudio gui;

    Logger log = LoggerFactory.getLogger(InoutComponent.class);

    public InoutComponent(RenjinStudio gui) {

        this.gui = gui;

        fileTree = new FileTree(gui.getCodeComponent());

        Tab filesTab = new Tab();
        filesTab.setText("Files");

        BorderPane filesPane = new BorderPane();
        FlowPane filesButtonPane = new FlowPane();

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(this::handleRefresh);
        filesButtonPane.getChildren().add(refreshButton);

        Button changeDirButton = new Button("Change dir");
        changeDirButton.setOnAction(this::handleChangeDir);
        filesButtonPane.getChildren().add(changeDirButton);

        filesPane.setTop(filesButtonPane);
        filesPane.setCenter(fileTree);
        filesTab.setContent(filesPane);
        getTabs().add(filesTab);

        Tab plots = new Tab();
        plots.setText("Plots");

        getTabs().add(plots);

        Tab packages = new Tab();
        packages.setText("Packages");

        getTabs().add(packages);

        Tab help = new Tab();
        help.setText("Help");

        getTabs().add(help);

        Tab viewer = new Tab();
        viewer.setText("Viewer");

        getTabs().add(viewer);
    }

    private void handleChangeDir(ActionEvent actionEvent) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
        File selectedDirectory = dirChooser.showDialog(gui.getStage());

        if (selectedDirectory == null){
            log.info("No Directory selected");
        } else {
            fileTree.refresh(selectedDirectory);
        }
    }

    private void handleRefresh(ActionEvent actionEvent) {
        fileTree.refresh();
    }

    public void fileAdded(File file) {
        fileTree.addFile(file);
    }

    public File getRootDir() {
        return fileTree.getRootDir();
    }
}
