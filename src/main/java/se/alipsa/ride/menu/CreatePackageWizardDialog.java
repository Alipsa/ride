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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.io.File;
import java.net.URL;

public class CreatePackageWizardDialog extends Dialog<CreatePackageWizardResult> {

  private static final Logger log = LogManager.getLogger();

  private TextField groupNameField;
  private TextField packageNameField;
  private File selectedDirectory;
  private Ride gui;
  private TextField dirField;
  private CheckBox changeToDir;

  private TextField packageDirField;

  CreatePackageWizardDialog(Ride gui) {
    this.gui = gui;
    setTitle("Create Package Wizard");

    Insets insets = new Insets(10, 15, 10, 10);

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    BorderPane pane = new BorderPane();
    pane.setPadding(insets);
    getDialogPane().setContent(pane);
    VBox vBox = new VBox();
    vBox.setPadding(insets);
    pane.setCenter(vBox);

    HBox groupBox = new HBox();
    groupBox.setPadding(insets);
    groupBox.setSpacing(10);
    vBox.getChildren().add(groupBox);
    Label groupNameLabel = new Label("Group Name");
    groupBox.getChildren().add(groupNameLabel);
    groupNameField = new TextField();
    groupNameField.setPrefColumnCount(10);
    groupNameField.setTooltip(new Tooltip("Should be reverse domain name of your org e.g. com.acme"));
    groupBox.getChildren().add(groupNameField);

    HBox packageBox = new HBox();
    packageBox.setPadding(insets);
    packageBox.setSpacing(10);
    vBox.getChildren().add(packageBox);
    Label packageNameLabel = new Label("Package Name");
    packageBox.getChildren().add(packageNameLabel);
    packageNameField = new TextField();
    packageNameField.setPrefColumnCount(10);
    packageNameField.setTooltip(new Tooltip("The name of your package; do not use spaces or slashes, only a-z, 0-9, _, -"));
    packageNameField.focusedProperty().addListener((arg0, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        updateDirField(packageNameField.getText());
      }
    });
    packageBox.getChildren().add(packageNameField);

    HBox dirBox = new HBox();
    dirBox.setPadding(insets);
    dirBox.setSpacing(10);
    vBox.getChildren().add(dirBox);
    Label chooseDirLabel = new Label("Base dir");
    dirBox.getChildren().add(chooseDirLabel);
    Button chooseDirButton = new Button("Browse...");
    dirBox.getChildren().add(chooseDirButton);
    chooseDirButton.setOnAction(this::chooseProjectDir);
    dirField = new TextField();
    // Need to warp it as disabled nodes cannot show tooltips.
    Label dirWrapper = new Label("", dirField);
    dirField.setDisable(true);
    HBox.setHgrow(dirField, Priority.ALWAYS);
    HBox.setHgrow(dirWrapper, Priority.ALWAYS);
    dirWrapper.setMaxWidth(Double.MAX_VALUE);
    dirField.setMaxWidth(Double.MAX_VALUE);
    selectedDirectory = gui.getInoutComponent().getRootDir();
    dirField.setText(selectedDirectory.getAbsolutePath());
    dirWrapper.setTooltip(new Tooltip(selectedDirectory.getAbsolutePath()));
    dirBox.getChildren().add(dirWrapper);

    HBox packageDirBox = new HBox();
    packageDirBox.setPadding(insets);
    packageDirBox.setSpacing(10);
    vBox.getChildren().add(packageDirBox);
    Label packageDirlabel = new Label("Package project dir");
    packageDirField = new TextField();
    packageDirField.setText(selectedDirectory.getAbsolutePath());
    packageDirField.setDisable(true);
    HBox.setHgrow(packageDirField, Priority.ALWAYS);
    HBox.setHgrow(packageDirlabel, Priority.ALWAYS);
    packageDirlabel.setMaxWidth(Double.MAX_VALUE);
    packageDirField.setMaxWidth(Double.MAX_VALUE);
    packageDirBox.getChildren().addAll(packageDirlabel, packageDirField);

    HBox changeToDirBox = new HBox();
    changeToDirBox.setPadding(insets);
    changeToDirBox.setSpacing(10);
    vBox.getChildren().add(changeToDirBox);
    changeToDir = new CheckBox("Change to new project dir");
    changeToDir.setSelected(true);
    changeToDirBox.getChildren().add(changeToDir);

    HBox packageLayoutBox = new HBox();
    packageLayoutBox.setPadding(insets);
    packageLayoutBox.setSpacing(10);
    vBox.getChildren().add(packageLayoutBox);
    final ToggleGroup group = new ToggleGroup();
    RadioButton renjinLayout = new RadioButton("Renjin Layout");
    renjinLayout.setToggleGroup(group);
    renjinLayout.setSelected(true);
    RadioButton gnurLayout = new RadioButton("GNU R Layout");
    gnurLayout.setToggleGroup(group);
    packageLayoutBox.getChildren().addAll(renjinLayout, gnurLayout);

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

  private void updateDirField(String artifactName) {
    packageDirField.setText(new File(selectedDirectory, artifactName.trim()).getAbsolutePath());
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
      packageDirField.setText(new File(selectedDirectory, packageNameField.getText().trim()).getAbsolutePath());
      getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }
  }

  private CreatePackageWizardResult createResult() {
    CreatePackageWizardResult res = new CreatePackageWizardResult();
    res.groupName = groupNameField.getText();
    res.packageName = packageNameField.getText();
    res.dir = new File(packageDirField.getText().trim());
    res.changeToDir = changeToDir.isSelected();
    return res;
  }
}
