package se.alipsa.ride.inout.plot.grdevice;

import org.jfree.fx.FXGraphics2D;
import org.renjin.grDevices.GDObject;
import org.renjin.grDevices.GDState;

import java.awt.*;

// Similar to AwtPanel
public class GrPane extends javafx.scene.canvas.Canvas {

   private final int bufferWidth;
   private final int bufferHeight;

   private FXGraphics2D g2bridge;

   public GrPane(Dimension size) {
      g2bridge = new FXGraphics2D(getGraphicsContext2D());
      bufferWidth = (int) size.getWidth();
      bufferHeight = (int) size.getHeight();
      super.setWidth(size.getWidth());
      super.setHeight(size.getHeight());
   }

   public Graphics getGraphics() {
      return g2bridge.create();
   }

   public void paint(GDObject gdObject, GDState state) {
      System.out.println("GrPane.paint, not implemented");
   }
}
