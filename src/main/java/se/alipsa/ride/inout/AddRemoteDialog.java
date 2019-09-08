package se.alipsa.ride.inout;


import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

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

      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(10, 15, 10, 10));
      getDialogPane().setContent(grid);

      Label nameLabel = new Label("Name");
      grid.add(nameLabel,0,0);
      nameField = new TextField();
      nameField.setPrefColumnCount(10);
      nameField.setTooltip(new Tooltip("The name of the remote to add."));
      grid.add(nameField, 1,0);

      Label urlLabel = new Label("URI");
      grid.add(urlLabel,0,1);
      urlField = new TextField();
      urlField.setPrefColumnCount(15);
      urlField.setTooltip(new Tooltip("The URL of the repository for the new remote."));
      grid.add(urlField, 1,1);

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
