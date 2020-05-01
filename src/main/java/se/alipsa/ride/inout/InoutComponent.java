package se.alipsa.ride.inout;

import static se.alipsa.ride.menu.GlobalOptions.ENABLE_GIT;
import static se.alipsa.ride.menu.GlobalOptions.USE_MAVEN_CLASSLOADER;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.UnStyledCodeArea;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.inout.plot.PlotsTab;
import se.alipsa.ride.inout.viewer.ViewTab;
import se.alipsa.ride.model.Table;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class InoutComponent extends TabPane implements InOut {

  private final FileTree fileTree;
  private final PlotsTab plotsTab;
  private final Tab packages;
  private final ViewTab viewer;
  private final Ride gui;
  private final Label branchLabel;
  private final TextField statusField;
  private boolean enableGit;

  private static final Logger log = LogManager.getLogger(InoutComponent.class);

  public InoutComponent(Ride gui) {

    this.gui = gui;
    enableGit = gui.getPrefs().getBoolean(ENABLE_GIT, true);

    fileTree = new FileTree(gui, this);

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

    branchLabel = new Label("");
    branchLabel.setPadding(new Insets(0,0,0,10));
    filesButtonPane.getChildren().add(branchLabel);


    HBox hbox = new HBox();
    //Label statusLabel = new Label("Status");
    //statusLabel.setPadding(new Insets(0,5,0,10));
    //hbox.getChildren().add(statusLabel);
    statusField = new TextField();
    statusField.setPadding(new Insets(1,10,1,10));
    statusField.setDisable(true);

    HBox.setHgrow(statusField, Priority.ALWAYS);
    hbox.getChildren().add(statusField);

    filesPane.setTop(filesButtonPane);
    filesPane.setCenter(fileTree);
    filesPane.setBottom(hbox);
    filesTab.setContent(filesPane);
    getTabs().add(filesTab);

    plotsTab = new PlotsTab();

    getTabs().add(plotsTab);

    packages = new Tab();
    packages.setText("Packages");
    UnStyledCodeArea ta = new UnStyledCodeArea();
    ta.getStyleClass().add("txtarea");
    packages.setContent(ta);

    getTabs().add(packages);

    Tab help = new Tab();
    help.setText("Help");

    getTabs().add(help);

    viewer = new ViewTab();

    getTabs().add(viewer);

    setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
  }

  private void handleChangeDir(ActionEvent actionEvent) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    File rootDir = gui.getInoutComponent().getRootDir();
    if (rootDir != null && rootDir.exists()) {
      dirChooser.setInitialDirectory(rootDir);
    }
    File selectedDirectory = dirChooser.showDialog(gui.getStage());

    if (selectedDirectory == null) {
      log.info("No Directory selected");
    } else {
      changeRootDir(selectedDirectory);
    }
  }

  public void changeRootDir(File dir) {
    if(!dir.equals(getRootDir())) {
      fileTree.refresh(dir);
      if (gui.getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false)) {
        gui.getConsoleComponent().initRenjin(gui.getClass().getClassLoader());
      }
    }
  }

  private void handleRefresh(ActionEvent actionEvent) {
    refreshFileTree();
  }

  public void refreshFileTree() {
    fileTree.refresh();
    fileTree.getRoot().setExpanded(true);
  }

  public void expandTree() {
    expandTreeNodes(fileTree.getRoot());
  }

  public void expandTreeNodes(TreeItem<?> item){
    if(item != null && !item.isLeaf()){
      item.setExpanded(true);
      for(TreeItem<?> child:item.getChildren()){
        expandTreeNodes(child);
      }
    }
  }

  public void fileAdded(File file) {
    fileTree.addTreeNode(file);
  }

  public File getRootDir() {
    return fileTree.getRootDir();
  }

  public void display(Node node, String... title) {
    Platform.runLater(() -> {
          plotsTab.showPlot(node, title);
          SingleSelectionModel<Tab> selectionModel = getSelectionModel();
          selectionModel.select(plotsTab);
        }
    );
  }

  public void display(Image img, String... title) {
    ImageView node = new ImageView(img);
    display(node, title);
  }

  public void View(SEXP sexp, String... title) {
    // For some reason polymorfism of Vector, StringVector and Matrix does not work (everything is treated as Vector)
    // so need to differentiate explicitly
    String type = sexp.getTypeName();
    if (sexp instanceof ListVector) {
      log.debug("Identified as ListVector of type {}", type);
      view((ListVector) sexp, title);
    } else if (sexp instanceof Vector) {
      log.debug("Identified as Vector of type {}", type);
      Vector vec = (Vector) sexp;
      if (vec.hasAttributes()) {
        AttributeMap attributes = vec.getAttributes();
        Vector dim = attributes.getDim();
        if (dim == null) {
          log.debug("Identified as a vector of type {} with length {}", type, vec.length());
          view(vec, title);
        } else {
          if (dim.length() == 2) {
            log.debug("Identified as a {}x{} matrix of type {}", dim.getElementAsInt(0), dim.getElementAsInt(1), type);
            Matrix mat = new Matrix(vec);
            view(mat, title);
          } else {
            Platform.runLater(() ->
            Alerts.warn("Array of type, " + sexp.getTypeName() + " cannot be shown",
                "Result is an array with " + dim.length() + " dimensions. Convert this object to a data.frame to view it!")
            );
          }
        }
      } else {
        view(vec, title);
      }
    } else {
      Platform.runLater(() ->
          Alerts.warn("Unknown type, " + sexp.getTypeName(), ", convert this object to a data.frame or vector to view it")
      );
    }
  }

  /**
   * As this is called from the script engine which runs on a separate thread
   * any gui interaction must be performed in a Platform.runLater (not sure if this qualifies as gui interaction though)
   * TODO: If the error is not printed after extensive testing then remove the catch IllegalStateException block
   * @return the file from the active tab or null if the active tab has never been saved
   */
  @Override
  public String scriptFile() {

    try {
      File file = gui.getCodeComponent().getActiveTab().getFile();
      return file == null ? null : file.getAbsolutePath();
    } catch (IllegalStateException e) {
      log.info("Not on javafx thread", e);
      final FutureTask<String> query = new FutureTask<>(() -> {
        File file = gui.getCodeComponent().getActiveTab().getFile();
        return file == null ? null : file.getAbsolutePath();
      });
      Platform.runLater(query);
      try {
        return query.get();
      } catch (InterruptedException | ExecutionException e1) {
        Platform.runLater(() -> ExceptionAlert.showAlert("Failed to get file from active tab", e1));
        return null;
      }
    }
  }

  @Override
  public ConnectionInfo connection(String name) {
    return  gui.getEnvironmentComponent().getConnections().stream()
        .filter(ci -> ci.getName().equals(name)).findAny().orElse(null);
  }

  private void view(Matrix mat, String... title) {
    Table table = new Table(mat);
    showInViewer(table, title);
  }

  private void view(Vector vec, String... title) {
    Table table = new Table(vec);
    showInViewer(table, title);
  }

  public void showInViewer(Table table, String... title) {
    Platform.runLater(() -> {
          viewer.viewTable(table, title);
          SingleSelectionModel<Tab> selectionModel = getSelectionModel();
          selectionModel.select(viewer);
        }
    );
  }

  public void view(ListVector listVec, String... title) {
    Table table = new Table(listVec);
    showInViewer(table, title);
  }

  public void setPackages(StringVector pkgs) {
    UnStyledCodeArea ta = (UnStyledCodeArea) packages.getContent();
    ta.clear();
    if (pkgs == null) {
      return;
    }
    for (String pkg : pkgs) {
      ta.appendText(pkg + "\n");
    }
  }

  @Override
  public String toString() {
    return "The Ride InOutComponent";
  }

  public TreeItem<FileItem> getRoot() {
    return fileTree.getRoot();
  }

  public Git getGit() {
    return fileTree.getGit();
  }

  public Label getBranchLabel() {
    return branchLabel;
  }

  public void setStatus(String status) {
    Platform.runLater(() -> statusField.setText(status));;
  }

  public void clearStatus() {
    setStatus("");
  }

  public void setEnableGit(boolean enableGit) {
    boolean doRefresh = false;
    if (this.enableGit != enableGit) {
      doRefresh = true;
    }
    this.enableGit = enableGit;

    if (!enableGit) {
      branchLabel.setText("");
      statusField.setText("");
    }

    if (doRefresh) {
      refreshFileTree();
    }
  }

  public boolean isGitEnabled() {
    return enableGit;
  }
}
