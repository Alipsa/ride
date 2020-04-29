package se.alipsa.ride.inout.git;


import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static se.alipsa.ride.Constants.BRIGHT_THEME;
import static se.alipsa.ride.Constants.THEME;

public class CredentialsDialog extends Dialog<Map<CredentialsDialog.KEY, String>> {

   public enum KEY {
      NAME,
      PASSWORD,
      STORE_CREDENTIALS
   };

   private TextField nameField;
   private PasswordField passwordField;
   private  CheckBox storeCheckBox;

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

      HBox storeHbox = new HBox();
      storeHbox.setSpacing(10);
      storeHbox.setPadding(new Insets(10, 10, 10, 10));
      Label storeLabel = new Label("Save permanently");
      storeHbox.getChildren().add(storeLabel);
      storeCheckBox = new CheckBox();
      storeHbox.getChildren().add(storeCheckBox);
      vBox.getChildren().add(storeHbox);

      setResizable(true);

      Ride gui = Ride.instance();
      if (gui != null) {
         String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);

         URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
         if (styleSheetUrl != null) {
            getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
         }
      }

      setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
   }

   private Map<KEY, String> createResult() {
      Map<KEY, String> result = new HashMap<>();
      result.put(KEY.NAME, nameField.getText());
      result.put(KEY.PASSWORD, passwordField.getText());
      result.put(KEY.STORE_CREDENTIALS, String.valueOf(storeCheckBox.isSelected()));
      return result;
   }
}
