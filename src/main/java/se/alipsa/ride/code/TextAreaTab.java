package se.alipsa.ride.code;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabTextArea;

import java.util.Optional;

public abstract class TextAreaTab extends Tab implements TabTextArea {

    private boolean isChanged = false;

    public TextAreaTab(Ride gui) {
        super.setOnCloseRequest( event -> {
            if(isChanged()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("File is not saved");
                alert.setHeaderText("Save file " + getTitle());

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
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
    }

    public void contentChanged() {
        setTitle(getTitle() + "*");
        isChanged = true;
    }

    public void contentSaved() {
        setTitle(getTitle().replace("*", ""));
        isChanged = false;
    }

    boolean isChanged() {
        return isChanged;
    }

}
