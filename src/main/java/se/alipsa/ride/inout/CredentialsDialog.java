package se.alipsa.ride.inout;


import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class CredentialsDialog extends Dialog<Map<CredentialsDialog.KEY, String>> {

   public enum KEY {
      NAME,
      PASSWORD
   };

   private TextField nameField;
   private PasswordField passwordField;

   public CredentialsDialog() {
      setTitle("Add credentials");

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
      nameField.setTooltip(new Tooltip("The name of the remote to add."));

      nameHbox.getChildren().add(nameField);
      HBox.setHgrow(nameField, Priority.ALWAYS);
      vBox.getChildren().add(nameHbox);

      HBox pwdHbox = new HBox();
      pwdHbox.setSpacing(10);
      pwdHbox.setPadding(new Insets(10, 10, 10, 10));
      Label pwdLabel = new Label("Password");
      pwdHbox.getChildren().add(pwdLabel);
      passwordField = new PasswordField();
      passwordField.setTooltip(new Tooltip("The password for the remote."));

      pwdHbox.getChildren().add(passwordField);
      HBox.setHgrow(passwordField, Priority.ALWAYS);
      vBox.getChildren().add(pwdHbox);

      setResizable(true);

      setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
   }

   private Map<KEY, String> createResult() {
      Map<KEY, String> result = new HashMap<>();
      result.put(KEY.NAME, nameField.getText());
      result.put(KEY.PASSWORD, passwordField.getText());
      return result;
   }
}
