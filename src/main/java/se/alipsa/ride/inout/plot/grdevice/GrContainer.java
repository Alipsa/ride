package se.alipsa.ride.inout.plot.grdevice;

import org.renjin.grDevices.GDContainer;
import org.renjin.grDevices.GDObject;
import org.renjin.grDevices.GDState;

import java.awt.*;

public class GrContainer implements GDContainer {

   @Override
   public void add(GDObject gdObject) {

   }

   @Override
   public void reset() {

   }

   @Override
   public GDState getGState() {
      return null;
   }

   @Override
   public Graphics getGraphics() {
      return null;
   }

   @Override
   public void syncDisplay(boolean b) {

   }

   @Override
   public void setDeviceNumber(int i) {

   }

   @Override
   public void closeDisplay() {

   }

   @Override
   public int getDeviceNumber() {
      return 0;
   }

   @Override
   public Dimension getSize() {
      return null;
   }
}
