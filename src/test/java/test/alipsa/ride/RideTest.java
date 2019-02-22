package test.alipsa.ride;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import se.alipsa.ride.Ride;

public class RideTest extends ApplicationTest {

  Ride ride = new Ride();

  @Override
  public void start(Stage stage) throws Exception {
    stage.setScene(ride.getScene());
    stage.show();
    stage.toFront();
  }

  @Test void testBasicApp() {

  }
}
