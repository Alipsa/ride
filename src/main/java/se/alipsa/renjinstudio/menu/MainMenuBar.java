package se.alipsa.renjinstudio.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public class MainMenuBar extends MenuBar {

    public MainMenuBar() {
        Menu menuFile = new Menu("File");
        Menu menuEdit = new Menu("Edit");
        Menu menuCode = new Menu("Code");
        Menu menuView = new Menu("View");
        Menu menuPlots = new Menu("Plots");
        Menu menuSession = new Menu("Session");
        Menu menuBuild = new Menu("Build");
        Menu menuDebug = new Menu("Debug");
        Menu menuProfile = new Menu("Profile");
        Menu menuTools = new Menu("Tools");
        Menu menuHelp = new Menu("Help");
        getMenus().addAll(menuFile, menuEdit, menuCode, menuView, menuPlots, menuSession,
                menuBuild, menuDebug, menuProfile, menuTools, menuHelp);
    }
}
