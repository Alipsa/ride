package se.alipsa.ride.menu;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.eclipse.aether.repository.RemoteRepository;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalOptionsDialog extends Dialog<Map<String,Object>> {

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

        TableView<Repo> reposTable = new TableView();
        List<RemoteRepository> repos = gui.getConsoleComponent().getRemoteRepositories();
        TableColumn idCol = new TableColumn("id");
        idCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("id"));
        TableColumn typeCol = new TableColumn("type");
        typeCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("type"));
        TableColumn urlCol = new TableColumn("url");
        urlCol.setCellValueFactory(new PropertyValueFactory<Repo,String>("url"));
        urlCol.setMinWidth(450);
        reposTable.getColumns().addAll(idCol, typeCol, urlCol);

        reposTable.setItems(createObservable(repos));
        reposTable.setEditable(false); // todo change to true when editing can be saved
        grid.add(reposTable, 1, 0);

        getDialogPane().setPrefSize(800, 400);
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        //getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        setResizable(true);


    }

    ObservableList<Repo> createObservable(List<RemoteRepository> repos ) {
        List<Repo> repoList = new ArrayList<>();
        for (RemoteRepository repo : repos) {
            repoList.add(new Repo(repo.getId(), repo.getContentType(), repo.getUrl()));
        }
        ObservableList<Repo> list = FXCollections.observableArrayList(repoList);
        return list;
    }


}
