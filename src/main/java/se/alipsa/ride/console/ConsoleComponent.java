package se.alipsa.ride.console;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.eclipse.aether.repository.RemoteRepository;
import org.renjin.RenjinVersion;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.ParseException;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ConsoleComponent extends BorderPane {

    private ScriptEngine engine;
    private Session session;

    private ConsoleTextArea console;
    private Ride gui;

    public ConsoleComponent(Ride gui) {
        this.gui = gui;
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(this::clearConsole);
        FlowPane topPane = new FlowPane();
        topPane.getChildren().add(clearButton);
        setTop(topPane);

        console = new ConsoleTextArea();
        setCenter(console);
        initRenjin();
    }

    private void clearConsole(ActionEvent actionEvent) {
        console.clear();
    }

    private void initRenjin() {
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        List<RemoteRepository> repositories = new ArrayList<>();
        repositories.add(AetherFactory.renjinRepo());
        repositories.add(AetherFactory.mavenCentral());
        ClassLoader parentClassLoader = getClass().getClassLoader();

        AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, repositories);

        session = new SessionBuilder()
                .withDefaultPackages()
                .setPackageLoader(loader) // allows library to work without having to include in the pom
                .setClassLoader(loader.getClassLoader()) //allows imports in r code to work
                .build();

        engine = factory.getScriptEngine(session);
        String greeting = "* Renjin " + RenjinVersion.getVersionName() + " *";
        String surround = getStars(greeting.length());
        console.append(surround, true);
        console.append(greeting);
        console.append(surround + "\n");
    }

    private String getStars(int length) {
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            buf.append("*");
        }
        return buf.toString();
    }

    public void restartR() {
        console.append("Restarting Renjin..\n");
        initRenjin();
        gui.getEnvironmentComponent().clearEnvironment();
    }

    // TODO: figure out why wait cursor is not set on console text area
    public void runScript(String script) {
        gui.setWaitCursor();
        console.setCursor(Cursor.WAIT);

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
            try (StringWriter outputWriter = new StringWriter()){
                try {
                    executeScriptAndReport(script, outputWriter);
                } catch (RuntimeException e) {
                   throw new RuntimeScriptException(e);
                }

            }
            return null ;
            }
        };

        task.setOnSucceeded(e -> {
            gui.setNormalCursor();
            console.setCursor(Cursor.DEFAULT);
        });
        task.setOnFailed(e -> {
            gui.setNormalCursor();
            console.setCursor(Cursor.DEFAULT);
            Throwable ex = task.getException();
            String msg = "";
            if (ex instanceof org.renjin.parser.ParseException) {
                msg = "Error parsing R script: ";
            } else if (ex instanceof ScriptException || ex instanceof EvalException ){
                msg = "Error running R script: ";
            } else if (ex instanceof IOException){
                msg = "Failed to close writer capturing renjin results";
            } else if (ex instanceof RuntimeScriptException ) {
                msg = "An unknown error occurred running R script: ";
            } else if (ex instanceof Exception){
                msg = "Exception thrown when running script";
            }
            ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
        });
        new Thread(task).start();
    }

    private void executeScriptAndReport(String script, StringWriter outputWriter) throws ScriptException {
        engine.put("inout", gui.getInoutComponent());
        engine.getContext().setWriter(outputWriter);
        engine.getContext().setErrorWriter(outputWriter);
        engine.eval(script);
        outputWriter.write("\n");
        session.printWarnings();
        session.clearWarnings();
        console.append(outputWriter.toString());
        Environment global = session.getGlobalEnvironment();
        Platform.runLater(() -> {
            try {
                gui.getEnvironmentComponent().setEnvironment(global, session.getTopLevelContext());
                StringVector pkgs = (StringVector) engine.eval("(.packages())");
                gui.getInoutComponent().setPackages(pkgs);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        });
    }

}
