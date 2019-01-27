package se.alipsa.ride.inout;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import org.renjin.primitives.Types;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.inout.plot.PlotsTab;
import se.alipsa.ride.inout.viewer.ViewTab;
import se.alipsa.ride.utils.Alerts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InoutComponent extends TabPane {

  FileTree fileTree;
  PlotsTab plotsTab;
  Tab packages;
  ViewTab viewer;
  Ride gui;

  Logger log = LoggerFactory.getLogger(InoutComponent.class);

  public InoutComponent(Ride gui) {

    this.gui = gui;

    fileTree = new FileTree(gui);

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

    plotsTab = new PlotsTab();

    getTabs().add(plotsTab);

    packages = new Tab();
    packages.setText("Packages");
    packages.setContent(new TextArea());

    getTabs().add(packages);

    Tab help = new Tab();
    help.setText("Help");

    getTabs().add(help);

    viewer = new ViewTab();

    getTabs().add(viewer);
  }

  private void handleChangeDir(ActionEvent actionEvent) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setInitialDirectory(gui.getInoutComponent().getRootDir());
    File selectedDirectory = dirChooser.showDialog(gui.getStage());

    if (selectedDirectory == null) {
      log.info("No Directory selected");
    } else {
      fileTree.refresh(selectedDirectory);
    }
  }

  private void handleRefresh(ActionEvent actionEvent) {
    fileTree.refresh();
  }

  public void fileAdded(File file) {
    fileTree.addTreeNode(file);
  }

  public File getRootDir() {
    return fileTree.getRootDir();
  }

  /**
   * display an image in the Plot txttab
   */
  public void display(Node node, String... title) {
    Platform.runLater(()-> {
      plotsTab.showPlot(node, title);
      SingleSelectionModel<Tab> selectionModel = getSelectionModel();
      selectionModel.select(plotsTab);
    }
    );
  }

  /**
   * display an image in the Plot txttab
   */
  public void display(Image img, String... title) {
    ImageView node = new ImageView(img);
    display(node, title);
  }

  public void View(SEXP sexp, String... title) {
    // For some reason polymorfism of Vector, StringVector and Matrix does not work (everything is treated as Vector)
    // so need to differentiate explicitly
    String type =  sexp.getTypeName();
    if (sexp instanceof ListVector) {
      System.out.println("Identified as ListVector of type " + type);
      view((ListVector)sexp, title);
    } else if (sexp instanceof Vector) {
      System.out.println("Identified as Vector of type " + type);
      Vector vec = (Vector)sexp;
      if (vec.hasAttributes()) {
        AttributeMap attributes = vec.getAttributes();
        Vector dim = attributes.getDim();
        if (dim == null) {
          System.out.println("Identified as a vector of type " + type + " with length " + vec.length());
          view(vec, title);
        } else {
          if (dim.length() == 2) {
            System.out.println("Identified as a " +
                dim.getElementAsInt(0) + "x" +
                dim.getElementAsInt(1) + " matrix of type " + type);
            Matrix mat = new Matrix(vec);
            view(mat, title);
          } else {
            Alerts.warn("Array of type, " + sexp.getTypeName() + " cannot be shown",
                "Result is an array with " + dim.length() + " dimensions. Convert this object to a data.frame to view it!");
          }
        }
      }
      else {
        view(vec, title);
      }
    } else {
      Alerts.warn("Unknown type, " + sexp.getTypeName(), ", convert this object to a data.frame or vector to view it");
    }
  }


  private void view(Matrix mat, String... title) {
    String type = mat.getVector().getTypeName();
    List<String> colList = new ArrayList<>();
    for (int i = 0; i < mat.getNumCols(); i++) {
      String colName = mat.getColName(i) == null ? i + "" : mat.getColName(i);
      colList.add(colName);
    }

    List<List<Object>> data = new ArrayList<>();

    List<Object> row;
    for (int i = 0; i < mat.getNumRows(); i++) {
      row = new ArrayList<>();
      for (int j = 0; j < mat.getNumCols(); j++) {
        if ("integer".equals(type)) {
          row.add(mat.getElementAsInt(i, j));
        } else {
          row.add(mat.getElementAsDouble(i, j));
        }
      }
      data.add(row);
    }

    showInViewer(colList, data, title);

  }

  private void view(Vector vec, String... title) {
    List<String> colList = new ArrayList<>();
    colList.add(vec.getTypeName());

    List<Vector> values = new ArrayList<>();
    values.add(vec);

    List<List<Object>> rowList = transpose(values);
    showInViewer(colList, rowList, title);

  }

  private void showInViewer(List<String> colList, List<List<Object>> rowList, String[] title) {
    Platform.runLater(() -> {
          viewer.viewTable(colList, rowList, title);
          SingleSelectionModel<Tab> selectionModel = getSelectionModel();
          selectionModel.select(viewer);
        }
    );
  }

  private void view(ListVector listVec, String... title ) {
    List<String> colList = new ArrayList<>();
    if (listVec.hasAttributes()) {
      AttributeMap attributes = listVec.getAttributes();
      Map<Symbol, SEXP> attrMap = attributes.toMap();
      Symbol s = attrMap.keySet().stream().filter(p -> "names".equals(p.getPrintName())).findAny().orElse(null);
      Vector colNames = (Vector)attrMap.get(s);
      for(int i = 0; i < colNames.length(); i++) {
        colList.add(colNames.getElementAsString(i));
      }
    }

    List<Vector> table = new ArrayList<>();
    for(SEXP col : listVec) {
      Vector column = (Vector)col;
      table.add(column);
    }
    List<List<Object>> rowList = transpose(table);

    showInViewer(colList, rowList, title);
  }

  private List<List<Object>> transpose(List<Vector> table) {
    List<List<Object>> ret = new ArrayList<>();
    final int N = table.get(0).length();
    for (int i = 0; i < N; i++) {
      List<Object> row = new ArrayList<>();
      for (Vector col : table) {
        if (Types.isFactor(col)) {
          AttributeMap attributes = col.getAttributes();
          Map<Symbol, SEXP> attrMap = attributes.toMap();
          Symbol s = attrMap.keySet().stream().filter(p -> "levels".equals(p.getPrintName())).findAny().orElse(null);
          Vector vec = (Vector)attrMap.get(s);
          row.add(vec.getElementAsObject(col.getElementAsInt(i)-1));
        } else {
          row.add(col.getElementAsObject(i));
        }
      }
      ret.add(row);
    }
    return ret;
  }

  public void setPackages(StringVector pkgs) {
    TextArea ta = (TextArea) packages.getContent();
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
}
