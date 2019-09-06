package se.alipsa.ride.menu;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;

import java.io.File;

public class CreatePackageWizardDialog extends Dialog<CreatePackageWizardResult> {

  private static final Logger log = LogManager.getLogger();

  private TextField packageNameField;
  private File selectedDirectory;
  private Ride gui;
  private TextField dirField;

  CreatePackageWizardDialog(Ride gui) {
    this.gui = gui;
    setTitle("Create Package Wizard");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    Label packageNameLabel = new Label("Package Name");
    grid.add(packageNameLabel,0,0);
    packageNameField = new TextField();
    packageNameField.setPrefColumnCount(10);
    grid.add(packageNameField, 1,0);

    Label chooseDirLabel = new Label("Project dir");
    grid.add(chooseDirLabel, 0,1);
    Button chooseDirButton = new Button("Browse...");
    grid.add(chooseDirButton, 1, 1);
    chooseDirButton.setOnAction(this::chooseProjectDir);
    dirField = new TextField();
    dirField.setPrefColumnCount(25);
    dirField.setDisable(true);
    grid.add(dirField, 2,1);


    getDialogPane().setPrefSize(600, 300);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    setResizable(true);

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
  }

  private void chooseProjectDir(ActionEvent actionEvent) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    File rootDir = gui.getInoutComponent().getRootDir();
    if (rootDir != null && rootDir.exists()) {
      dirChooser.setInitialDirectory(rootDir);
    }
    selectedDirectory = dirChooser.showDialog(gui.getStage());

    if (selectedDirectory == null) {
      log.info("No Directory selected");
    } else {
      dirField.setText(new File(selectedDirectory, packageNameField.getText().trim()).getAbsolutePath());
      getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }
  }

  private CreatePackageWizardResult createResult() {
    CreatePackageWizardResult res = new CreatePackageWizardResult();
    res.packageName = packageNameField.getText();
    res.dir = new File(selectedDirectory, packageNameField.getText().trim());
    return res;
  }
}
