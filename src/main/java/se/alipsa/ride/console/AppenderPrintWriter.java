package se.alipsa.ride.console;

import java.io.Writer;

public class AppenderPrintWriter extends Writer {

    private ConsoleTextArea console;

    public AppenderPrintWriter(ConsoleTextArea console) {
        this.console = console;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        console.appendFx(new String(cbuf, off, len));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
