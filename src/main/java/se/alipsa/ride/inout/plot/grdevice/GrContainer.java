package se.alipsa.ride.inout.plot.grdevice;

import org.jfree.fx.FXGraphics2D;
import org.renjin.grDevices.GDContainer;
import org.renjin.grDevices.GDObject;
import org.renjin.grDevices.GDState;

import java.awt.*;

public class GrContainer  implements GDContainer {

   GrPane grPane;
   private GDState state;
   private Dimension size;

   private int deviceNumber = -1;


   public GrContainer(double w, double h) {
      size = new Dimension((int)w, (int)h);
      grPane = new GrPane(size);
      state = new GDState();
   }

   @Override
   public void add(GDObject gdObject) {
      grPane.paint(gdObject, state);

   }

   @Override
   public void reset() {
   }

   @Override
   public GDState getGState() {
      return state;
   }

   @Override
   public Graphics getGraphics() {
      return grPane.getGraphics();
   }

   @Override
   public void syncDisplay(boolean b) {

   }

   @Override
   public void setDeviceNumber(int deviceNumber) {
      this.deviceNumber = deviceNumber;
   }

   @Override
   public void closeDisplay() {

   }

   @Override
   public int getDeviceNumber() {
      return deviceNumber;
   }

   @Override
   public Dimension getSize() {
      return size;
   }
}
