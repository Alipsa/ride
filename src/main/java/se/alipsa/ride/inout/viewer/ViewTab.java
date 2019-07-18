package se.alipsa.ride.inout.viewer;

import static se.alipsa.ride.Constants.KEY_CODE_COPY;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import se.alipsa.ride.model.Table;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ViewTab extends Tab {

  private TabPane viewPane;

  public ViewTab() {
    setText("Viewer");
    viewPane = new TabPane();
    setContent(viewPane);
    viewPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
  }

  public void viewTable(Table table, String... title) {
    List<String> colList = table.getColList();
    List<List<Object>> rowList = table.getRowList();
    NumberFormat numberFormatter = NumberFormat.getInstance();
    numberFormatter.setGroupingUsed(false);

    TableView<List<String>> tableView = new TableView<>();
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.setOnKeyPressed(event -> {
      if (KEY_CODE_COPY.match(event)) {
        copySelectionToClipboard(tableView);
      }
    });

    tableView.setRowFactory(tv -> {
      final TableRow<List<String>> row = new TableRow<>();
      final ContextMenu contextMenu = new ContextMenu();
      final MenuItem copyMenuItem = new MenuItem("copy");
      copyMenuItem.setOnAction(event -> copySelectionToClipboard(tv));
      contextMenu.getItems().addAll(copyMenuItem);
      row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
              .then((ContextMenu) null)
              .otherwise(contextMenu)
      );
      return row;
    });

    for (int i = 0; i < colList.size(); i++) {
      final int j = i;
      String colName = colList.get(i);
      TableColumn<List<String>, String> col = new TableColumn<>(colName);
      tableView.getColumns().add(col);
      col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
    }
    ObservableList<List<String>> data = FXCollections.observableArrayList();
    for (List<Object> row : rowList) {
      List<String> obsRow = new ArrayList<>();
      for (Object obj : row) {
        if (obj instanceof Number) {
          obsRow.add(numberFormatter.format(obj));
        } else {
          obsRow.add(obj + "");
        }
      }
      data.add(obsRow);
    }
    tableView.setItems(data);
    Tab tab = new Tab();
    String tabTitle = " (" + rowList.size() + ")";
    if (title.length > 0) {
      tabTitle = title[0] + tabTitle;
    }
    tab.setText(tabTitle);
    viewPane.getTabs().add(tab);
    tab.setContent(tableView);

    SingleSelectionModel<Tab> selectionModel = viewPane.getSelectionModel();
    selectionModel.select(tab);
  }

  @SuppressWarnings("rawtypes")
  private void copySelectionToClipboard(final TableView<?> table) {
    final Set<Integer> rows = new TreeSet<>();
    for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
      rows.add(tablePosition.getRow());
    }
    final StringBuilder strb = new StringBuilder();
    boolean firstRow = true;
    for (final Integer row : rows) {
      if (!firstRow) {
        strb.append('\n');
      }
      firstRow = false;
      boolean firstCol = true;
      for (final TableColumn<?, ?> column : table.getColumns()) {
        if (!firstCol) {
          strb.append('\t');
        }
        firstCol = false;
        final Object cellData = column.getCellData(row);
        strb.append(cellData == null ? "" : cellData.toString());
      }
    }
    final ClipboardContent clipboardContent = new ClipboardContent();
    clipboardContent.putString(strb.toString());
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }
}
