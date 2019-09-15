package tree;

import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Paint;
import se.alipsa.ride.inout.FileItem;

import java.io.File;

public class ColorFileItem extends FileItem {

   public ColorFileItem(File file) {
      super(file);
   }

   public Paint getTextColor() {
      return caption.getFill();
   }

   public void addLColoristener(ChangeListener<Paint> listener) {
      caption.fillProperty().addListener(listener);
   }

   public void setColor(Paint color) {
      caption.fillProperty().set(color);
   }
}
