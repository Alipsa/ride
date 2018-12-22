package se.alipsa.ride.inout.plot;

import javafx.scene.canvas.Canvas;
import org.jfree.fx.FXGraphics2D;
import org.renjin.grDevices.AwtDevice;

public class PlotCanvas extends Canvas {

  // TODO consider using https://github.com/brucejohnson/jfxplot
  // in a similar way as https://github.com/brucejohnson/studiofx

    private FXGraphics2D g2bridge;

    AwtDevice awtDevice;

    public PlotCanvas() {
        awtDevice = new AwtDevice(null, null);
        g2bridge = new FXGraphics2D(getGraphicsContext2D());

        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
        System.setProperty("JAVAGD_CLASS_NAME", AwtDevice.class.getName());
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();
        getGraphicsContext2D().clearRect(0, 0, width, height);

        //awtContainer.paint(g2bridge);
    }
}
