package se.alipsa.ride.code;

public enum CodeType {
  TXT("Text file"),
  R("R script"), XML("Xml file"),
  SQL("SQL script"),
  MD("Markdown file"),
  MDR("mdr file"),
  MR("Munin report"),
  JAVA("Java code"),
  GROOVY("Groovy code"),
  JAVA_SCRIPT("Javascript code");

  CodeType(String displayValue) {
    this.displayValue = displayValue;
  }

  private final String displayValue;

  public String getDisplayValue() {
    return displayValue;
  }
}
