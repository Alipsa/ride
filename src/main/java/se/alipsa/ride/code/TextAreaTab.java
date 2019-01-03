package se.alipsa.ride.code;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TabTextArea;

import java.util.Optional;

public abstract class TextAreaTab extends Tab implements TabTextArea {

    protected boolean isChanged = false;

    public TextAreaTab(Ride gui) {
        super.setOnCloseRequest( event -> {
            if(isChanged()) {
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(yes, no);
                alert.setTitle("File is not saved");
                alert.setHeaderText("Save file " + getTitle());

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == yes){
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

    public boolean isChanged() {
        return isChanged;
    }

}
