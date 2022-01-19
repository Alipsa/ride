package se.alipsa.ride.inout.git;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.StoredConfig;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.GuiUtils;

import java.net.URL;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

public class GitConfigureDialog extends Dialog<ConfigResult> {

  private Git git;
  private ComboBox<CoreConfig.AutoCRLF> autoCrLfCombo;

  public GitConfigureDialog(Git git) {
    this.git = git;
    StoredConfig config = git.getRepository().getConfig();
    setTitle("Global options");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    Label autoCrlfLabel = new Label("autoCrLf");
    grid.add(autoCrlfLabel, 0,0);
    autoCrLfCombo = new ComboBox<>();

    autoCrLfCombo.getItems().addAll(CoreConfig.AutoCRLF.values());
    autoCrLfCombo.getSelectionModel().select(config.getEnum(ConfigConstants.CONFIG_CORE_SECTION, null,
        ConfigConstants.CONFIG_KEY_AUTOCRLF, CoreConfig.AutoCRLF.INPUT));
    grid.add(autoCrLfCombo, 1,0);

    Ride gui = Ride.instance();
    GuiUtils.addStyle(gui, this);

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
  }

  private ConfigResult createResult() {
    ConfigResult res = new ConfigResult();
    res.autoCRLF = autoCrLfCombo.getValue();
    return res;
  }
}
