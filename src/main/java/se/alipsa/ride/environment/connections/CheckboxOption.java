package se.alipsa.ride.environment.connections;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class CheckboxOption extends HBox {

  CheckBox cb = new CheckBox();

  public CheckboxOption(String label) {
    Label lbl = new Label(label);
    setSpacing(5);
    getChildren().addAll(cb, lbl);
  }

  public void setOnAction(EventHandler<ActionEvent> actionEvent) {
    cb.setOnAction(actionEvent);
  }

  public boolean isSelected() {
    return cb.isSelected();
  }
}
