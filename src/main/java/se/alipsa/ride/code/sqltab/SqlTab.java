package se.alipsa.ride.code.sqltab;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.TextAreaTab;

import java.io.File;

public class SqlTab extends TextAreaTab {

  SqlTextArea sqlTextArea;

  public SqlTab(String title, Ride gui) {
    super(gui);
    setTitle(title);
    BorderPane pane = new BorderPane();

    FlowPane buttonPane = new FlowPane();
    buttonPane.setHgap(5);
    buttonPane.setPadding(new Insets(5, 10, 5, 5));
    pane.setTop(buttonPane);

    buttonPane.getChildren().add(saveButton);

    Button runButton = new Button("Run");
    buttonPane.getChildren().add(runButton);

    ComboBox<String> connectionCombo= new ComboBox<>();
    connectionCombo.getItems().add("conn");
    connectionCombo.getItems().add("conn2");
    buttonPane.getChildren().add(connectionCombo);

    sqlTextArea = new SqlTextArea(this);
    VirtualizedScrollPane<SqlTextArea> scrollPane = new VirtualizedScrollPane<>(sqlTextArea);
    pane.setCenter(scrollPane);
    setContent(pane);
  }

  @Override
  public File getFile() {
    return sqlTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    sqlTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return sqlTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return sqlTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    sqlTextArea.replaceContentText(start, end, content);
  }
}
