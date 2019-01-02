package se.alipsa.ride.code.txttab;

import se.alipsa.ride.code.TabTextArea;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class TxtTab extends TextAreaTab {

    TxtTextArea txtTextArea;

    public TxtTab(String title) {
        setText(title);
        txtTextArea = new TxtTextArea();
        setContent(txtTextArea);
    }

    public TxtTab(String title, String content) {
        setText(title);
        txtTextArea = new TxtTextArea();
        setContent(txtTextArea);
        txtTextArea.replaceText(0, 0, content);
    }

    @Override
    public File getFile() {
        return txtTextArea.getFile();
    }

    @Override
    public void setFile(File file) {
        txtTextArea.setFile(file);
    }

    @Override
    public String getTextContent() {
        return txtTextArea.getTextContent();
    }

    @Override
    public String getAllTextContent() {
        return txtTextArea.getAllTextContent();
    }

    @Override
    public void replaceText(int start, int end, String content) {
        txtTextArea.replaceText(start, end, content);
    }

    @Override
    public String getTitle() {
        return getText();
    }

    @Override
    public void setTitle(String title) {
        setText(title);
    }
}
