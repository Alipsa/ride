package se.alipsa.ride.menu;

import java.io.File;

public class CreateProjectWizardResult {

  String groupName;
  String projectName;
  File dir;
  boolean changeToDir;

  @Override
  public String toString() {
    return "groupName = " + groupName + ", packageName = " + projectName + ", dir = " + dir + ", changeToDir = " + changeToDir;
  }
}
