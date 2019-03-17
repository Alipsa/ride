package se.alipsa.ride.inout;

import javafx.scene.Node;
import javafx.scene.image.Image;
import org.renjin.sexp.SEXP;

public interface InOut {

  /**
   * display an image in the Plot tab
   */
  void display(Node node, String... title);

  /**
   * display an image in the Plot tab
   */
  void display(Image img, String... title);

  /**
   * display data in the Viewer tab
   */
  void View(SEXP sexp, String... title);

  /** Return the current active script file or null if is has not been saved yet */
  String scriptFile();
}
