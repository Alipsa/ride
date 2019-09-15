package tree;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import se.alipsa.ride.inout.FileItem;

import java.io.File;

public class StyleClassFileItem extends FileItem {

   public StyleClassFileItem(File file) {
      super(file);
   }

   public void addStyleClass(String styleClass) {
      caption.getStyleClass().add(styleClass);
   }

   public void addStyleClassListener(ListChangeListener<String> styleClassListener) {
      caption.getStyleClass().addListener(styleClassListener);
   }

   public ObservableList<String> getStyleClass() {
      return caption.getStyleClass();
   }

}
