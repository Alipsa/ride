package se.alipsa.renjinstudio.console;

import javafx.scene.control.TextArea;
import org.renjin.script.RenjinScriptEngineFactory;

import javax.script.ScriptEngine;

public class ConsoleWindow extends TextArea {

    ScriptEngine engine;

    public ConsoleWindow() {
        setText("Console");
        initRenjin();
    }

    private void initRenjin() {
        // create a script engine manager:
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        // create a Renjin engine:
        engine = factory.getScriptEngine();
    }
}
