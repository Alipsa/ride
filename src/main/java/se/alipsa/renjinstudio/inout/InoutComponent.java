package se.alipsa.renjinstudio.inout;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.alipsa.renjinstudio.RenjinStudio;

import java.io.File;

public class InoutComponent extends TabPane {

    FileTree fileTree;

    public InoutComponent(RenjinStudio gui) {
        Tab files = new Tab();
        files.setText("Files");

        fileTree = new FileTree(gui.getCodeComponent());
        files.setContent(fileTree);
        getTabs().add(files);

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

    public void fileAdded(File file) {
        fileTree.refresh(file);
    }
}
