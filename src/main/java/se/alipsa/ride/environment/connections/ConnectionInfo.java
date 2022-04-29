package se.alipsa.ride.environment.connections;

import static se.alipsa.ride.menu.GlobalOptions.USE_MAVEN_CLASSLOADER;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import se.alipsa.maven.MavenUtils;
import se.alipsa.ride.Ride;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;
import se.alipsa.ride.utils.JdbcDriverDependencyUtils;
import se.alipsa.rideutils.Dialogs;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
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
  public Connection connect() throws SQLException {
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
    var gui = Ride.instance();
    // DriverManager.getConnection uses system classloader no matter what so we need to dance around this
    // to allow dynamic classloading from a pom etc. by getting the connection directly from the driver
    Driver driver = null;
    ClassLoader cl = gui.getConsoleComponent().getClassLoader();

    try {
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
        if (Ride.instance().getPrefs().getBoolean(USE_MAVEN_CLASSLOADER, false) && Ride.instance().getInoutComponent().hasPomFile()) {
          var addToPom = Alerts.confirm("Failed to load driver", "The driver " + getDriver() + " is missing in the pom.xml",
              "Add dependency for " + getDriver() + " to the pom.xlm file?");
          if (addToPom) {
            gui.setWaitCursor();
            File pomFile = new File(gui.getInoutComponent().getRootDir(), "pom.xml");
            try {
              log.info("Adding dependency for {}", getDriver());
              addDependency(pomFile, JdbcDriverDependencyUtils.driverDependency(getDriver()));
              log.trace("Reloading tab content");
              gui.getCodeComponent().reloadTabContent(pomFile);
              log.trace("Reinitialize classloader and renjin...");
              gui.getConsoleComponent().initRenjin(gui.getClass().getClassLoader(), true);
              log.trace("Try to connect again");
              return connect();
            } catch (IOException | JDOMException ex) {
              ExceptionAlert.showAlert("Failed to add dependency to pom.xml", ex);
            }
          }
        } else {
          Platform.runLater(() ->
              Alerts.showAlert("Failed to load driver",
                  "You need to add the jar with " + getDriver() + " to the classpath (pom.xml or ride lib dir)",
                  Alert.AlertType.ERROR)
          );
        }
        return null;
      }
    }
    Properties props = new Properties();
    if (getUser() != null) {
      props.put("user", getUser());
      if ( getPassword() != null) {
        props.put("password",  getPassword());
      }
    }
    gui.setNormalCursor();
    if (driver == null) {
      return DriverManager.getConnection(getUrl(), props);
    } else {
      return driver.connect(getUrl(), props);
    }
  }

  private void addDependency(File pomFile, Dependency driverDependency) throws IOException, JDOMException {
    try {
      org.jdom2.Document doc = new SAXBuilder().build(pomFile);
      Element root = doc.getRootElement();
      Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
      Element dependencies = root.getChild("dependencies", ns);
      if (dependencies == null) {
        dependencies = new Element("dependencies", ns);
      }
      var dep = new Element("dependency", ns);
      dep.addContent(new Element("groupId", ns).setText(driverDependency.getGroupId()));
      dep.addContent(new Element("artifactId", ns).setText(driverDependency.getArtifactId()));
      dep.addContent(new Element("version", ns).setText(driverDependency.getVersion()));
      dependencies.addContent(dep);
      XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
      xmlOutput.output(doc, new FileOutputStream(pomFile));
    } catch (RuntimeException e) {
      log.error(e);
      throw new JDOMException(e.toString());
    }
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
