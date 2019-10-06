package se.alipsa.ride.inout;


import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AddRemoteDialog extends Dialog<Map<AddRemoteDialog.KEY, String>> {

   public enum KEY {
      NAME,
      URI
   };

   private TextField nameField;
   private TextField urlField;

   public AddRemoteDialog() {
      setTitle("Add remote");

      getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

      BorderPane pane = new BorderPane();

      VBox vBox = new VBox();
      vBox.setPadding(new Insets(10, 15, 10, 10));
      pane.setCenter(vBox);
      getDialogPane().setContent(pane);


      HBox nameHbox = new HBox();
      nameHbox.setSpacing(10);
      nameHbox.setPadding(new Insets(10, 10, 10, 10));
      Label nameLabel = new Label("Name");
      nameHbox.getChildren().add(nameLabel);
      nameField = new TextField();
      nameField.setText("origin");
      nameField.setTooltip(new Tooltip("The name of the remote to add."));

      nameHbox.getChildren().add(nameField);
      HBox.setHgrow(nameField, Priority.ALWAYS);
      vBox.getChildren().add(nameHbox);

      HBox urlHbox = new HBox();
      urlHbox.setSpacing(10);
      urlHbox.setPadding(new Insets(10, 10, 10, 10));
      Label urlLabel = new Label("URI");
      urlHbox.getChildren().add(urlLabel);
      urlField = new TextField();
      urlField.setTooltip(new Tooltip("The URL of the repository for the new remote."));

      urlHbox.getChildren().add(urlField);
      HBox.setHgrow(urlField, Priority.ALWAYS);
      vBox.getChildren().add(urlHbox);

      setResizable(true);

      setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
   }

   private Map<KEY, String> createResult() {
      Map<KEY, String> result = new HashMap<>();
      result.put(KEY.NAME, nameField.getText());
      result.put(KEY.URI, urlField.getText());
      return result;
   }
}
