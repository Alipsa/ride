package se.alipsa.ride.inout.git;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.eclipse.jgit.api.Status;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.GuiUtils;

import java.util.Set;

public class GitStatusDialog extends Dialog<Status> {

  private Status status;

  public GitStatusDialog(Status gitStatus, String path ) {
    status = gitStatus;
    System.out.println("---------------- Status ------------------------");
    System.out.println("Path = " +  path);
    Set<String> added = status.getAdded();
    System.out.println("Added: " + added);
    Set<String> changed = status.getChanged();
    System.out.println("Changed" + changed);
    Set<String> conflicting = status.getConflicting();
    System.out.println("Conflicting: " + conflicting);
    Set<String> missing = status.getMissing();
    System.out.println("Missing: " + missing);
    Set<String> modified = status.getModified();
    System.out.println("Modified: " + modified);
    Set<String> removed = status.getRemoved();
    System.out.println("Removed: " + removed);
    Set<String> uncomittedChanges = status.getUncommittedChanges();
    System.out.println("Uncommited changes" + uncomittedChanges);
    Set<String> untracked = status.getUntracked();
    System.out.println("Untracked: " + untracked);
    System.out.println("hasUncommittedChanges: " + status.hasUncommittedChanges());
    System.out.println("isClean: " + status.isClean());
    System.out.println("---------------- /Status -----------------------");

    setTitle("Git status for " + path);

    getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    grid.add(new Label("Added"),0,0);
    CheckBox addedCheckBox = new CheckBox();
    addedCheckBox.setDisable(true);
    addedCheckBox.setSelected(added.size() > 0);
    grid.add(addedCheckBox, 1,0);

    grid.add(new Label("Changed"),0,1);
    CheckBox changedCheckBox = new CheckBox();
    changedCheckBox.setDisable(true);
    changedCheckBox.setSelected(changed.size() > 0);
    grid.add(changedCheckBox, 1,1);

    grid.add(new Label("Conflicting"),0,2);
    CheckBox conflictingCheckBox = new CheckBox();
    conflictingCheckBox.setDisable(true);
    conflictingCheckBox.setSelected(conflicting.size() > 0);
    grid.add(conflictingCheckBox, 1,2);

    grid.add(new Label("Missing"),0,3);
    CheckBox missingCheckBox = new CheckBox();
    missingCheckBox.setDisable(true);
    missingCheckBox.setSelected(missing.size() > 0);
    grid.add(missingCheckBox, 1,3);

    grid.add(new Label("Modified"),0,4);
    CheckBox modifiedCheckBox = new CheckBox();
    modifiedCheckBox.setDisable(true);
    modifiedCheckBox.setSelected(modified.size() > 0);
    grid.add(modifiedCheckBox, 1,4);

    grid.add(new Label("Removed"),2,0);
    CheckBox removedCheckBox = new CheckBox();
    removedCheckBox.setDisable(true);
    removedCheckBox.setSelected(removed.size() > 0);
    grid.add(removedCheckBox, 3,0);

    grid.add(new Label("Uncommited changes"),2,1);
    CheckBox uncomittedCheckBox = new CheckBox();
    uncomittedCheckBox.setDisable(true);
    uncomittedCheckBox.setSelected(status.hasUncommittedChanges());
    grid.add(uncomittedCheckBox, 3,1);

    grid.add(new Label("Untracked"),2,2);
    CheckBox untrackedCheckBox = new CheckBox();
    untrackedCheckBox.setDisable(true);
    untrackedCheckBox.setSelected(untracked.size() > 0);
    grid.add(untrackedCheckBox, 3,2);

    grid.add(new Label("Clean"),2,3);
    CheckBox cleanCheckBox = new CheckBox();
    cleanCheckBox.setDisable(true);
    cleanCheckBox.setSelected(status.isClean());
    grid.add(cleanCheckBox, 3,3);
    GuiUtils.addStyle(Ride.instance(), this);
    setResizable(true);
  }
}
