package se.alipsa.renjinstudio.console;

import javafx.scene.control.TextArea;
import org.renjin.script.RenjinScriptEngineFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ConsoleComponent extends TextArea {

    private ScriptEngine engine;

    public ConsoleComponent() {
        setText("Console");
        initRenjin();
    }

    private void initRenjin() {
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        engine = factory.getScriptEngine();
    }

    public void runScript(String script) {

        StringWriter outputWriter = new StringWriter();
        engine.getContext().setWriter(outputWriter);
        PrintWriter writer = null;
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            writer = new PrintWriter(outputWriter);
            e.printStackTrace(writer);
        }
        setText(getText() + "\n" + outputWriter.toString());
        try {
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            writer.close();
        }
    }
}
