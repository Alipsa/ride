package se.alipsa.ride.code;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.fxmisc.richtext.CodeArea;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.util.Optional;

import static se.alipsa.ride.Constants.ICON_HEIGHT;
import static se.alipsa.ride.Constants.ICON_WIDTH;

public abstract class TextAreaTab extends Tab implements TabTextArea {

  private static final Image IMG_SAVE = new Image(FileUtils
      .getResourceUrl("image/save.png").toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  protected boolean isChanged = false;
  protected Button saveButton = new Button();
  protected Ride gui;
  private Tooltip saveToolTip;
  private TabType tabType;

  public TextAreaTab(Ride gui, TabType tabType) {
    this.gui = gui;
    this.tabType = tabType;
    saveButton.setGraphic(new ImageView(IMG_SAVE));
    saveButton.setDisable(true);
    saveButton.setOnAction(a -> gui.getMainMenu().saveContent(this));
    saveToolTip = new Tooltip("Save");
    saveButton.setTooltip(saveToolTip);

    super.setOnCloseRequest(event -> {
          if (isChanged()) {
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(yes, no);
            alert.setTitle("File is not saved");
            alert.setHeaderText("Save file " + getTitle());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == yes) {
              gui.getMainMenu().saveContent(this);
            } else {
              // ... user chose CANCEL or closed the dialog
            }
          }
        }
    );
  }

  public String getTitle() {
    return getText();
  }

  public void setTitle(String title) {
    setText(title);
    saveToolTip.setText("Save " + title.replace("*", ""));
  }

  public abstract CodeArea getCodeArea();

  public void contentChanged() {
    setTitle(getTitle() + "*");
    isChanged = true;
    saveButton.setDisable(false);
  }

  public void contentSaved() {
    setTitle(getTitle().replace("*", ""));
    isChanged = false;
    saveButton.setDisable(true);
  }

  public boolean isChanged() {
    return isChanged;
  }

  public Ride getGui() {
    return gui;
  }

  public TabType getTabType() {
    return tabType;
  }
}
