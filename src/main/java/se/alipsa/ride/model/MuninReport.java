package se.alipsa.ride.model;

public class MuninReport {

  private String reportName;
  private String description;
  private String definition;
  private String inputContent;
  private String reportType;
  private String reportGroup;

  public MuninReport() {}

  public MuninReport(String reportName, String reportType) {
    this.reportName = reportName;
    this.reportType = reportType;
  }



  public String getReportName() {
    return reportName;
  }

  public void setReportName(String reportName) {
    this.reportName = reportName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getInputContent() {
    return inputContent;
  }

  public void setInputContent(String inputContent) {
    this.inputContent = inputContent;
  }

  public String getReportType() {
    return reportType;
  }

  public void setReportType(String reportType) {
    this.reportType = reportType;
  }

  public String getReportGroup() {
    return reportGroup;
  }

  public void setReportGroup(String reportGroup) {
    this.reportGroup = reportGroup;
  }

  @Override
  public String toString() {
    return reportName + " - " + description;
  }
}
