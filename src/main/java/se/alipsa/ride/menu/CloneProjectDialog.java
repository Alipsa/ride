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
import se.alipsa.ride.utils.GuiUtils;

import java.io.File;

public class CloneProjectDialog extends Dialog<CloneProjectDialogResult> {

  private static final Logger log = LogManager.getLogger();

  private final TextField urlField = new TextField();
  private final TextField dirField = new TextField();
  Button chooseDirButton = new Button("Browse...");

  File selectedDirectory;
  private final Ride gui;

  public CloneProjectDialog(Ride gui) {
    this.gui = gui;
    setTitle("Clone git project");
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    Insets insets = new Insets(10, 15, 10, 10);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(insets);
    getDialogPane().setContent(grid);

    Label urlLabel = new Label("URL");
    grid.add(urlLabel,0,0);
    grid.add(urlField, 1,0, 3, 1);
    chooseDirButton.setDisable(true);
    urlField.focusedProperty().addListener((arg0, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        checkAndMaybeEnableButtons();
      }
    });

    Label dirLabel = new Label("Target dir");
    grid.add(dirLabel,0,1);
    selectedDirectory = gui.getInoutComponent().getRootDir().getParentFile();
    dirField.setText(selectedDirectory.getAbsolutePath());
    dirField.setPrefColumnCount(35);
    dirField.setDisable(true);
    grid.add(dirField, 1,1, 3, 1);

    grid.add(chooseDirButton, 4, 1);
    chooseDirButton.setOnAction(this::chooseProjectDir);
    getDialogPane().setPrefSize(640, 250);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    setResizable(true);

    GuiUtils.addStyle(gui, this);

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
  }

  private void checkAndMaybeEnableButtons() {
    if (urlField.getText().trim().equals("")) {
      return;
    }
    if (urlField.getText().endsWith(".git")) {
      chooseDirButton.setDisable(false);
    }
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
      String dir = urlField.getText().substring(urlField.getText().lastIndexOf('/'), urlField.getLength() - ".git".length());
      selectedDirectory = new File(selectedDirectory, dir);
      dirField.setText(selectedDirectory.getAbsolutePath());
      getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
    }
  }

  private CloneProjectDialogResult createResult() {
    CloneProjectDialogResult res = new CloneProjectDialogResult();
    res.url = urlField.getText();
    res.targetDir = new File(dirField.getText());
    return res;
  }
}
