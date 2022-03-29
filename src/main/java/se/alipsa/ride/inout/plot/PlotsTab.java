package se.alipsa.ride.inout.plot;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.ExceptionAlert;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class PlotsTab extends Tab {

  TabPane imageTabPane;

  public PlotsTab() {
    setText("Plots");
    imageTabPane = new TabPane();
    setContent(imageTabPane);
  }

  public void showPlot(Node node, String[] title) {
    Tab tab = new Tab();
    imageTabPane.getTabs().add(tab);
    if (title.length > 0) {
      tab.setText(title[0]);
    }
    tab.setContent(node);
    final ContextMenu contextMenu = new ContextMenu();
    final MenuItem item = new MenuItem("save as image file");
    contextMenu.getItems().add(item);

    if (node instanceof ImageView) {
      var view = (ImageView)node;
      item.setOnAction(a -> promptAndWriteImage(tab.getText(), view.getImage()));

      view.setOnContextMenuRequested(e ->
          contextMenu.show(view, e.getScreenX(), e.getScreenY()));
    } else if (node instanceof WebView) {
      var view = (WebView) node;
      SnapshotParameters param = new SnapshotParameters();
      param.setDepthBuffer(true);
      item.setOnAction(a -> {
        WritableImage snapshot = view.snapshot(param, null);
        promptAndWriteImage(tab.getText(), snapshot);
      });
      view.setContextMenuEnabled(false);
      view.setOnMousePressed(e -> {
        if (e.getButton() == MouseButton.SECONDARY) {
          contextMenu.show(view, e.getScreenX(), e.getScreenY());
        } else {
          contextMenu.hide();
        }
      });

    }

    SingleSelectionModel<Tab> imageTabsSelectionModel = imageTabPane.getSelectionModel();
    imageTabsSelectionModel.select(tab);
  }

  void promptAndWriteImage(String title, Image image) {
    FileChooser fc = new FileChooser();
    fc.setInitialFileName(title + ".png");
    File file = fc.showSaveDialog(Ride.instance().getStage());
    if (file == null) {
      return;
    }
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to save image", e);
    }
  }
}
