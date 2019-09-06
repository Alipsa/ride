package se.alipsa.ride.menu;

import java.io.File;

public class CreatePackageWizardResult {

  String packageName;
  File dir;

  @Override
  public String toString() {
    return "packageName = " + packageName + ", dir = " + dir;
  }
}
