package se.alipsa.ride.inout;

import se.alipsa.ride.environment.connections.ConnectionInfo;
import se.alipsa.rideutils.GuiInteraction;

public interface InOut extends GuiInteraction {

  /** Return a connections for the name defined in Ride */
  ConnectionInfo connection(String name);

}
