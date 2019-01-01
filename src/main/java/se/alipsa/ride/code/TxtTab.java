package se.alipsa.ride.code;

public class TxtTab extends TextAreaTab {

    TxtTextArea txtTextArea;

    public TxtTab(String title, String content) {
        setText(title);

        txtTextArea = new TxtTextArea();

        setContent(txtTextArea);

        txtTextArea.replaceText(0, 0, content);
    }

    @Override
    TabTextArea getTabTextArea() {
        return txtTextArea;
    }
}
