package se.alipsa.ride.menu;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.renjin.aether.AetherPackageLoader;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.IntField;
import se.alipsa.ride.utils.TableViewWithVisibleRowCount;

import java.util.List;

import static se.alipsa.ride.Constants.*;
import static se.alipsa.ride.console.ConsoleComponent.PACKAGE_LOADER_PREF;
import static se.alipsa.ride.console.ConsoleTextArea.CONSOLE_MAX_LENGTH_DEFAULT;
import static se.alipsa.ride.menu.GlobalOptions.CONSOLE_MAX_LENGTH_PREF;

public class GlobalOptionsDialog extends Dialog<GlobalOptions> {

  private TableViewWithVisibleRowCount<Repo> reposTable;
  private ComboBox<Class> pkgLoaderCb;
  private IntField intField;
  private ComboBox<String> themes;

  public GlobalOptionsDialog(Ride gui) {
    setTitle("Global options");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(10, 15, 10, 10));
    getDialogPane().setContent(grid);

    Label pkgLoaderLabel = new Label("Package Loader");
    grid.add(pkgLoaderLabel, 0, 0);
    pkgLoaderCb = new ComboBox<>();
    pkgLoaderCb.setConverter(new PackageLoaderClassConverter());
    pkgLoaderCb.getItems().addAll(ClasspathPackageLoader.class, AetherPackageLoader.class);
    String defaultPkgLoader = gui.getPrefs().get(PACKAGE_LOADER_PREF, AetherPackageLoader.class.getSimpleName());
    if (AetherPackageLoader.class.getSimpleName().equals(defaultPkgLoader)) {
      pkgLoaderCb.getSelectionModel().select(AetherPackageLoader.class);
    } else {
      pkgLoaderCb.getSelectionModel().select(ClasspathPackageLoader.class);
    }
    grid.add(pkgLoaderCb, 1, 0);

    Label reposLabel = new Label("Remote Repositories");
    grid.add(reposLabel, 0, 1);

    reposTable = new TableViewWithVisibleRowCount<>();
    List<Repo> repos = gui.getConsoleComponent().getRemoteRepositories();

    TableColumn<Repo, String> idCol = new TableColumn<>("id");
    idCol.setCellValueFactory(new PropertyValueFactory<Repo, String>("id"));
    idCol.setCellFactory(TextFieldTableCell.forTableColumn());
    idCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setId(t.getNewValue())
    );

    TableColumn<Repo, String> typeCol = new TableColumn<>("type");
    typeCol.setCellValueFactory(new PropertyValueFactory<Repo, String>("type"));
    typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
    typeCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setType(t.getNewValue())
    );

    TableColumn<Repo, String> urlCol = new TableColumn<>("url");
    urlCol.setCellValueFactory(new PropertyValueFactory<Repo, String>("url"));
    urlCol.setCellFactory(TextFieldTableCell.forTableColumn());
    urlCol.setOnEditCommit(t ->
        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
            .setUrl(t.getNewValue())
    );

    reposTable.setRowFactory(tableView -> {
      final TableRow<Repo> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem addMenuItem = new MenuItem("add row");
      addMenuItem.setOnAction(event -> {
        addRow(new Repo());
      });

      final Menu addDefault = new Menu("add default");
      final MenuItem addBedataDriven = new MenuItem("Renjin repo");
      addBedataDriven.setOnAction(this::addRenjinRepo);
      final MenuItem addMavenCentral = new MenuItem("Maven Central");
      addMavenCentral.setOnAction(this::addMvnCentralRepo);
      addDefault.getItems().addAll(addBedataDriven, addMavenCentral);

      final MenuItem removeMenuItem = new MenuItem("delete row");
      removeMenuItem.setOnAction(event -> reposTable.getItems().remove(row.getItem()));
      contextMenu.getItems().addAll(addMenuItem, addDefault, removeMenuItem);

      // Set context menu on row, but use a binding to make it only show for non-empty rows:
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );
      return row;
    });


    urlCol.setMinWidth(450);
    reposTable.getColumns().addAll(idCol, typeCol, urlCol);

    reposTable.setItems(createObservable(repos));
    reposTable.setNumberOfRows(reposTable.getItems().size());
    reposTable.setEditable(true);

    if (ClasspathPackageLoader.class.equals(pkgLoaderCb.getValue())) {
      reposTable.setDisable(true);
    }

    grid.add(reposTable, 1, 1);

    pkgLoaderCb.valueProperty().addListener(e -> {
      if (ClasspathPackageLoader.class.equals(pkgLoaderCb.getValue())) {
        reposTable.setDisable(true);
      } else {
        reposTable.setDisable(false);
      }
    });

    Label consoleMaxSizeLabel = new Label("Console max size");
    grid.add(consoleMaxSizeLabel, 0, 2);
    intField = new IntField(1000, Integer.MAX_VALUE, gui.getPrefs().getInt(CONSOLE_MAX_LENGTH_PREF, CONSOLE_MAX_LENGTH_DEFAULT));
    grid.add(intField, 1, 2);

    Label styleTheme = new Label("Style theme");
    grid.add(styleTheme, 0, 3);
    themes = new ComboBox<>();
    themes.getItems().addAll(DARK_THEME, BRIGHT_THEME, BLUE_THEME);
    themes.getSelectionModel().select(gui.getPrefs().get(THEME, BRIGHT_THEME));
    grid.add(themes, 1, 3);

    getDialogPane().setPrefSize(800, 400);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    setResizable(true);

    setResultConverter(button -> button == ButtonType.OK ? createResult() : null);

  }

  private void addRow(Repo repo) {
    reposTable.getItems().add(repo);
    reposTable.setNumberOfRows(reposTable.getItems().size());
  }

  private void addRenjinRepo(ActionEvent actionEvent) {
    addRow(ConsoleComponent.RENJIN_REPO);
  }

  private void addMvnCentralRepo(ActionEvent actionEvent) {
    addRow(ConsoleComponent.MVN_CENTRAL_REPO);
  }

  ObservableList<Repo> createObservable(List<Repo> repos) {
    if (repos == null) {
      return FXCollections.emptyObservableList();
    }
    ObservableList<Repo> list = FXCollections.observableArrayList(repos);
    return list;
  }

  GlobalOptions createResult() {
    GlobalOptions result = new GlobalOptions();
    result.put(GlobalOptions.REMOTE_REPOSITORIES, reposTable.getItems());
    result.put(GlobalOptions.PKG_LOADER, pkgLoaderCb.getSelectionModel().getSelectedItem());
    result.put(CONSOLE_MAX_LENGTH_PREF, intField.getValue());
    result.put(THEME, themes.getValue());
    return result;
  }


}
