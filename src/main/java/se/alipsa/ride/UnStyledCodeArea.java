package se.alipsa.ride;

import org.fxmisc.richtext.CodeArea;

public class UnStyledCodeArea extends CodeArea {

  public UnStyledCodeArea() {
    getStylesheets().clear();
    getStyleClass().add("styled-text-area");
    getStyleClass().add("code-area");
  }
}
