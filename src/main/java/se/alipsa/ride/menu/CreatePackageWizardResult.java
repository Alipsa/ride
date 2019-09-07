package se.alipsa.ride.menu;

import java.io.File;

public class CreatePackageWizardResult {

  String groupName;
  String packageName;
  File dir;
  boolean changeToDir;

  @Override
  public String toString() {
    return "groupName = " + groupName + ", packageName = " + packageName + ", dir = " + dir + ", changeToDir = " + changeToDir;
  }
}
