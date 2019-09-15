package tree;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import se.alipsa.ride.inout.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* works by itself but does not work with color themes*/
public class ColorTree extends Application {

   private TreeView<ColorFileItem> treeView;
   private final TreeItem<ColorFileItem> rootNode = new TreeItem<>(new ColorFileItem(new File(".")));

   private Button greenButton;
   private Button blackButton;

   @Override
   public void start(Stage stage) {
      VBox box = new VBox();
      Scene scene = new Scene(box, 400, 400);

      treeView = new TreeView<>(rootNode);
      treeView.setShowRoot(true);
      rootNode.setExpanded(true);

      List<TreeItem<ColorFileItem>> list = new ArrayList<>();
      list.add(createTreeItem(new ColorFileItem(new File("blueTheme.css"))));
      list.add(createTreeItem(new ColorFileItem(new File("brightTheme.css"))));
      list.add(createTreeItem(new ColorFileItem(new File("darkTheme.css"))));
      rootNode.getChildren().setAll(list);

      treeView.setCellFactory((TreeView<ColorFileItem> tv) -> {
         TreeCell<ColorFileItem> cell = new TreeCell<ColorFileItem>() {
            @Override
            protected void updateItem(ColorFileItem item, boolean empty) {
               if (item != null && !empty) {
                  setText(item.getText());
                  setTextFill(item.getTextColor());
               }
               super.updateItem(item, empty);
            }
         };
         return cell;
      });


      greenButton = new Button("Green");
      greenButton.setOnAction(a -> updateTreeViewItem(Color.GREEN));
      blackButton = new Button("Black");
      blackButton.setOnAction(a -> updateTreeViewItem(Color.BLACK));

      HBox buttonBox = new HBox();
      buttonBox.getChildren().addAll(blackButton, greenButton);
      HBox.setMargin(blackButton, new Insets(10));
      HBox.setMargin(greenButton, new Insets(10));

      box.getChildren().addAll(treeView, buttonBox);
      VBox.setMargin(treeView, new Insets(10));
      VBox.setMargin(buttonBox, new Insets(10));

      stage.setScene(scene);
      stage.show();
   }


   private void updateTreeViewItem(Paint color) {
      TreeItem<ColorFileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
      ColorFileItem selected = selectedItem.getValue();
      selected.setColor(color);
   }

   public static void main(String[] args) {
      Application.launch(args);
   }


   private TreeItem<ColorFileItem> createTreeItem(ColorFileItem fileItem) {
      TreeItem<ColorFileItem> treeItem = new TreeItem<>(fileItem);
      ChangeListener<Paint> fillListener = (obs, oldName, newName) -> {
         TreeModificationEvent<ColorFileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
         Event.fireEvent(treeItem, event);
      };
      fileItem.addLColoristener(fillListener);
      return treeItem;
   }
}
