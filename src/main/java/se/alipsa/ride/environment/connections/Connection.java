package se.alipsa.ride.environment.connections;

public class Connection {

  private String name;
  private String driver;
  private String url;

  public Connection() {}

  public Connection(String name, String driver, String url) {
    this.name = name;
    this.driver = driver;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String toString() {
    return name;
  }
}
