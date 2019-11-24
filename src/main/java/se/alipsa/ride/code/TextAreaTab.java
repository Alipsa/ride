package se.alipsa.ride.code;

import static se.alipsa.ride.Constants.*;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.inout.FileItem;
import se.alipsa.ride.utils.FileUtils;

import java.util.Optional;

public abstract class TextAreaTab extends Tab implements TabTextArea {

  private static final Image IMG_SAVE = new Image(FileUtils
      .getResourceUrl("image/save.png").toExternalForm(), ICON_WIDTH, ICON_HEIGHT, true, true);
  protected boolean isChanged = false;
  protected Button saveButton = new Button();
  protected Ride gui;
  private Tooltip saveToolTip;
  private CodeType codeType;
  protected BorderPane pane;
  protected FlowPane buttonPane;
  protected TreeItem<FileItem> treeItem;

  public TextAreaTab(Ride gui, CodeType codeType) {
    this.gui = gui;
    this.codeType = codeType;

    super.setTooltip(new Tooltip(codeType.getDisplayValue()));
    saveButton.setGraphic(new ImageView(IMG_SAVE));
    saveButton.setDisable(true);
    saveButton.setOnAction(a -> gui.getMainMenu().saveContent(this));
    saveToolTip = new Tooltip("Save");
    saveButton.setTooltip(saveToolTip);

    pane = new BorderPane();
    setContent(pane);
    buttonPane = new FlowPane();
    buttonPane.setHgap(5);
    buttonPane.setPadding(FLOWPANE_INSETS);
    pane.setTop(buttonPane);

    buttonPane.getChildren().add(saveButton);

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

  public abstract CodeTextArea getCodeArea();

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

  public CodeType getCodeType() {
    return codeType;
  }

  public TreeItem<FileItem> getTreeItem() {
    return treeItem;
  }

  public void setTreeItem(TreeItem<FileItem> treeItem) {
    this.treeItem = treeItem;
    setTooltip(new Tooltip(treeItem.getValue().getFile().getAbsolutePath()));
  }
}
