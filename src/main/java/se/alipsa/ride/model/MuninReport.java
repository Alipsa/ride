package se.alipsa.ride.model;

import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MuninReport {

  public static final String FILE_EXTENSION = ".mr";

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MuninReport that = (MuninReport) o;

    if (!Objects.equals(reportName, that.reportName)) return false;
    if (!Objects.equals(description, that.description)) return false;
    if (!Objects.equals(definition, that.definition)) return false;
    if (!Objects.equals(inputContent, that.inputContent)) return false;
    if (!Objects.equals(reportType, that.reportType)) return false;
    return Objects.equals(reportGroup, that.reportGroup);
  }

  @Override
  public int hashCode() {
    int result = reportName != null ? reportName.hashCode() : 0;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (definition != null ? definition.hashCode() : 0);
    result = 31 * result + (inputContent != null ? inputContent.hashCode() : 0);
    result = 31 * result + (reportType != null ? reportType.hashCode() : 0);
    result = 31 * result + (reportGroup != null ? reportGroup.hashCode() : 0);
    return result;
  }
}
