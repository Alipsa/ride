package se.alipsa.ride.console;

public class TestResult {

  private String testName;

  private OutCome result;
  private Throwable error;
  private String issue;

  public TestResult() {
  }

  public TestResult(String testName) {
    this.testName = testName;
  }

  public OutCome getResult() {
    return result;
  }

  public void setResult(OutCome result) {
    this.result = result;
  }

  public Throwable getError() {
    return error;
  }

  public void setError(Throwable error) {
    this.error = error;
  }

  public String getTesName() {
    return testName;
  }

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public enum OutCome {SUCCESS, FAILURE, ERROR}
}
