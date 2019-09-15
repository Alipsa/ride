package tree;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import se.alipsa.ride.inout.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* works with color themes but downside is that the colors might not be appropriate for all themes */
public class StylableTree extends Application {

   private TreeView<FileItem> treeView;
   private final TreeItem<FileItem> rootNode = new TreeItem<>(new FileItem(new File(".")));

   static final String GIT_ADDED = "-fx-text-fill: rgba(115, 155, 105, 255);";
   static final String GIT_UNTRACKED = "-fx-text-fill: sienna";
   static final String GIT_CHANGED= "-fx-text-fill: royalblue";
   static final String GIT_NONE= "";

   @Override
   public void start(Stage stage) {
      VBox box = new VBox();
      Scene scene = new Scene(box, 500, 300);

      treeView = new TreeView<>(rootNode);
      treeView.setShowRoot(true);
      rootNode.setExpanded(true);

      List<TreeItem<FileItem>> list = new ArrayList<>();
      list.add(createTreeItem(new FileItem(new File("blueTheme.css"))));
      list.add(createTreeItem(new FileItem(new File("brightTheme.css"))));
      list.add(createTreeItem(new FileItem(new File("darkTheme.css"))));
      rootNode.getChildren().setAll(list);

      treeView.setCellFactory((TreeView<FileItem> tv) -> {
         TreeCell<FileItem> cell = new TreeCell<FileItem>() {
            @Override
            protected void updateItem(FileItem item, boolean empty) {
               if (item != null && !empty) {
                  setText(item.getText());
                  setStyle(item.getStyle());
               }
               super.updateItem(item, empty);
            }
         };
         return cell;
      });


      Button addButton = new Button("Add");
      addButton.setOnAction(a -> updateTreeViewItem(GIT_ADDED));
      Button changedButton = new Button("Changed");
      changedButton.setOnAction(a -> updateTreeViewItem(GIT_CHANGED));
      Button untrackButton = new Button("Untrack");
      untrackButton.setOnAction(a -> updateTreeViewItem(GIT_UNTRACKED));
      Button resetButton = new Button("Reset");
      resetButton.setOnAction(a -> updateTreeViewItem(GIT_NONE));

      ComboBox<String> styleSheetChooser = new ComboBox<>();
      styleSheetChooser.getItems().addAll("brightTheme.css", "blueTheme.css", "darkTheme.css");
      styleSheetChooser.setOnAction(a -> {
         scene.getStylesheets().clear();
         scene.getStylesheets().add(styleSheetChooser.getValue());
      });
      styleSheetChooser.setValue("blueTheme.css");
      scene.getStylesheets().add(styleSheetChooser.getValue());


      HBox buttonBox = new HBox();
      buttonBox.getChildren().addAll(addButton, changedButton, untrackButton, resetButton, styleSheetChooser);
      Insets insets = new Insets(5);
      HBox.setMargin(untrackButton, insets );
      HBox.setMargin(addButton, insets);
      HBox.setMargin(changedButton, insets);
      HBox.setMargin(resetButton, insets);
      HBox.setMargin(styleSheetChooser, insets);

      box.getChildren().addAll(treeView, buttonBox);
      VBox.setMargin(treeView, new Insets(10));
      VBox.setMargin(buttonBox, new Insets(10));

      stage.setScene(scene);
      stage.show();
   }


   private void updateTreeViewItem(String style) {
      TreeItem<FileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
      FileItem selected = selectedItem.getValue();
      selected.setStyle(style);
   }

   public static void main(String[] args) {
      Application.launch(args);
   }


   private TreeItem<FileItem> createTreeItem(FileItem fileItem) {
      TreeItem<FileItem> treeItem = new TreeItem<>(fileItem);
      ChangeListener<String> fillListener = (obs, oldName, newName) -> {
         TreeModificationEvent<FileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
         Event.fireEvent(treeItem, event);
      };
      fileItem.addListener(fillListener);
      return treeItem;
   }
}
