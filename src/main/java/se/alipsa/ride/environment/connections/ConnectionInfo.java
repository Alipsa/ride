package se.alipsa.ride.environment.connections;

import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionInfo implements Comparable {

  private final SimpleStringProperty name;
  private final SimpleStringProperty driver;
  private final SimpleStringProperty url;
  private final SimpleStringProperty user;
  private final SimpleStringProperty password;

  public ConnectionInfo() {
    this.name = new SimpleStringProperty();
    this.driver = new SimpleStringProperty();
    this.url = new SimpleStringProperty();
    this.user = new SimpleStringProperty();
    this.password = new SimpleStringProperty();
  }

  public ConnectionInfo(String name, String driver, String url, String user, String password) {
    this.name = new SimpleStringProperty(name);
    this.driver = new SimpleStringProperty(driver);
    this.url = new SimpleStringProperty(url);
    this.user = new SimpleStringProperty(user);
    this.password = new SimpleStringProperty(password);
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

  public String getUser() {
    return user.get();
  }

  public void setUser(String user) {
    this.user.set(user);
  }

  public String getPassword() {
    return password.get();
  }

  public void setPassword(String password) {
    this.password.set(password);
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
    if (obj instanceof ConnectionInfo) {
      return toString().equals(obj.toString());
    } else {
      return false;
    }
  }

  public Connection connect() throws SQLException {
    return DriverManager.getConnection(getUrl(), getUser(), getPassword());
  }
}
