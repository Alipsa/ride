import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import se.alipsa.ride.inout.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StylableTree extends Application {

    private TreeView<FileItem> treeView;
    private final TreeItem<FileItem> rootNode = new TreeItem<>(new FileItem(new File(".")));

    private TextField textField;

    @Override
    public void start(Stage stage) {
      VBox box = new VBox();
      Scene scene = new Scene(box, 400, 400);

      treeView = new TreeView<>(rootNode);
      treeView.setShowRoot(true);
      rootNode.setExpanded(true);

      List<TreeItem<FileItem>> list = new ArrayList<>();
      list.add(createTreeItem(new FileItem(new File("blueTheme.css"))));
      list.add(createTreeItem(new FileItem(new File("brightTheme.css"))));
      list.add(createTreeItem(new FileItem(new File("darkTheme.css"))));
      rootNode.getChildren().setAll(list);

      textField = new TextField("");

      attachListeners();

      box.getChildren().add(treeView);
      box.getChildren().add(textField);
      VBox.setMargin(treeView, new Insets(10));
      VBox.setMargin(textField, new Insets(10));

      stage.setScene(scene);
      stage.show();
    }

    private void attachListeners() {
      treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<FileItem>>() {
        @Override
        public void changed(ObservableValue<? extends TreeItem<FileItem>> observable, TreeItem<FileItem> oldValue, TreeItem<FileItem> newValue) {
          textField.setText(newValue.getValue().getText());
        }
      });

      textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (!newValue) {
            updateTreeViewItem();
          }
        }
      });

      textField.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          updateTreeViewItem();
        }
      });
    }

    private void updateTreeViewItem() {
      TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
      FileItem selected = selectedItem.getValue();
      selected.getCaption().fillProperty().set(Color.GREEN);
    }

    public static void main(String[] args) {
      Application.launch(args);
    }


    private TreeItem<FileItem> createTreeItem(FileItem fileItem) {
      TreeItem<FileItem> treeItem = new TreeItem<>(fileItem);
      ChangeListener<Paint> fillListener = (obs, oldName, newName) -> {
        TreeModificationEvent<FileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
        Event.fireEvent(treeItem, event);
      };
      fileItem.getCaption().fillProperty().addListener(fillListener);
      treeItem.valueProperty().addListener((obs, oldValue, newValue) -> {
        if (oldValue != null) {
          oldValue.getCaption().fillProperty().removeListener(fillListener);
        }
        if (newValue != null) {
          newValue.getCaption().fillProperty().addListener(fillListener);
        }
      });
      return treeItem ;
    }
}
