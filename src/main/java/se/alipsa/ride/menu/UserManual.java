package se.alipsa.ride.menu;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.net.URL;
import java.util.Objects;

public class UserManual {

  private static final Logger log = LogManager.getLogger();
  private static final String NORMAL_BUTTON = "usermanual-normal-button";
  private static final String CLICKED_BUTTON = "usermanual-clicked-button";

  private final WebEngine webEngine;
  private final Button featuresButton;
  private final Button rideShortCuts;
  private final Button interactingWithRideButton;
  private final Button examplesButton;
  private final Button packagesButton;
  private final Stage stage;
  private final URL featuresUrl;

  public UserManual(Ride gui) {
    WebView browser = new WebView();
    webEngine = browser.getEngine();
    BorderPane borderPane = new BorderPane();
    borderPane.setCenter(browser);
    borderPane.getStylesheets().addAll(gui.getStyleSheets());
    String cssPath = gui.getStyleSheets().get(0);
    log.debug("Adding style sheet {}", cssPath);
    webEngine.setUserStyleSheetLocation(cssPath);
    browser.getStylesheets().addAll(gui.getStyleSheets());

    FlowPane linkPane = new FlowPane();
    borderPane.setTop(linkPane);

    featuresUrl = FileUtils.getResourceUrl("manual/features.html");
    URL interactionUrl = FileUtils.getResourceUrl("manual/InteractingWithRide.html");
    URL shortcutsUrl = FileUtils.getResourceUrl("manual/KeyBoardShortcuts.html");
    URL examplesUrl = FileUtils.getResourceUrl("manual/examples.html");
    URL packagesUrl = FileUtils.getResourceUrl("manual/packages.html");

    featuresButton = new Button("Ride features");
    featuresButton.getStyleClass().add(NORMAL_BUTTON);
    featuresButton.setOnAction(e -> loadPage(featuresUrl, featuresButton));

    rideShortCuts = new Button("Ride keyboard shortcuts");
    rideShortCuts.getStyleClass().add(NORMAL_BUTTON);
    rideShortCuts.setOnAction(e -> loadPage(shortcutsUrl, rideShortCuts));

    interactingWithRideButton = new Button("Interacting with Ride");
    interactingWithRideButton.getStyleClass().add(NORMAL_BUTTON);
    interactingWithRideButton.setOnAction(e -> loadPage(interactionUrl, interactingWithRideButton));

    examplesButton = new Button("Tips and tricks");
    examplesButton.getStyleClass().add(NORMAL_BUTTON);
    examplesButton.setOnAction(e -> loadPage(examplesUrl, examplesButton));

    packagesButton = new Button("Packages");
    packagesButton.getStyleClass().add(NORMAL_BUTTON);
    packagesButton.setOnAction(e -> loadPage(packagesUrl, packagesButton));

    linkPane.getChildren().addAll(featuresButton, rideShortCuts, interactingWithRideButton, packagesButton, examplesButton);

    webEngine.setCreatePopupHandler(
        (PopupFeatures config) -> {
          WebView nBrowser = new WebView();
          // Always use bright theme as external links will usually look funny when coming from dark mode
          URL themeUrl = FileUtils.getResourceUrl(Constants.BRIGHT_THEME);
          if (themeUrl != null) {
            nBrowser.getEngine().setUserStyleSheetLocation(themeUrl.toExternalForm());
          }
          nBrowser.getStylesheets().addAll(gui.getStyleSheets());
          Scene scene = new Scene(nBrowser);
          Stage stage = new Stage();
          stage.setScene(scene);
          stage.show();
          return nBrowser.getEngine();
        });


    Scene dialog = new Scene(borderPane, 1024, 768);
    stage = new Stage();
    stage.setWidth(1024);
    stage.setHeight(768);
    stage.initModality(Modality.NONE);
    stage.initOwner(gui.getStage());
    stage.setTitle("User Manual");
    stage.setScene(dialog);
  }

  public void loadPage(URL url, Button source) {
    clearButtonStyles();
    webEngine.load(Objects.requireNonNull(url).toExternalForm());
    source.getStyleClass().add(CLICKED_BUTTON);
  }

  public void show() {
    stage.show();
    loadPage(featuresUrl, featuresButton);
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);
  }

  void clearButtonStyles() {
    clearButtonStyle(featuresButton);
    clearButtonStyle(rideShortCuts);
    clearButtonStyle(interactingWithRideButton);
    clearButtonStyle(examplesButton);
    clearButtonStyle(packagesButton);
  }

  void clearButtonStyle(Button button) {
    button.getStyleClass().remove(CLICKED_BUTTON);
    button.getStyleClass().add(NORMAL_BUTTON);
  }
}
