package se.alipsa.ride.inout.viewer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class ViewTab extends Tab {

  TabPane viewPane;

  public ViewTab() {
    setText("Viewer");
    viewPane = new TabPane();
    setContent(viewPane);
  }

  public void viewTable(List<String> colList, List<List<Object>> rowList, String... title) {
    TableView<List<String>> tableview = new TableView<>();

    for (int i = 0; i < colList.size(); i++) {
      final int j = i;
      String colName = colList.get(i);
      TableColumn<List<String>, String> col = new TableColumn<>(colName);
      tableview.getColumns().add(col);
      col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
    }
    ObservableList<List<String>> data = FXCollections.observableArrayList();
    for (List row: rowList) {
      List<String> obsRow = new ArrayList<>();
      for (Object obj : row) {
        obsRow.add(obj + "");
      }
      data.add(obsRow);
    }
    tableview.setItems(data);
    Tab tab = new Tab();
    String tabTitle = " (" + rowList.size() + ")";
    if (title.length > 0) {
      tabTitle = title[0] + tabTitle;
    }
    tab.setText(tabTitle);
    viewPane.getTabs().add(tab);
    tab.setContent(tableview);

    SingleSelectionModel<Tab> selectionModel = viewPane.getSelectionModel();
    selectionModel.select(tab);
  }
}
