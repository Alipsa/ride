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
import org.renjin.sexp.StringVector;
import se.alipsa.ride.model.RenjinLibrary;
import se.alipsa.ride.utils.LibraryUtils;

import java.util.Set;

public class PackagesTab extends Tab {

  final TableView<AvailablePackage> view = new TableView<>();
  ObservableList<AvailablePackage> data = FXCollections.observableArrayList();

  public PackagesTab() {
    setText("Packages");
    view.getStyleClass().add("packagesView");
    final ObservableList<TableColumn<AvailablePackage, ?>> columns = view.getColumns();

    SortedList<AvailablePackage> sortedData = new SortedList<>(data);
    sortedData.comparatorProperty().bind(view.comparatorProperty());
    view.setItems(sortedData);

    final TableColumn<AvailablePackage, Boolean> loadedColumn = new TableColumn<>( "Loaded" );
    loadedColumn.setCellValueFactory( new PropertyValueFactory<>( "loaded" ));
    loadedColumn.setCellFactory( tc -> new CheckBoxTableCell<>());
    loadedColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.1));

    final TableColumn<AvailablePackage, String> versionColumn = new TableColumn<>( "Version" );
    versionColumn.setCellValueFactory( new PropertyValueFactory<>( "version" ));
    versionColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.12));

    final TableColumn<AvailablePackage, String> descriptionColumn = new TableColumn<>( "   Description   " );
    descriptionColumn.setCellValueFactory( new PropertyValueFactory<>( "description" ));
    descriptionColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.52));

    final TableColumn<AvailablePackage, String> nameColumn = new TableColumn<>( "Library" );
    nameColumn.setCellValueFactory( new PropertyValueFactory<>( "name" ));
    nameColumn.prefWidthProperty().bind(view.widthProperty().multiply(0.26));
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
    view.setEditable( false );
    setContent(view);
  }

  public void setLoadedPackages(StringVector loadedPackages) {
    // LibraryUtils.getAvailableLibraries() is very fast, but maybe it would make more sense to run this in a separate thread
    Platform.runLater(() -> {
      Set<RenjinLibrary> availablePackages = LibraryUtils.getAvailableLibraries();
      //view.getItems().clear();
      data.clear();
      availablePackages.forEach(p -> {
        //view.getItems().add(new AvailablePackage(p,loadedPackages.indexOf(p.getPackageName()) > -1));
        data.add(new AvailablePackage(p,loadedPackages.indexOf(p.getPackageName()) > -1));
      });
    });
  }

  public static class AvailablePackage {

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
  }
}
