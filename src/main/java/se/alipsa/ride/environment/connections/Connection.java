package se.alipsa.ride.environment.connections;

import javafx.beans.property.SimpleStringProperty;

public class Connection implements Comparable {

  private final SimpleStringProperty name;
  private final SimpleStringProperty driver;
  private final SimpleStringProperty url;

  public Connection() {
    this.name = new SimpleStringProperty();
    this.driver = new SimpleStringProperty();
    this.url = new SimpleStringProperty();
  }

  public Connection(String name, String driver, String url) {
    this.name = new SimpleStringProperty(name);
    this.driver = new SimpleStringProperty(driver);
    this.url = new SimpleStringProperty(url);
  }

  public String getName() {
    return name.get();
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public String getDriver() {
    return driver.get();
  }

  public void setDriver(String driver) {
    this.driver.set(driver);
  }

  public String getUrl() {
    return url.get();
  }

  public void setUrl(String url) {
    this.url.set(url);
  }

  public String toString() {
    return name.get();
  }

  @Override
  public int compareTo(Object obj) {
    return this.toString().compareTo(obj.toString());
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Connection) {
      return toString().equals(obj.toString());
    } else {
      return false;
    }
  }
}
