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
import org.eclipse.aether.repository.RemoteRepository;
import se.alipsa.ride.Ride;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.model.Repo;
import se.alipsa.ride.utils.TableViewWithVisibleRowCount;

import java.util.ArrayList;
import java.util.List;

public class GlobalOptionsDialog extends Dialog<GlobalOptions> {

    TableViewWithVisibleRowCount<Repo> reposTable;

    public GlobalOptionsDialog(Ride gui) {
        setTitle("Global options");
        //setGraphic(new ImageView(new Image(FileUtils.getResourceUrl("image/logo2.png").toExternalForm())));


        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 15, 10, 10));
        getDialogPane().setContent(grid);

        Label reposLabel = new Label("Remote Repositories");
        grid.add(reposLabel,0,0);

        reposTable = new TableViewWithVisibleRowCount<>();
        List<Repo> repos = gui.getConsoleComponent().getRemoteRepositories();

        TableColumn<Repo, String> idCol = new TableColumn<>("id");
        idCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("id"));
        idCol.setCellFactory(TextFieldTableCell.forTableColumn());
        idCol.setOnEditCommit( t ->
                        (t.getTableView().getItems().get(t.getTablePosition().getRow()))
                                .setId(t.getNewValue())
        );

        TableColumn<Repo, String> typeCol = new TableColumn<>("type");
        typeCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("type"));
        typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typeCol.setOnEditCommit( t ->
                (t.getTableView().getItems().get(t.getTablePosition().getRow()))
                        .setType(t.getNewValue())
        );

        TableColumn<Repo, String>  urlCol = new TableColumn<>("url");
        urlCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("url"));
        urlCol.setCellFactory(TextFieldTableCell.forTableColumn());
        urlCol.setOnEditCommit( t ->
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
                            .then((ContextMenu)null)
                            .otherwise(contextMenu)
            );
            return row ;
        });


        urlCol.setMinWidth(450);
        reposTable.getColumns().addAll(idCol, typeCol, urlCol);

        reposTable.setItems(createObservable(repos));
        reposTable.setNumberOfRows(reposTable.getItems().size());
        reposTable.setEditable(true);
        grid.add(reposTable, 1, 0);

        getDialogPane().setPrefSize(800, 400);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        //getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        setResizable(true);

        setResultConverter(button -> button == ButtonType.OK ?  createResult() : null);

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

    ObservableList<Repo> createObservable(List<Repo> repos ) {
        if (repos == null) {
            return FXCollections.emptyObservableList();
        }
        ObservableList<Repo> list = FXCollections.observableArrayList(repos);
        return list;
    }

    GlobalOptions createResult() {
        GlobalOptions result = new GlobalOptions();
        result.put(GlobalOptions.REMOTE_REPOSITORIES, reposTable.getItems());
        return result;
    }


}
