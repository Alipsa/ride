package se.alipsa.ride.environment.connections;

import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionInfo implements Comparable {

  private Logger log = LoggerFactory.getLogger(ConnectionInfo.class);

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
    return name.getValue();
  }

  public void setName(String name) {
    this.name.setValue(name);
  }

  public String getDriver() {
    return driver.getValue();
  }

  public void setDriver(String driver) {
    this.driver.setValue(driver);
  }

  public String getUrl() {
    return url.getValue();
  }

  public void setUrl(String url) {
    this.url.setValue(url);
  }

  public String toString() {
    return name.getValue();
  }

  public String getUser() {
    return user.getValue();
  }

  public void setUser(String user) {
    this.user.setValue(user);
  }

  public String getPassword() {
    return password.getValue();
  }

  public void setPassword(String password) {
    this.password.setValue(password);
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
    String user = getUser();
    String password = getPassword();
    String theUrl = getUrl();

    if ( urlContainsLogin() ) {
      log.info("Skipping specified user/password since it is part of the url");
      return DriverManager.getConnection(theUrl);
    }
    return DriverManager.getConnection(theUrl, user, password);
  }

  public boolean urlContainsLogin() {
    String safeLcUrl = url.getValueSafe().toLowerCase();
    return ( safeLcUrl.contains("user") && safeLcUrl.contains("pass") ) || safeLcUrl.contains("@");
  }
}
