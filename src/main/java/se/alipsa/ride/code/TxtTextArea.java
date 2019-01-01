package se.alipsa.ride.code;

import javafx.scene.control.TextArea;

import java.io.File;

public class TxtTextArea extends TextArea implements TabTextArea  {

    private File file;

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String getTextContent() {
        return getText();
    }

    @Override
    public String getAllTextContent() {
        return getText();
    }
}
