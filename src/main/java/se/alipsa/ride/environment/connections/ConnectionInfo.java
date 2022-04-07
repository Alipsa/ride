package se.alipsa.ride.environment.connections;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.Alerts;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class ConnectionInfo implements Comparable<ConnectionInfo> {

  private static final Logger log = LogManager.getLogger(ConnectionInfo.class);

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

  @Override
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
  public int compareTo(ConnectionInfo obj) {
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

  @SuppressWarnings("unchecked")
  public Optional<Connection> connect() throws SQLException {
    /*
    String user = getUser();
    String password = getPassword();
    String theUrl = getUrl();

    if ( urlContainsLogin() ) {
      log.info("Skipping specified user/password since it is part of the url");
      return DriverManager.getConnection(theUrl);
    }
    return DriverManager.getConnection(theUrl, user, password);
    */

    // DriverManager.getConnection uses system classloader no matter what so we need to dance around this
    // to allow dynamic classloading from a pom etc. by getting the connection directly from the driver
    Driver driver = null;
    try {
      ClassLoader cl = Ride.instance().getConsoleComponent().getSession().getClassLoader();
      Class<Driver> clazz = (Class<Driver>) cl.loadClass(getDriver());
      log.debug("Loaded driver from session classloader, instating the driver {}", getDriver());
      try {
        driver = clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        log.error("Failed to instantiate the driver: {}", getDriver(), e);
      }
    } catch (ClassCastException | ClassNotFoundException e) {
      log.info("Failed to load the class for {}, attempting to use Class.forName instead", getDriver());
      try {
        Class<?> clazz = Class.forName(getDriver());
        driver = ((Driver)clazz.getDeclaredConstructor().newInstance());
        log.debug("Loaded driver {} with Class.forName successfully", getDriver());
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException classNotFoundException) {
        log.info("Failed to load and instantiate the driver class using Class.forName(\"{}\")", getDriver());
        Platform.runLater(() ->
            // TODO add option to download driver here and autotry again after classloader update
            Alerts.showAlert("Failed to load driver",
                "You need to add the jar with " + getDriver() + " to the classpath (pom.xml or ride lib dir)",
                Alert.AlertType.ERROR)
        );
        return Optional.empty();
      }
    }
    Properties props = new Properties();
    if (getUser() != null) {
      props.put("user", getUser());
      if ( getPassword() != null) {
        props.put("password",  getPassword());
      }
    }
    Connection con;
    if (driver == null) {
      con = DriverManager.getConnection(getUrl(), props);
    } else {
      con = driver.connect(getUrl(), props);
    }
    if (con == null) {
      Alerts.warn("Failed to connect to database", "Failed to connect to database: " + getUrl() + ", probably something in the url that the Driver could not understand since connect() returned null.");
      return Optional.empty();
    }
    return Optional.of(con);
  }

  public boolean urlContainsLogin() {
    String safeLcUrl = url.getValueSafe().toLowerCase();
    return ( safeLcUrl.contains("user") && safeLcUrl.contains("pass") ) || safeLcUrl.contains("@");
  }

  public String asJson() {
    return "{" +
       "\"name\"=\"" + name.getValue() +
       "\", \"driver\"=\"" + driver.getValue() +
       "\", \"url\"=\"" + url.getValue() +
       "\", \"user\"=" + user.getValue() +
       "\", \"password\"=\"" + password.getValue() +
       "\"}";
  }
}
