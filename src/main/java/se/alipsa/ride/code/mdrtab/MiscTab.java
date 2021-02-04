package se.alipsa.ride.code.mdrtab;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.alipsa.ride.code.munin.MuninTab;
import se.alipsa.ride.code.xmltab.XmlTextArea;
import se.alipsa.ride.model.MuninReport;

public class MiscTab extends Tab {

  private final TextField reportNameTF;
  private final TextArea descriptionTA;
  private final TextField groupTF;
  private final ComboBox<String> typeCB;
  private final XmlTextArea inputTA;

  public MiscTab(MuninTab parentTab) {
    MuninReport muninReport = parentTab.getMuninReport();
    setText("Config");
    VBox vBox = new VBox();
    vBox.setSpacing(5);

    HBox nameHbox = new HBox(5);
    reportNameTF = new TextField(muninReport.getReportName());
    nameHbox.getChildren().addAll(new Label("Report name:"), reportNameTF);
    vBox.getChildren().add(nameHbox);
    if (muninReport.getReportName() != null && muninReport.getReportName().length() > 0) {
      reportNameTF.setDisable(true);
    }
    //vBox.getChildren().addAll(new Label("Report name"), reportNameTF);


    //HBox descriptionHbox = new HBox(5);
    descriptionTA = new TextArea(muninReport.getDescription());
    descriptionTA.setPrefRowCount(2);
    //descriptionTA.setText(muninReport.getDescription());
    //descriptionHbox.getChildren().addAll(new Label("Description"), descriptionTA);
    vBox.getChildren().addAll(new Label("Description"), descriptionTA);

    HBox groupAndTypeBox = new HBox(5);
    vBox.getChildren().add(groupAndTypeBox);
    groupTF = new TextField(muninReport.getReportGroup());
    groupAndTypeBox.getChildren().addAll(new Label("Group"), groupTF);
    typeCB = new ComboBox<>();
    typeCB.getItems().addAll("UNMANAGED", "MDR");
    typeCB.getSelectionModel().select(muninReport.getReportType());
    groupAndTypeBox.getChildren().addAll(new Label("Type"), typeCB);

    //HBox inputHbox = new HBox(5);
    inputTA = new XmlTextArea(parentTab);
    //inputTA.setPrefSize(100, 200);
    inputTA.replaceContentText(0, 0, muninReport.getInputContent());
    //inputTA.replaceText(muninReport.getInputContent());
    //inputHbox.getChildren().addAll(new Label("Input parameters"), inputTA);
    VBox.setVgrow(inputTA, Priority.ALWAYS);
    vBox.getChildren().addAll(new Label("Input parameters"), inputTA);

    //vBox.getChildren().addAll(nameHbox, descriptionHbox, inputHbox);
    setContent(vBox);
  }

  public String getReportName() {
    return reportNameTF.getText();
  }

  public String getDescription() {
    return descriptionTA.getText();
  }

  public String getReportGroup() {
    return groupTF.getText();
  }

  public String getReportType() {
    return typeCB.getValue();
  }

  public String getInputContent() {
    return inputTA.getAllTextContent();
  }
}
