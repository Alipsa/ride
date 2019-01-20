package se.alipsa.ride.utils;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class IntField extends TextField {
  final private IntegerProperty value;
  final private int minValue;
  final private int maxValue;

  public int  getValue()                 { return value.getValue(); }
  public void setValue(int newValue)     { value.setValue(newValue); }
  public IntegerProperty valueProperty() { return value; }

  public IntField(int minValue, int maxValue, int initialValue) {
    if (minValue > maxValue)
      throw new IllegalArgumentException(
          "IntField min value " + minValue + " greater than max value " + maxValue
      );
    if (maxValue < minValue)
      throw new IllegalArgumentException(
          "IntField max value " + minValue + " less than min value " + maxValue
      );
    if (!((minValue <= initialValue) && (initialValue <= maxValue)))
      throw new IllegalArgumentException(
          "IntField initialValue " + initialValue + " not between " + minValue + " and " + maxValue
      );

    // initialize the field values.
    this.minValue = minValue;
    this.maxValue = maxValue;
    value = new SimpleIntegerProperty(initialValue);
    setText(initialValue + "");

    final IntField intField = this;

    // make sure the value property is clamped to the required range
    // and update the field's text to be in sync with the value.
    value.addListener((observableValue, oldValue, newValue) -> {
      if (newValue == null) {
        intField.setText("");
      } else {
        if (newValue.intValue() < intField.minValue) {
          value.setValue(intField.minValue);
          return;
        }

        if (newValue.intValue() > intField.maxValue) {
          value.setValue(intField.maxValue);
          return;
        }

        if (newValue.intValue() == 0 && (textProperty().get() == null || "".equals(textProperty().get()))) {
          // no action required, text property is already blank, we don't need to set it to 0.
        } else {
          intField.setText(newValue.toString());
        }
      }
    });

    // restrict key input to numerals.
    this.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
      if (!"0123456789".contains(keyEvent.getCharacter())) {
        keyEvent.consume();
      }
    });

    // ensure any entered values lie inside the required range.
    this.textProperty().addListener((observableValue, oldValue, newValue) -> {
      if (newValue == null || "".equals(newValue)) {
        value.setValue(0);
        return;
      }

      final int intValue = Integer.parseInt(newValue);

      if (intField.minValue > intValue || intValue > intField.maxValue) {
        textProperty().setValue(oldValue);
      }

      value.set(Integer.parseInt(textProperty().get()));
    });
  }
}