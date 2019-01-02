package se.alipsa.ride.code;

import java.io.File;

public interface TabTextArea  {

    File getFile();

    void setFile(File file);

    String getTextContent();

    String getAllTextContent();

    void replaceText(int start, int end, String content);
}
