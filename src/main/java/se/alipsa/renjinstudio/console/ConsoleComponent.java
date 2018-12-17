package se.alipsa.renjinstudio.console;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.eclipse.aether.repository.RemoteRepository;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngineFactory;
import se.alipsa.renjinstudio.RenjinStudio;
import se.alipsa.renjinstudio.utils.ExceptionAlert;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ConsoleComponent extends BorderPane {

    private ScriptEngine engine;

    TextArea console;

    static RemoteRepository mavenCentral() {
        return new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
    }

    static RemoteRepository renjinRepo() {
        return new RemoteRepository.Builder("renjin", "default", "https://nexus.bedatadriven.com/content/groups/public/").build();
    }

    public ConsoleComponent(RenjinStudio gui) {
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(this::clearConsole);
        FlowPane topPane = new FlowPane();
        topPane.getChildren().add(clearButton);
        setTop(topPane);

        console = new TextArea();
        setCenter(console);
        console.setText("Console");
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

        Session session = new SessionBuilder()
            .withDefaultPackages()
            .setPackageLoader(loader)
            .build();

        engine = factory.getScriptEngine(session);
    }

    public void runScript(String script) {
        Platform.runLater(() -> {
            StringWriter outputWriter = new StringWriter();
            engine.getContext().setWriter(outputWriter);
            engine.getContext().setErrorWriter(outputWriter);
            try {
                engine.eval(script);
            } catch (ScriptException | EvalException e) {
                ExceptionAlert.showAlert("Error running R code", e);
            }
            console.setText(console.getText() + "\n" + outputWriter.toString());
            try {
                outputWriter.close();
            } catch (IOException e) {
                ExceptionAlert.showAlert("Failed to close writer capturing renjin results", e);
            }
        });
    }
}
