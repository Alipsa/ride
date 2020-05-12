package se.alipsa.ride.menu;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

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

public class CreateProjectWizardDialog extends Dialog<CreateProjectWizardResult> {

  private static final Logger log = LogManager.getLogger();

  private final TextField groupNameField;
  private final TextField projectNameField;
  private File selectedDirectory;
  private final Ride gui;
  private final TextField dirField;
  private final CheckBox changeToDir;
  private final TextField projectDirField;

  CreateProjectWizardDialog(Ride gui) {
    this.gui = gui;
    setTitle("Create Maven Project Wizard");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    Insets insets = new Insets(10, 15, 10, 10);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(insets);
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
    projectNameField = new TextField();
    projectNameField.setPrefColumnCount(10);
    projectNameField.setTooltip(new Tooltip("The name of your project; do not use spaces or slashes, only a-z, 0-9, _, -"));
    projectNameField.focusedProperty().addListener((arg0, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        updateDirField(projectNameField.getText());
      }
    });
    grid.add(projectNameField, 1,1);

    Label chooseDirLabel = new Label("Base dir");
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

    Label packageDirlabel = new Label("Package project dir");
    grid.add(packageDirlabel, 0, 3);
    projectDirField = new TextField();
    projectDirField.setText(selectedDirectory.getAbsolutePath());
    projectDirField.setDisable(true);
    grid.add(projectDirField, 1, 3, 2, 1);

    changeToDir = new CheckBox("Change to new project dir");
    changeToDir.setSelected(true);
    grid.add(changeToDir, 0, 4, 2, 1);

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

  private void updateDirField(String projectName) {
    projectDirField.setText(new File(selectedDirectory, projectName.trim()).getAbsolutePath());
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
      dirField.setText(selectedDirectory.getAbsolutePath());
      projectDirField.setText(new File(selectedDirectory, projectNameField.getText().trim()).getAbsolutePath());
      getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }
  }

  private CreateProjectWizardResult createResult() {
    CreateProjectWizardResult res = new CreateProjectWizardResult();
    res.groupName = groupNameField.getText();
    res.projectName = projectNameField.getText();
    res.dir = new File(projectDirField.getText());
    res.changeToDir = changeToDir.isSelected();
    return res;
  }
}
