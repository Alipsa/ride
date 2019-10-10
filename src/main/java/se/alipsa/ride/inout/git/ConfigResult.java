package se.alipsa.ride.inout.git;

import org.eclipse.jgit.lib.CoreConfig;

public class ConfigResult {

  public CoreConfig.AutoCRLF autoCRLF;

  @Override
  public String toString() {
    return "autoCRLF = " + autoCRLF;
  }
}
