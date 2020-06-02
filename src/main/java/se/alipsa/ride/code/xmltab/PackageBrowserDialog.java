package se.alipsa.ride.code.xmltab;

import static se.alipsa.ride.Constants.FLOWPANE_INSETS;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PackageBrowserDialog extends Dialog<Void> {

   private final Ride gui;
   private final TextArea textArea = new TextArea();
   private final TextField artifactField;
   private final TextField groupField;
   private final ComboBox<LookupUrl> repoCombo;

   public PackageBrowserDialog(Ride gui) {
      initOwner(gui.getStage());
      this.gui = gui;
      setTitle("Search for package info");
      getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
      BorderPane borderPane = new BorderPane();
      getDialogPane().setContent(borderPane);

      textArea.setPrefColumnCount(40);
      textArea.setPrefRowCount(8);
      borderPane.setCenter(textArea);


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
         ExceptionAlert.showAlert("Failed to get metadata from " + url, e);
      }
   }
}
