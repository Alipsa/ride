package se.alipsa.ride.console;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.eclipse.aether.repository.RemoteRepository;
import org.renjin.RenjinVersion;
import org.renjin.aether.AetherFactory;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.Environment;
import org.renjin.sexp.StringVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.Animation;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.FileUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class ConsoleComponent extends BorderPane {

    private ScriptEngine engine;
    private Session session;

    private ImageView globeView;
    Button statusButton;
    private ConsoleTextArea console;
    private Ride gui;
    private List<RemoteRepository> remoteRepositories;

    private Timeline scriptExecutionTimeline;

    public static final Repo RENJIN_REPO = asRepo(AetherFactory.renjinRepo());
    public static final Repo MVN_CENTRAL_REPO = asRepo(AetherFactory.mavenCentral());
    public static final String REMOTE_REPOSITORIES_PREF = "ConsoleComponent.RemoteRepositories";

    private static final Image IMG_RUNNING = new Image(FileUtils
        .getResourceUrl("image/running.png").toExternalForm(), 30, 30, true, true);
    private static final Image IMG_WAITING = new Image(FileUtils
        .getResourceUrl("image/waiting.png").toExternalForm(), 30, 30, true, true);

    private static final Logger log = LoggerFactory.getLogger(ConsoleComponent.class);

    public ConsoleComponent(Ride gui) {
        this.gui = gui;
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> console.clear());
        FlowPane topPane = new FlowPane();
        topPane.setPadding(new Insets(1, 10, 1,5));
        topPane.setHgap(10);

        //spinningGlobe = createSpinner(5 * 1000);
        //spinningGlobe.setCycleCount(20);

        globeView = new ImageView();
        statusButton = new Button();
        statusButton.setOnAction(e -> interruptR());
        statusButton.setGraphic(globeView);
        waiting();

        topPane.getChildren().addAll(statusButton, clearButton);
        setTop(topPane);

        console = new ConsoleTextArea();
        setCenter(console);
        initRenjin(getStoredRemoteRepositories());
    }

    private Animation createSpinner(int duration) {
        Image[] sequence = new Image[24];
        for (int i = 0; i < 24; i++) {
            sequence[i] = new Image(FileUtils.getResourceUrl("image/spinner/" + i + ".gif").toExternalForm(),
                    30, 30, true, true);
        }
        return new Animation(sequence, duration);
    }

    private void initRenjin(List<Repo> repos) {
        RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
        remoteRepositories = new ArrayList<>();
        remoteRepositories.addAll(asRemoteRepositories(repos));
        ClassLoader parentClassLoader = getClass().getClassLoader();

        AetherPackageLoader loader = new AetherPackageLoader(parentClassLoader, remoteRepositories);

        session = new SessionBuilder()
                .withDefaultPackages()
                .setPackageLoader(loader) // allows library to work without having to include in the pom
                .setClassLoader(loader.getClassLoader()) //allows imports in r code to work
                .build();

        engine = factory.getScriptEngine(session);
        String greeting = "* Renjin " + RenjinVersion.getVersionName() + " *";
        String surround = getStars(greeting.length());
        console.append(surround);
        console.append(greeting);
        console.append(surround + "\n>");
    }

    private List<RemoteRepository> asRemoteRepositories(List<Repo> items) {
        List<RemoteRepository> list = new ArrayList<>();
        for (Repo repo: items) {
            list.add(new RemoteRepository.Builder(repo.getId(), repo.getType(), repo.getUrl()).build());
        }
        return list;
    }

    private List<Repo> asRepos(List<RemoteRepository> repositories) {
        List<Repo> list = new ArrayList<>();
        for (RemoteRepository repo: repositories) {
            list.add(asRepo(repo));
        }
        return list;
    }

    private static Repo asRepo(RemoteRepository repo) {
        return new Repo(repo.getId(), repo.getContentType(), repo.getUrl());
    }

    private List<Repo> getStoredRemoteRepositories() {
        List<Repo> list = new ArrayList<>();
        String remotes = gui.getPrefs().get(REMOTE_REPOSITORIES_PREF, null);
        log.info("Remotes from prefs are: {}", remotes);
        if (remotes == null) {
            log.warn("No stored prefs for remote repos, adding defaults");
            addDefaultRepos(list);
            return list;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            list = mapper.readValue(remotes, new TypeReference<List<Repo>>(){});
        } catch (InvalidDefinitionException e) {
            e.printStackTrace();
            log.warn("Something is wrong with the pref key {}, deleting it to start fresh", REMOTE_REPOSITORIES_PREF);
            gui.getPrefs().remove(REMOTE_REPOSITORIES_PREF);
            addDefaultRepos(list);
        } catch (IOException e) {
            e.printStackTrace();
            addDefaultRepos(list);
        }
        return list;
    }

    private void addDefaultRepos(List<Repo> list) {
        log.info("add renjin repo");
        list.add(RENJIN_REPO);
        log.info("add maven central repo");
        list.add(MVN_CENTRAL_REPO);
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
        initRenjin(getStoredRemoteRepositories());
        gui.getEnvironmentComponent().clearEnvironment();
    }

    /** This will not work as the Scriptengine must run in the jfx thread
     * (Platform.runLater()) to allow interaction with the gui e.g. printing plots
     */
    public void interruptR() {
        log.info("Interrupting runnning script");
        if (Animation.Status.RUNNING.equals(scriptExecutionTimeline.getStatus())) {
            console.append("Interrupting Renjin...\n>");
            scriptExecutionTimeline.stop();
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.info("Sleep was interrupted");
        }
    }

    public void runScript(String script, String title) {
        running();
        try (StringWriter outputWriter = new StringWriter()){
                executeScriptAndReport(script, title, outputWriter);
        } catch (IOException e) {
            waiting();
            ExceptionAlert.showAlert("Failed to close writer capturing renjin results: ", e);
        }
    }

    private void executeScriptAndReport(String script, String title, StringWriter outputWriter) {
        // A bit unorthodox use of timeline but this allows us to interrupt a running script
        // since the running script must be on the jfx thread to allow interaction with the gui
        // e.g. for plots, this is the best way to do that.
        scriptExecutionTimeline = new Timeline();
        KeyFrame scriptFrame = new KeyFrame(Duration.seconds(1), evt -> {
            try {
                engine.put("inout", gui.getInoutComponent());
                engine.getContext().setWriter(outputWriter);
                engine.getContext().setErrorWriter(outputWriter);
                outputWriter.write(title + "\n");
                engine.eval(script);
                session.printWarnings();
                session.clearWarnings();
                outputWriter.write(">");
                console.append(outputWriter.toString(), true);
            } catch (org.renjin.parser.ParseException e) {
                Platform.runLater(() ->
                ExceptionAlert.showAlert("Error parsing R script: " + e.getMessage(), e));
            } catch(ScriptException | EvalException e) {
                Platform.runLater(() ->
                ExceptionAlert.showAlert("Error running R script: " + e.getMessage(), e));
            } catch (RuntimeException e) {
                Platform.runLater(() ->
                ExceptionAlert.showAlert("A runtime error occurred running R script: " + e.getMessage(), e));
            } catch (Exception e) {
                Platform.runLater(() ->
                ExceptionAlert.showAlert("Exception thrown when running script: " + e.getMessage(), e));
            }
        });

        KeyFrame pkgFrame = new KeyFrame(Duration.seconds(1), evt -> {
            try {
                Environment global = session.getGlobalEnvironment();
                gui.getEnvironmentComponent().setEnvironment(global, session.getTopLevelContext());
                StringVector pkgs = (StringVector) engine.eval("(.packages())");
                gui.getInoutComponent().setPackages(pkgs);
                waiting();
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        });

        scriptExecutionTimeline.getKeyFrames().addAll(scriptFrame, pkgFrame);
        scriptExecutionTimeline.setCycleCount(1);
        scriptExecutionTimeline.play();

    }

    public List<Repo> getRemoteRepositories() {
        return asRepos(remoteRepositories);
    }

    public void setRemoterepositories(List<Repo> repos) {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, repos);
            String r = writer.toString();
            log.info("Writing repos to prefs as: {}", r);
            gui.getPrefs().put(REMOTE_REPOSITORIES_PREF, r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("initializing Renjin with {} repos", repos.size());
        initRenjin(repos);
    }

    private void running() {
        Platform.runLater(() -> {
            globeView.setImage(IMG_RUNNING);
            statusButton.setTooltip(new Tooltip("Script is running, click to abort"));
            showTooltip(statusButton);
        });
        sleep(10);
    }

    private void waiting() {
        Platform.runLater(() -> {
            globeView.setImage(IMG_WAITING);
            statusButton.setTooltip(new Tooltip("Engine is idle"));
        });
    }

    public void showTooltip(Control control)
    {
        Tooltip customTooltip = control.getTooltip();
        Stage owner = gui.getStage();
        Point2D p = control.localToScene(10.0, 20.0);

        customTooltip.setAutoHide(true);

        customTooltip.show(owner, p.getX()
            + control.getScene().getX() + control.getScene().getWindow().getX(), p.getY()
            + control.getScene().getY() + control.getScene().getWindow().getY());

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                Platform.runLater(() -> customTooltip.hide());
            }
        };
        timer.schedule(task,800);
    }
}
