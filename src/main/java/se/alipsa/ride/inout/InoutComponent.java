package se.alipsa.ride.inout;

import static se.alipsa.ride.menu.GlobalOptions.ENABLE_GIT;
import static se.alipsa.ride.menu.GlobalOptions.USE_MAVEN_CLASSLOADER;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.objects.NativeArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;
import se.alipsa.renjin.client.datautils.Table;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleTextArea;
import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.ride.inout.plot.PlotsTab;
import se.alipsa.ride.inout.viewer.ViewTab;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.TikaUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class InoutComponent extends TabPane implements InOut {

  private final FileTree fileTree;
  private final PlotsTab plotsTab;
  private final PackagesTab packages;
  private final ViewTab viewer;
  private final HelpTab helpTab;
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
    branchLabel.setPadding(new Insets(0, 0, 0, 10));
    filesButtonPane.getChildren().add(branchLabel);


    HBox hbox = new HBox();
    //Label statusLabel = new Label("Status");
    //statusLabel.setPadding(new Insets(0,5,0,10));
    //hbox.getChildren().add(statusLabel);
    statusField = new TextField();
    statusField.setPadding(new Insets(1, 10, 1, 10));
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

    packages = new PackagesTab(gui);

    getTabs().add(packages);

    helpTab = new HelpTab();
    helpTab.setText("Help");

    getTabs().add(helpTab);

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
      setBusy(true);
      changeRootDir(selectedDirectory);
      setBusy(false);
    }
  }

  private void setBusy(boolean busy) {
    if (busy) {
      gui.setWaitCursor();
      this.setCursor(Cursor.WAIT);
    } else {
      gui.setNormalCursor();
      this.setCursor(Cursor.DEFAULT);
    }
  }

  public void changeRootDir(File dir) {
    if (!dir.equals(getRootDir())) {
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

  public void expandTreeNodes(TreeItem<?> item) {
    if (item != null && !item.isLeaf()) {
      item.setExpanded(true);
      for (TreeItem<?> child : item.getChildren()) {
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

  @Override
  public void display(String fileName, String... title) {
    URL url = FileUtils.getResourceUrl(fileName);
    log.info("Reading image from " + url);
    if (url == null) {
      Alerts.warn("Cannot display image", "Failed to find " + fileName);
      return;
    }
    File file = new File(fileName);
    if (file.exists()) {
      try {
        String contentType = TikaUtils.instance().detectContentType(file);
        if ("image/svg+xml".equals(contentType)) {
          Platform.runLater(() -> {
            final WebView browser = new WebView();
            browser.getEngine().load(url.toExternalForm());
            display(browser, title);
          });
          return;
        }
      } catch (IOException e) {
        ExceptionAlert.showAlert("Failed to detect image content type", e);
      }
    }
    Image img = new Image(url.toExternalForm());
    display(img, title);
  }

  public void view(Object matrix, String... title) {
   View(matrix,title);
  }

  public void View(Object matrix, String... title) {
    if (matrix == null) {
      Alerts.warnFx("View", "matrix is null, cannot View");
      return;
    }
    ConsoleTextArea console = gui.getConsoleComponent().getConsole();
    if (matrix instanceof NativeArray || matrix instanceof ScriptObjectMirror) {
      viewJavaScriptMatrix(matrix, title);
    } if (matrix instanceof Object[][]) {
      view2dArray((Object[][])matrix, title);
    } else {
      console.appendWarningFx("Unknown matrix type " + matrix.getClass().getName());
      console.appendFx(String.valueOf(matrix), true);
    }
  }

  private void viewJavaScriptMatrix(Object matrix, String... title) {
    ConsoleTextArea console = gui.getConsoleComponent().getConsole();
    Object obj = toJava(matrix);
    if (obj instanceof List) {
      List<List<Object>> rowList = new ArrayList<>();
      if (((List) obj).get(0) instanceof List) {
        List<List<Object>> rows = (List<List<Object>>) obj;

        for (List<Object> row : rows) {
          List<Object> cols = new ArrayList<>();
          for (Object col : row) {
            cols.add(String.valueOf(col));
          }
          rowList.add(cols);
        }
        List<String> header = createAnonymousHeader(rows.get(0).size());
        Table table = new Table(header, rowList);
        showInViewer(table, title);
      } else {
        List<Object> objList = (List<Object>)obj;
        List<Object> cols = new ArrayList<>();
        for (Object col : objList) {
          cols.add(String.valueOf(col));
        }
        rowList.add(cols);
        List<String> header = createAnonymousHeader(objList.size());
        Table table = new Table(header, rowList);
        showInViewer(table, title);
      }
    } else {
      console.appendWarningFx("This does not look like a matrix, not sure how to render a "
          + obj.getClass().getName() + ",\n  js class is " + matrix.getClass().getName()
          + "\n  unwrapped as " + ScriptUtils.unwrap(matrix).getClass().getName());
      console.appendFx( String.valueOf(obj), true);
    }
  }

  private Object toJava(Object jsObj) {
    if (jsObj instanceof ScriptObjectMirror) {
      ScriptObjectMirror jsObjectMirror = (ScriptObjectMirror) jsObj;
      if (jsObjectMirror.isArray()) {
        List<Object> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : jsObjectMirror.entrySet()) {
          list.add(toJava(entry.getValue()));
        }
        return list;
      } else {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsObjectMirror.entrySet()) {
          map.put(entry.getKey(), toJava(entry.getValue()));
        }
        return map;
      }
    } else {
      return jsObj;
    }
  }

  public void view(List<List<Object>> matrix, String... title) {
    View(matrix, title);
  }

  public void View(List<List<Object>> matrix, String... title) {
    if (matrix == null) {
      Alerts.warnFx("View", "matrix is null, cannot View");
      return;
    }
    List<String> header = createAnonymousHeader(matrix.size());
    Table table = new Table(header, matrix);
    showInViewer(table, title);
  }

  @NotNull
  private List<String> createAnonymousHeader(int size) {
    List<String> header = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      header.add("V" + i);
    }
    return header;
  }

  public void View(SEXP sexp, String... title) {
    if (sexp == null) {
      Alerts.warnFx("View", "sexp is null, cannot View");
      return;
    }
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

  private void view2dArray(Object[][] matrix, String... title) {
    List<List<Object>> objList = new ArrayList<>();
    for (Object[] row : matrix) {
      objList.add(Arrays.asList(row));
    }
    List<String> header = createAnonymousHeader(matrix[0].length);
    Table table = new Table(header, objList);
    showInViewer(table, title);
  }

  @Override
  public void viewHtml(SEXP sexp, String... title) {
    Platform.runLater(() -> {
      viewer.viewHtml(sexp.asString(), title);
      getSelectionModel().select(viewer);
    });
  }

  public void viewHtmlWithBootstrap(String html, String... title) {
    Platform.runLater(() -> {
      viewer.viewHtmlWithBootstrap(html, title);
      getSelectionModel().select(viewer);
    });
  }

  @Override
  public void viewer(SEXP sexp, String... title) {
    Platform.runLater(() -> {
      viewer.viewer(sexp.asString(), title);
      getSelectionModel().select(viewer);
    });
  }

  @Override
  public void viewHelp(SEXP sexp, String... title) {
    Platform.runLater(() -> {
      helpTab.display(sexp.asString(), title);
      getSelectionModel().select(helpTab);
    });
  }

  /**
   * As this is called from the script engine which runs on a separate thread
   * any gui interaction must be performed in a Platform.runLater (not sure if this qualifies as gui interaction though)
   * TODO: If the error is not printed after extensive testing then remove the catch IllegalStateException block
   *
   * @return the file from the active tab or null if the active tab has never been saved
   */
  @Override
  public String scriptFile() {

    try {
      File file = gui.getCodeComponent().getActiveTab().getFile();
      if (file == null) {
        return null;
      }
      return file.getCanonicalPath().replace('\\', '/');
    } catch (IllegalStateException e) {
      log.info("Not on javafx thread", e);
      final FutureTask<String> query = new FutureTask<>(() -> {
        File file = gui.getCodeComponent().getActiveTab().getFile();
        return file.getCanonicalPath().replace('\\', '/');
      });
      Platform.runLater(query);
      try {
        return query.get();
      } catch (InterruptedException | ExecutionException e1) {
        Platform.runLater(() -> ExceptionAlert.showAlert("Failed to get file from active tab", e1));
        return null;
      }
    } catch (IOException e) {
      log.info("Failed to resolve canonical path", e);
      File file = gui.getCodeComponent().getActiveTab().getFile();
      return file.getAbsolutePath().replace('\\', '/');
    }
  }

  @Override
  public ConnectionInfo connection(String name) {
    return gui.getEnvironmentComponent().getConnections().stream()
        .filter(ci -> ci.getName().equals(name)).findAny().orElse(null);
  }

  @Override
  public Stage getStage() {
    return gui.getStage();
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
   packages.setLoadedPackages(pkgs);
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
    Platform.runLater(() -> statusField.setText(status));
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

  public void cloneGitRepo(String url, File targetDir) throws GitAPIException {
    Git.cloneRepository()
        .setURI(url)
        .setDirectory(targetDir)
        .call();
    changeRootDir(targetDir);
  }
}
