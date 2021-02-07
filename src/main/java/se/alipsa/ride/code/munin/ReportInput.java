package se.alipsa.ride.code.munin;

public class ReportInput {

  Object params;

  public void addParams(Object params) {
    System.out.println("ReportInput: Got " + params);
    this.params = params;
  }
}
