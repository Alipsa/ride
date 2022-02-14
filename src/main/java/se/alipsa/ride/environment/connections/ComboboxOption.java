package se.alipsa.ride.environment.connections;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class ComboboxOption extends HBox {

  ComboBox<String> cb = new ComboBox<>();

  public ComboboxOption(String label, String... options) {
    Label lbl = new Label(label);
    setSpacing(5);
    for (String option : options) {
      cb.getItems().add(option);
    }
    getChildren().addAll(lbl, cb);
  }

  public void setOnAction(EventHandler<ActionEvent> eventHandler) {
    cb.setOnAction(eventHandler);
  }

  public String getValue() {
    return cb.getValue();
  }
}
