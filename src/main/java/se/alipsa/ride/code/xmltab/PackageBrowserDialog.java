package se.alipsa.ride.code.xmltab;

import static se.alipsa.ride.Constants.FLOWPANE_INSETS;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import se.alipsa.ride.Ride;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/*
   --------------------------
   |    top input, action   |
   --------------------------
   |                        |
   |    center textarea     |
   |                        |
   --------------------------
   |    bottom, usage hint  |
   --------------------------
 */
public class PackageBrowserDialog extends Dialog<Void> {

   private final Ride gui;
   private final TextArea textArea = new TextArea();
   private final TextField artifactField;
   private final TextField groupField;
   private final ComboBox<LookupUrl> repoCombo;
   private Stage browserStage = null;
   
   private static final Logger log = LogManager.getLogger();

   public PackageBrowserDialog(Ride gui) {
      initOwner(gui.getStage());
      setResizable(true);
      getDialogPane().setPrefWidth(800);
      this.gui = gui;
      setTitle("Search for package info");
      getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
      BorderPane borderPane = new BorderPane();
      getDialogPane().setContent(borderPane);

      HBox topPane = new HBox();
      topPane.setPadding(FLOWPANE_INSETS);
      borderPane.setTop(topPane);

      Label groupLabel = new Label("Group name");
      groupLabel.setPadding(FLOWPANE_INSETS);
      topPane.getChildren().add(groupLabel);

      groupField = new TextField("org.renjin.cran");
      groupField.setPrefWidth(150);
      groupField.setPadding(FLOWPANE_INSETS);
      topPane.getChildren().add(groupField);

      Label artifactLabel = new Label("Artifact name");
      artifactLabel.setPadding(FLOWPANE_INSETS);
      topPane.getChildren().add(artifactLabel);

      artifactField = new TextField();
      artifactField.setPrefWidth(150);
      artifactField.setPadding(FLOWPANE_INSETS);
      topPane.getChildren().add(artifactField);

      Button searchButton = new Button("Search");
      searchButton.setPadding(FLOWPANE_INSETS);
      searchButton.setOnAction(this::lookupArtifact);
      topPane.getChildren().add(searchButton);

      repoCombo = new ComboBox<>();
      HBox.setMargin(repoCombo, new Insets(0,5,0,10));
      repoCombo.getItems().addAll(LookupUrl.RENJIN_CRAN, LookupUrl.MAVEN_CENTRAL);
      repoCombo.getSelectionModel().select(LookupUrl.RENJIN_CRAN);
      repoCombo.setOnAction(e -> {
         if (repoCombo.getSelectionModel().getSelectedItem().equals(LookupUrl.RENJIN_CRAN)) {
            groupField.setText("org.renjin.cran");
         }
      });
      topPane.getChildren().add(repoCombo);

      textArea.setPrefColumnCount(40);
      textArea.setPrefRowCount(8);
      borderPane.setCenter(textArea);

      Label hintLabel = new Label("Hint: copy useful text from the search result before closing the dialog.");
      borderPane.setBottom(hintLabel);

      setOnCloseRequest(eh -> {
         if (browserStage != null) {
            browserStage.close();
         }
         close();
      });
   }

   private enum LookupUrl {
      RENJIN_CRAN("Renjin CRAN", "https://nexus.bedatadriven.com/content/groups/public/"),
      MAVEN_CENTRAL("Maven Central", "https://repo1.maven.org/maven2/");

      String name;
      String baseUrl;

      LookupUrl(String name, String baseUrl) {
         this.name = name;
         this.baseUrl = baseUrl;
      }
   }

   private void lookupArtifact(ActionEvent actionEvent) {
      String group = groupField.getText().trim();
      String groupUrlpart = group.replace('.', '/') + "/";
      String artifact = artifactField.getText().trim();
      LookupUrl lookupUrl = repoCombo.getSelectionModel().getSelectedItem();
      String baseUrl = lookupUrl.baseUrl;
      String url = baseUrl + groupUrlpart + artifact + "/maven-metadata.xml";
      try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(url);
         Element versioning = (Element)doc.getDocumentElement().getElementsByTagName("versioning").item(0);
         Element release = (Element)versioning.getElementsByTagName("release").item(0);
         String version = release.getTextContent();

         StringBuilder sb = new StringBuilder("Latest version is:")
            .append("\n<dependency>")
            .append("\n\t").append("<groupId>").append(group).append("</groupId>")
            .append("\n\t").append("<artifactId>").append(artifact).append("</artifactId>")
            .append("\n\t").append("<version>").append(version).append("</version>")
            .append("\n</dependency>\n\n");

             if (lookupUrl.equals(LookupUrl.RENJIN_CRAN)) {
               sb.append("Package status: http://packages.renjin.org/package/org.renjin.cran/")
                    .append(artifact);
             }

         textArea.setText(sb.toString());

      } catch (IOException | ParserConfigurationException | SAXException e) {
         log.info("Failed to get metadata from {}, opening search browser", url);
         openMavenSearchBrowser();
      }
   }

   private void openMavenSearchBrowser() {
      gui.setWaitCursor();
      WebView browser = new WebView();
      WebEngine webEngine = browser.getEngine();
      BorderPane borderPane = new BorderPane();
      borderPane.setCenter(browser);
      String cssPath = gui.getStyleSheets().get(0);
      webEngine.setUserStyleSheetLocation(cssPath);
      browser.getStylesheets().addAll(gui.getStyleSheets());
      Scene scene = new Scene(borderPane, 1280, 800);
      Platform.runLater(() -> {
         browser.setCursor(Cursor.WAIT);
         scene.setCursor(Cursor.WAIT);
      });
      browserStage = new Stage();
      browserStage.initModality(Modality.NONE);
      browserStage.setTitle("Artifact not found, showing repository search...");
      browserStage.setScene(scene);
      browserStage.sizeToScene();
      browserStage.show();
      String group = groupField.getText().trim();
      String artifact = artifactField.getText().trim();
      String url;
      if (LookupUrl.RENJIN_CRAN.equals(repoCombo.getValue())) {
         String searchString = artifact.length() > 0 ? artifact : group;
         url = "http://packages.renjin.org/packages/search?q=" + searchString;
      } else {
         url = "https://mvnrepository.com/search?q=" + group + "+" + artifact;
      }
      webEngine.load(url);
      browserStage.toFront();
      browserStage.requestFocus();
      browserStage.setAlwaysOnTop(false);
      browser.setCursor(Cursor.DEFAULT);
      scene.setCursor(Cursor.DEFAULT);
      gui.setNormalCursor();
   }
}
