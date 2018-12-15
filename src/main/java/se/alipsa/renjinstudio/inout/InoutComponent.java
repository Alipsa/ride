package se.alipsa.renjinstudio.inout;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

public class InoutComponent extends TabPane {

    public InoutComponent() {
        Tab files = new Tab();
        files.setText("Files");
        TextArea southEast = new TextArea();
        southEast.setText("Files");
        files.setContent(southEast);
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
}
