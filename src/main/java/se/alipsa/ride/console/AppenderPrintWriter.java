package se.alipsa.ride.console;

import java.io.Writer;

public class AppenderPrintWriter extends Writer {

    private ConsoleTextArea console;
    private StringBuilder text = null;

    public AppenderPrintWriter(ConsoleTextArea console, boolean... cacheText) {
        this.console = console;
        if (cacheText.length > 0 && cacheText[0]) {
            this.text = new StringBuilder();
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        String content = new String(cbuf, off, len);
        console.appendFx(content);
        if (text != null) {
            text.append(content);
        }
    }

    public String getCachedText() {
        return text.toString();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
