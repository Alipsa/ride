package se.alipsa.ride.utils;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import se.alipsa.ride.Ride;

import java.io.File;

public class FileTextField extends HBox {

  private final TextField field = new TextField();
  private final Button chooseFileButton = new Button("...");
  private final Ride gui;
  private File file;

  public FileTextField(Ride gui, File file) {
    this.gui = gui;
    setFile(file);
    HBox.setHgrow(field, Priority.ALWAYS);
    getChildren().addAll(field, chooseFileButton);
    chooseFileButton.setOnAction(this::selectFile);
  }

  private void selectFile(ActionEvent actionEvent) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Select jar file");
    chooser.setInitialDirectory(new File("."));
    chooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Jar", "*.jar")
    );
    File jarFile = chooser.showOpenDialog(gui.getStage());
    setFile(jarFile);
  }

  public File getFile() {
    return file;
  }


  public void setFile(File file) {
    this.file = file;
    if (file == null) {
      return;
    }
    field.setText(file.getAbsolutePath());
  }
}
