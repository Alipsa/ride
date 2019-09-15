package tree;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
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

/* works by itself but does not work with color themes*/
public class StyleClassTree extends Application {

   private TreeView<StyleClassFileItem> treeView;
   private final TreeItem<StyleClassFileItem> rootNode = new TreeItem<>(new StyleClassFileItem(new File(".")));

   private Button greenButton;
   private Button blackButton;

   @Override
   public void start(Stage stage) {
      VBox box = new VBox();
      Scene scene = new Scene(box, 400, 400);

      treeView = new TreeView<>(rootNode);
      treeView.setShowRoot(true);
      rootNode.setExpanded(true);

      List<TreeItem<StyleClassFileItem>> list = new ArrayList<>();
      list.add(createTreeItem(new StyleClassFileItem(new File("blueTheme.css"))));
      list.add(createTreeItem(new StyleClassFileItem(new File("brightTheme.css"))));
      list.add(createTreeItem(new StyleClassFileItem(new File("darkTheme.css"))));
      rootNode.getChildren().setAll(list);

      treeView.setCellFactory((TreeView<StyleClassFileItem> tv) -> {
         TreeCell<StyleClassFileItem> cell = new TreeCell<StyleClassFileItem>() {
            @Override
            protected void updateItem(StyleClassFileItem item, boolean empty) {
               if (item != null && !empty) {
                  setText(item.getText());
                  System.out.println("updateItem: Adding style class " + item.getStyleClass() + " to " + item.getText());
                  getStyleClass().addAll(item.getStyleClass());
               }
               super.updateItem(item, empty);
            }
         };
         return cell;
      });


      greenButton = new Button("Green");
      greenButton.setOnAction(a -> updateTreeViewItem("git-added"));
      blackButton = new Button("Black");
      blackButton.setOnAction(a -> updateTreeViewItem("git-changed"));

      ComboBox<String> styleSheetChooser = new ComboBox<>();
      styleSheetChooser.getItems().addAll("brightTheme.css", "blueTheme.css", "darkTheme.css");
      styleSheetChooser.setOnAction(a -> {
         scene.getStylesheets().clear();
         scene.getStylesheets().add(styleSheetChooser.getValue());
      });
      styleSheetChooser.setValue("brightTheme.css");
      scene.getStylesheets().add(styleSheetChooser.getValue());

      HBox buttonBox = new HBox();
      buttonBox.getChildren().addAll(blackButton, greenButton, styleSheetChooser);
      Insets insets = new Insets(10);
      HBox.setMargin(blackButton, insets );
      HBox.setMargin(greenButton, insets);
      HBox.setMargin(styleSheetChooser, insets);

      box.getChildren().addAll(treeView, buttonBox);
      VBox.setMargin(treeView, new Insets(10));
      VBox.setMargin(buttonBox, new Insets(10));

      stage.setScene(scene);
      stage.show();
   }


   private void updateTreeViewItem(String styleClass) {
      TreeItem<StyleClassFileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
      StyleClassFileItem selected = selectedItem.getValue();
      selected.addStyleClass(styleClass);
   }

   public static void main(String[] args) {
      Application.launch(args);
   }


   private TreeItem<StyleClassFileItem> createTreeItem(StyleClassFileItem fileItem) {
      TreeItem<StyleClassFileItem> treeItem = new TreeItem<>(fileItem);
      ListChangeListener<String> fillListener = styleClass-> {
         TreeModificationEvent<StyleClassFileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
         Event.fireEvent(treeItem, event);
      };
      fileItem.addStyleClassListener(fillListener);
      return treeItem;
   }
}
