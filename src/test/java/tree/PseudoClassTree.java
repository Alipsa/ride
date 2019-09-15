package tree;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* works by itself but does not work with color themes*/
public class PseudoClassTree extends Application {

   private TreeView<PseudoClassFileItem> treeView;
   private final TreeItem<PseudoClassFileItem> rootNode = new TreeItem<>(new PseudoClassFileItem(new File(".")));

   private Button greenButton;
   private Button blackButton;

   @Override
   public void start(Stage stage) {
      VBox box = new VBox();
      Scene scene = new Scene(box, 400, 400);

      treeView = new TreeView<>(rootNode);
      treeView.setShowRoot(true);
      rootNode.setExpanded(true);

      List<TreeItem<PseudoClassFileItem>> list = new ArrayList<>();
      list.add(createTreeItem(new PseudoClassFileItem(new File("blueTheme.css"))));
      list.add(createTreeItem(new PseudoClassFileItem(new File("brightTheme.css"))));
      list.add(createTreeItem(new PseudoClassFileItem(new File("darkTheme.css"))));
      rootNode.getChildren().setAll(list);

      treeView.setCellFactory((TreeView<PseudoClassFileItem> tv) -> {
         TreeCell<PseudoClassFileItem> cell = new TreeCell<PseudoClassFileItem>() {

            @Override
            protected void updateItem(PseudoClassFileItem item, boolean empty) {
               if (item != null) {
                  setText(item.getText());
                  System.out.println("updateItem: Updating pseudoclasses for " + item.getText() + " setting "
                     + item.getActivePseudoClass() + " to active");
                  item.getPseudoClasses().forEach(pc -> {
                     if (pc.equals(item.getActivePseudoClass())) {
                        System.out.println("Activating " + pc);
                        pseudoClassStateChanged(pc, true);
                     } else {
                        System.out.println("Inactivating " + pc);
                        pseudoClassStateChanged(pc, false);
                     }
                  });
                  System.out.println("--- Pseudo classes for cell are ---");
                  getPseudoClassStates().forEach(c -> System.out.println(c.toString()));
                  System.out.println("-----------------------------------");
                  System.out.println("--- style classes ---");
                  getStyleClass().forEach(m -> System.out.println(m.toString()));
                  System.out.println("---------------------");
                  System.out.println("--- Css class metadata ---");
                  getClassCssMetaData().forEach(m -> System.out.println(m.toString()));
                  System.out.println("--------------------------");
               }
               super.updateItem(item, empty);
            }
         };
         return cell;
      });


      greenButton = new Button("Added");
      greenButton.setOnAction(a -> updateTreeViewItem(PseudoClassFileItem.GIT_ADDED));
      blackButton = new Button("Changed");
      blackButton.setOnAction(a -> updateTreeViewItem(PseudoClassFileItem.GIT_CHANGED));

      ComboBox<String> styleSheetChooser = new ComboBox<>();
      styleSheetChooser.getItems().addAll("brightTheme.css", "blueTheme.css", "darkTheme.css");
      styleSheetChooser.setOnAction(a -> {
         scene.getStylesheets().clear();
         scene.getStylesheets().add(styleSheetChooser.getValue());
      });
      styleSheetChooser.setValue("blueTheme.css");
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


   private void updateTreeViewItem(PseudoClass styleClass) {
      TreeItem<PseudoClassFileItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
      PseudoClassFileItem selected = selectedItem.getValue();
      selected.enablePseudoClass(styleClass);
   }

   public static void main(String[] args) {
      Application.launch(args);
   }


   private TreeItem<PseudoClassFileItem> createTreeItem(PseudoClassFileItem fileItem) {
      TreeItem<PseudoClassFileItem> treeItem = new TreeItem<>(fileItem);
      SetChangeListener<PseudoClass> fillListener = styleClass-> {
         System.out.println("PseudoClass change detected");
         TreeModificationEvent<PseudoClassFileItem> event = new TreeModificationEvent<>(TreeItem.valueChangedEvent(), treeItem);
         Event.fireEvent(treeItem, event);
      };
      fileItem.addPseudoClassChangeLister(fillListener);
      return treeItem;
   }
}
