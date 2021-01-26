package se.alipsa.ride.menu;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.GuiUtils;

public class PasswordDialog extends Dialog<String> {

  public PasswordDialog(Ride gui, String text, String userName) {
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    GuiUtils.addStyle(gui, this);
    setTitle("Password is required");
    setContentText(text);
    GridPane pane = new GridPane();
    pane.setPadding(new Insets(5));
    getDialogPane().setContent(pane);

    pane.add(new Label("Username: "), 0, 2);
    TextField userNameTf = new TextField(userName);
    pane.add(userNameTf,1, 2);

    pane.add(new Label("Password: "), 0, 3);
    PasswordField pwdTf = new PasswordField();
    pane.add(pwdTf, 1, 3);

    setResultConverter(callback -> callback == ButtonType.OK ? pwdTf.getText() : null);
  }
}
