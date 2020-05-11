package se.alipsa.ride.code.xmltab;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import se.alipsa.ride.Constants;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.ExceptionAlert;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static se.alipsa.ride.Constants.FLOWPANE_INSETS;

public class PackageBrowserDialog extends Dialog<Void> {

   private final Ride gui;
   private final TextArea textArea = new TextArea();
   private final TextField artifactField;

   public PackageBrowserDialog(Ride gui) {
      initOwner(gui.getStage());
      this.gui = gui;
      setTitle("Search for package in Renjin CRAN");
      getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
      BorderPane borderPane = new BorderPane();
      getDialogPane().setContent(borderPane);

      Insets insets = new Insets(5,5,5,5);

      textArea.setPrefColumnCount(40);
      textArea.setPrefRowCount(8);
      borderPane.setCenter(textArea);


      FlowPane topPane = new FlowPane();
      topPane.setPadding(FLOWPANE_INSETS);
      borderPane.setTop(topPane);
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
   }

   private void lookupArtifact(ActionEvent actionEvent) {
      String artifact = artifactField.getText().trim();
      String baseUrl = "https://nexus.bedatadriven.com/content/groups/public/org/renjin/cran/";
      String url = baseUrl + artifact + "/maven-metadata.xml";
      try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
         Document doc = docBuilder.parse(url);
         Element versioning = (Element)doc.getDocumentElement().getElementsByTagName("versioning").item(0);
         Element release = (Element)versioning.getElementsByTagName("release").item(0);
         String version = release.getTextContent();

         StringBuilder sb = new StringBuilder("Latest version is:")
            .append("\n<dependency>")
            .append("\n\t").append("<groupId>org.renjin.cran</groupId>")
            .append("\n\t").append("<artifactId>").append(artifact).append("</artifactId>")
            .append("\n\t").append("<version>").append(version).append("</version>")
            .append("\n</dependency>\n\n")
            .append("Package status: http://packages.renjin.org/package/org.renjin.cran/")
            .append(artifact);

         textArea.setText(sb.toString());

      } catch (IOException | ParserConfigurationException | SAXException e) {
         ExceptionAlert.showAlert("Failed to get metadata from " + url, e);
      }
   }
}
