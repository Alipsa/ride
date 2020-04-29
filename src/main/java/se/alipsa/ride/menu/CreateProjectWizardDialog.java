package se.alipsa.ride.menu;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.net.URL;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

public class CreateProjectWizardDialog extends Dialog<CreateProjectWizardResult> {

  private static final Logger log = LogManager.getLogger();

  private TextField groupNameField;
  private TextField packageNameField;
  private File selectedDirectory;
  private Ride gui;
  private TextField dirField;
  private CheckBox changeToDir;

  CreateProjectWizardDialog(Ride gui) {
    this.gui = gui;
    setTitle("Create Maven Project Wizard");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    Label groupNameLabel = new Label("Group Name");
    groupNameLabel.setWrapText(false);
    grid.add(groupNameLabel,0,0);
    groupNameField = new TextField();
    groupNameField.setPrefColumnCount(12);
    groupNameField.setTooltip(new Tooltip("Should be reverse domain name of your org e.g. com.acme"));
    grid.add(groupNameField, 1,0);

    Label packageNameLabel = new Label("Project Name");
    packageNameLabel.setWrapText(false);
    grid.add(packageNameLabel,0,1);
    packageNameField = new TextField();
    packageNameField.setPrefColumnCount(10);
    packageNameField.setTooltip(new Tooltip("The name of your project; do not use spaces or slashes, only a-z, 0-9, _, -"));
    grid.add(packageNameField, 1,1);

    Label chooseDirLabel = new Label("Project dir");
    chooseDirLabel.setWrapText(false);
    grid.add(chooseDirLabel, 0,2);
    Button chooseDirButton = new Button("Browse...");
    grid.add(chooseDirButton, 1, 2);
    chooseDirButton.setOnAction(this::chooseProjectDir);
    dirField = new TextField();
    dirField.setPrefColumnCount(25);
    dirField.setDisable(true);
    grid.add(dirField, 2,2);

    selectedDirectory = gui.getInoutComponent().getRootDir();
    dirField.setText(selectedDirectory.getAbsolutePath());

    changeToDir = new CheckBox("Change to new project dir");
    changeToDir.setSelected(true);
    grid.add(changeToDir, 0, 3, 2, 1);

    getDialogPane().setPrefSize(700, 300);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    setResizable(true);

    String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);
    URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
    if (styleSheetUrl != null) {
      getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
    }

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
  }

  private void chooseProjectDir(ActionEvent actionEvent) {
    DirectoryChooser dirChooser = new DirectoryChooser();
    File rootDir = gui.getInoutComponent().getRootDir();
    if (rootDir != null && rootDir.exists()) {
      dirChooser.setInitialDirectory(rootDir);
    }
    File orgSelectedDir = selectedDirectory;
    selectedDirectory = dirChooser.showDialog(gui.getStage());

    if (selectedDirectory == null) {
      log.info("No Directory selected, revert to previous dir ({})", orgSelectedDir);
      selectedDirectory = orgSelectedDir;
    } else {
      dirField.setText(new File(selectedDirectory, packageNameField.getText().trim()).getAbsolutePath());
      getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }
  }

  private CreateProjectWizardResult createResult() {
    CreateProjectWizardResult res = new CreateProjectWizardResult();
    res.groupName = groupNameField.getText();
    res.projectName = packageNameField.getText();
    res.dir = new File(selectedDirectory, packageNameField.getText().trim());
    res.changeToDir = changeToDir.isSelected();
    return res;
  }
}
