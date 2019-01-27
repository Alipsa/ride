package se.alipsa.ride.inout.plot;

import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class PlotsTab extends Tab {

  TabPane imageTabpane;

  public PlotsTab() {
    setText("Plots");
    imageTabpane = new TabPane();
    setContent(imageTabpane);
  }

  public void showPlot(Node node, String[] title) {
    Tab tab = new Tab();
    imageTabpane.getTabs().add(tab);
    if (title.length > 0) {
      tab.setText(title[0]);
    }
    tab.setContent(node);

    SingleSelectionModel<Tab> imageTabsSelectionModel = imageTabpane.getSelectionModel();
    imageTabsSelectionModel.select(tab);
  }
}
