package se.alipsa.ride.code;

public enum CodeType {
  TXT("Text file"), R("R script"), XML("Xml file"), JAVA("Java code"),
  SQL("SQL script"), MD("Markdown file");

  CodeType(String displayValue) {
    this.displayValue = displayValue;
  }

  private String displayValue;

  public String getDisplayValue() {
    return displayValue;
  }
}
