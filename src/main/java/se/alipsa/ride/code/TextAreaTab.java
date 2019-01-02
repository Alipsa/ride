package se.alipsa.ride.code;

import javafx.scene.control.Tab;
import se.alipsa.ride.code.TabTextArea;

public abstract class TextAreaTab extends Tab implements TabTextArea {

    public String getTitle() {
        return getText();
    }

    public void setTitle(String title) {
        setText(title);
    }
}
