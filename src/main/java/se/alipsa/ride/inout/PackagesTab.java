package se.alipsa.ride.inout;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.renjin.sexp.StringVector;
import se.alipsa.ride.Ride;
import se.alipsa.ride.model.RenjinLibrary;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.LibraryUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class PackagesTab extends Tab {

  private static final Logger LOG = LogManager.getLogger();
  private final Ride gui;
  private final ObservableList<AvailablePackage> data = FXCollections.observableArrayList();

  public PackagesTab(Ride gui) {
    this.gui = gui;
    setText("Packages");
    TableView<AvailablePackage> view = new TableView<>();
    view.getStyleClass().add("packagesView");
    final ObservableList<TableColumn<AvailablePackage, ?>> columns = view.getColumns();

    SortedList<AvailablePackage> sortedData = new SortedList<>(data);
    sortedData.comparatorProperty().bind(view.comparatorProperty());
    view.setItems(sortedData);

    final TableColumn<AvailablePackage, Boolean> loadedColumn = new TableColumn<>( "Loaded" );
    loadedColumn.setCellValueFactory( new PropertyValueFactory<>( "loaded" ));
    loadedColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
    loadedColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.1));
    loadedColumn.setEditable(true);

    final TableColumn<AvailablePackage, String> versionColumn = new TableColumn<>( "Version" );
    versionColumn.setCellValueFactory( new PropertyValueFactory<>( "version" ));
    versionColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.12));
    versionColumn.setEditable(false);

    final TableColumn<AvailablePackage, String> descriptionColumn = new TableColumn<>( "   Description   " );
    descriptionColumn.setCellValueFactory( new PropertyValueFactory<>( "description" ));
    descriptionColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.52));
    descriptionColumn.setEditable(false);

    final TableColumn<AvailablePackage, String> nameColumn = new TableColumn<>( "Library" );
    nameColumn.setCellValueFactory( new PropertyValueFactory<>( "name" ));
    nameColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.26));
    nameColumn.setEditable(false);
    /*
    nameColumn.prefWidthProperty().bind(view.widthProperty()
        .subtract(loadedColumn.widthProperty().get())
        .subtract(versionColumn.widthProperty().get())
        .subtract(descriptionColumn.widthProperty().get())
    );

     */

    columns.addAll(loadedColumn, nameColumn, versionColumn, descriptionColumn);

    nameColumn.setSortType(TableColumn.SortType.ASCENDING);
    view.getSortOrder().setAll(nameColumn);
    view.setPlaceholder(new Label("No libraries (Renjin extensions) loaded"));
    view.setEditable( true );
    setContent(view);
  }

  public void setLoadedPackages(StringVector loadedPackages) {
    // LibraryUtils.getAvailableLibraries() is very fast, but maybe it would make more sense to run this in a separate thread
    Platform.runLater(() -> {
      // AetherPackageLoader might have picked up new packages, so we need to do this each time
      Set<RenjinLibrary> availablePackages;
      try {
        availablePackages = LibraryUtils.getAvailableLibraries(Ride.instance().getConsoleComponent().getRenjinClassLoader());
      } catch (IOException | RuntimeException e) {
        ExceptionAlert.showAlert("Failed to scan for available libraries", e);
        return;
      }
      //view.getItems().clear();
      data.clear();
      availablePackages.forEach(p -> {
        //view.getItems().add(new AvailablePackage(p,loadedPackages.indexOf(p.getPackageName()) > -1));
        data.add(new AvailablePackage(p,loadedPackages.indexOf(p.getPackageName()) > -1));
      });
    });
  }

  public class AvailablePackage {

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty loaded = new SimpleBooleanProperty();
    private final StringProperty version = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final RenjinLibrary renjinLibrary;

    public AvailablePackage( RenjinLibrary renjinLibrary, boolean loaded ) {
      this.renjinLibrary = renjinLibrary;
      this.name.set(renjinLibrary.getFullName());
      this.version.set(renjinLibrary.getVersion());
      this.loaded.set( loaded );
      this.description.set(renjinLibrary.getTitle());

      loadedProperty().addListener(
          (observableValue, oldVal, newVal) -> {
            LOG.trace(name.get() + " changed from " + oldVal + " to " + newVal);
            try {
              LibraryUtils.loadOrUnloadLibrary(gui.getConsoleComponent(), this, newVal);
            } catch (Exception e) {
              ExceptionAlert.showAlert(e.getMessage(), e);
            }
          }
      );
    }

    public StringProperty nameProperty() { return name; }
    public StringProperty versionProperty  () { return version; }
    public BooleanProperty loadedProperty() { return loaded; }
    public StringProperty descriptionProperty  () { return description; }
    public RenjinLibrary getRenjinLibrary() { return renjinLibrary; }

    @Override
    public String toString() {
      return renjinLibrary.getFullName();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      AvailablePackage that = (AvailablePackage) o;
      return name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }
}
