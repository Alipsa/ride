package se.alipsa.ride.inout.plot.grdevice;

import org.renjin.grDevices.GraphicsDevice;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

// TODO register this in the ConsoleComponent on the session
// probably something like session.getOptions().set("device", theGraphicsDevice);
public class GrDevice extends GraphicsDevice {

   @Override
   public void open(double w, double h) {
      container = new GrContainer(w, h);

   }
}
