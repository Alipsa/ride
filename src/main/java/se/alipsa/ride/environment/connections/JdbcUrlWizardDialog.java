package se.alipsa.ride.environment.connections;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;
import se.alipsa.ride.utils.IntField;

import java.net.URL;

import static se.alipsa.ride.Constants.*;

public class JdbcUrlWizardDialog extends Dialog<ConnectionInfo> {
   private static final Logger log = LogManager.getLogger();

   private final Ride gui;

   private final ComboBox<String> driver = new ComboBox<>();
   private final TextField server = new TextField();
   private final IntField port = new IntField(0, 65535, 5432);
   private final TextField database = new TextField();
   private final TextField url = new TextField();

   private static final int TF_LENGTH = 250;

   private String urlTemplate = "";

   public JdbcUrlWizardDialog(Ride gui) {
      this.gui = gui;

      setTitle("Create Connection URL Wizard");

      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(10, 15, 10, 10));
      getDialogPane().setContent(grid);

      getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      //getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

      int rowIndex = 0;

      driver.getItems().addAll(
         DRV_POSTGRES,
         DRV_MYSQL,
         DRV_H2,
         DRV_SQLSERVER,
         DRV_SQLLITE,
         DRV_FIREBIRD,
         DRV_DERBY
      );
      driver.getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
         if (! isNowFocused) {
            driver.setValue(driver.getEditor().getText());
         }
      });
      driver.setOnAction(this::addDefaultsForDriver);

      grid.add(new Label("Driver: "), 0, ++rowIndex);
      grid.add(driver, 1, rowIndex);

      server.setPrefWidth(TF_LENGTH);
      grid.add(new Label("Server: "), 0,++rowIndex);
      grid.add(server, 1, rowIndex);
      server.setOnAction(e -> updateUrl());
      server.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
         if (!newPropertyValue) {
            updateUrl();
         }
      });

      grid.add(new Label("Port: "), 0, ++rowIndex);
      grid.add(port, 1, rowIndex);
      port.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
         if (!newPropertyValue) {
            updateUrl();
         }
      });

      grid.add(new Label("Database: "), 0, ++rowIndex);
      grid.add(database, 1, rowIndex);
      database.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
         if (!newPropertyValue) {
            updateUrl();
         }
      });

      grid.add(new Label("Url: "), 0, ++rowIndex);
      grid.add(url, 1, rowIndex);
      url.setEditable(false);


      setResizable(true);
      String styleSheetPath = gui.getPrefs().get(THEME, BRIGHT_THEME);
      URL styleSheetUrl = FileUtils.getResourceUrl(styleSheetPath);
      if (styleSheetUrl != null) {
         getDialogPane().getStylesheets().add(styleSheetUrl.toExternalForm());
      }

      setResultConverter(button -> button == ButtonType.OK ? createResult() : null);
   }

   private void addDefaultsForDriver(ActionEvent actionEvent) {
      String driverName = driver.getValue();
      server.setText("localhost");
      database.setText("mydatabase");
      switch (driverName) {
         case DRV_POSTGRES:
            port.setValue(5432);
            urlTemplate = "jdbc:postgresql://{server}:{port}/{database}";
            break;
         case DRV_SQLSERVER:
            port.setValue(1433);
            urlTemplate = "jdbc:sqlserver://{server}:{port};databaseName={database}";
            break;
         case DRV_MYSQL:
            port.setValue(3306);
            urlTemplate = "jdbc:mysql://{server}:{port}/{database}";
            break;
         case DRV_DERBY:
            port.setValue(1527);
            urlTemplate = "jdbc:derby://{server}:{port}/{database}";
            break;
         case DRV_FIREBIRD:
            port.setValue(3050);
            urlTemplate = "jdbc:firebirdsql://{server}:{port}/{database}";
            break;
         case DRV_H2:
            port.setValue(9092);
            urlTemplate = "jdbc:h2:tcp://{server}:{port}/{database}";
            break;
         case DRV_SQLLITE:
            port.clear();
            server.clear();
            urlTemplate = "jdbc:sqlite:{database}";
            break;
         default:
            server.setText("unknown");
            port.setValue(1025);
            database.setText("");
      }
      updateUrl();
   }

   private void updateUrl() {
      String urlString = urlTemplate
         .replace("{server}", server.getText())
         .replace("{port}", port.getText())
         .replace("{database}", database.getText());
      url.setText(urlString);
   }

   private ConnectionInfo createResult() {
      ConnectionInfo ci = new ConnectionInfo();
      ci.setDriver(driver.getValue());
      ci.setUrl(url.getText());
      return ci;
   }
}
