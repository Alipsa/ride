package se.alipsa.renjinstudio.console;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.eclipse.aether.repository.RemoteRepository;
import org.renjin.RenjinVersion;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Environment;
import se.alipsa.renjinstudio.RenjinStudio;
import se.alipsa.renjinstudio.utils.ExceptionAlert;

import java.awt.*;
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
    private RenjinStudio gui;

    static RemoteRepository mavenCentral() {
        return new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
    }

    static RemoteRepository renjinRepo() {
        return new RemoteRepository.Builder("renjin", "default", "https://nexus.bedatadriven.com/content/groups/public/").build();
    }

    public ConsoleComponent(RenjinStudio gui) {
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
        repositories.add(renjinRepo());
        repositories.add(mavenCentral());
        ClassLoader parentClassLoader = getClass().getClassLoader();

        AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, repositories);

        session = new SessionBuilder()
            .withDefaultPackages()
            .setPackageLoader(loader)
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

    public void runScript(String script) {
        gui.setWaitCursor();
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
            try (StringWriter outputWriter = new StringWriter(); StringWriter envWriter = new StringWriter()){
                engine.getContext().setWriter(outputWriter);
                engine.getContext().setErrorWriter(outputWriter);
                engine.eval(script);
                console.append(outputWriter.toString());
                Environment global = session.getGlobalEnvironment();
                gui.getEnvironmentComponent().setEnvironment(global, session.getTopLevelContext());

            }
            return null ;
            }
        };

        task.setOnSucceeded(e -> gui.setNormalCursor());
        task.setOnFailed(e -> {
            gui.setNormalCursor();
            Throwable ex = task.getException();
            String msg = "";
            if (ex instanceof org.renjin.parser.ParseException){
                msg = "Error parsing R script: ";
            } else if (ex instanceof ScriptException || ex instanceof EvalException ){
                msg = "Error running R script: ";
            } else if (ex instanceof RuntimeException ) {
                msg = "An unknown error occurred running R script: ";
            } else if (ex instanceof IOException){
                msg = "Failed to close writer capturing renjin results";
            }
            ExceptionAlert.showAlert(msg + ex.getMessage(), ex);
        });
        new Thread(task).start();
    }
}
