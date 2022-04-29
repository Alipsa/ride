package se.alipsa.ride.utils;

import static se.alipsa.ride.Constants.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import se.alipsa.ride.Ride;

import java.io.File;

public class JdbcDriverDependencyUtils {

  private static final Logger log = LogManager.getLogger();

  // TODO, look up the latest version dynamically instead of hard coding it
  public static Dependency driverDependency(String driverClass) {
    Dependency dependency = new Dependency();
    switch (driverClass) {
      case DRV_POSTGRES:
        dependency.setGroupId("org.postgresql");
        dependency.setArtifactId("postgresql");
        dependency.setVersion("42.3.3");
        break;
      case DRV_DERBY:
        dependency.setGroupId("org.apache.derby");
        dependency.setArtifactId("derby");
        dependency.setVersion("10.15.2.0");
        break;
      case DRV_FIREBIRD:
        dependency.setGroupId("org.firebirdsql.jdbc");
        dependency.setArtifactId("jaybird");
        dependency.setVersion("4.0.5.java11");
        break;
      case DRV_H2:
        dependency.setGroupId("com.h2database");
        dependency.setArtifactId("h2");
        dependency.setVersion("2.1.212");
        break;
      case DRV_MARIADB:
        dependency.setGroupId("org.mariadb.jdbc");
        dependency.setArtifactId("mariadb-java-client");
        dependency.setVersion("3.0.4");
        break;
      case DRV_MYSQL:
        dependency.setGroupId("mysql");
        dependency.setArtifactId("mysql-connector-java");
        dependency.setVersion("8.0.28");
        break;
      case DRV_ORACLE:
        dependency.setGroupId("com.oracle.database.jdbc");
        dependency.setArtifactId("ojdbc11");
        dependency.setVersion("21.5.0.0");
        break;
      case DRV_SQLLITE:
        dependency.setGroupId("org.xerial");
        dependency.setArtifactId("sqlite-jdbc");
        dependency.setVersion("3.36.0.3");
        break;
      case DRV_SQLSERVER:
        dependency.setGroupId("com.microsoft.sqlserver");
        dependency.setArtifactId("mssql-jdbc");
        dependency.setVersion(checkForDllVersion("10.2.0.jre11"));
        break;
      default:
        // Do nothing
    }
    return dependency;
  }

  /*
    If there is a ddl file for integrated security we should use the corresponding jdbc version
   */
  private static String checkForDllVersion(String defaultVersion) {
    File libDir = new File(Ride.instance().getRideBaseDir(), "lib");
    String version = defaultVersion;
    String latestVersion = "0.0.0";
    String tmpVersion;
    if (libDir.exists() && libDir.isDirectory()) {
      for (String file : libDir.list()) {
        //"mssql-jdbc_auth-9.4.1.x64.dll"
        // there might be several ones so grab the latest one
        if (file.endsWith(".dll") && file.startsWith("mssql-jdbc_auth-") && file.contains(".x64.dll")) {
          tmpVersion = file.substring("mssql-jdbc_auth-".length(), file.indexOf(".x64.dll"))
              + defaultVersion.substring(defaultVersion.lastIndexOf('.'));
          if (SemanticVersion.compare(latestVersion, tmpVersion) < 0) {
            latestVersion = tmpVersion;
          }
        }
      }
      if (!"0.0.0".equals(latestVersion)) {
        log.info("Using the driver corresponding to the latest dll version in lib dir i.e. {}", latestVersion);
        version = latestVersion;
      }
    }
    return version;
  }
}
