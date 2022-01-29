package se.alipsa.ride.utils;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;

public final class JdbcUrlParser {

  /**
   *
   * @param jdbcDriver the full classname of the jdbc driver e.g. org.postgresql.Driver
   * @param jdbcUrl the jdbc url to validate e.g. jdbc:postgresql://localhost:5432/test
   * @return true if valid, false if not valid
   */
  public static void validate(@NotNull String jdbcDriver, @NotNull String jdbcUrl) throws MalformedURLException {
    switch(jdbcDriver) {
      case "org.postgresql.Driver": {
        validateStart(jdbcUrl, "Postgresql", "jdbc:postgresql://");
        break;
      }
      case 	"com.microsoft.sqlserver.jdbc.SQLServerDriver": {
        validateStart(jdbcUrl, "SQL Server", "jdbc:sqlserver://");
        break;
      }
      case "oracle.jdbc.OracleDriver": {
        validateStart(jdbcUrl, "Oracle", "jdbc:oracle:thin:", "jdbc:oracle:oci:");
        break;
      }
      case "com.mysql.jdbc.Driver": {
        validateStart(jdbcUrl, "MySQL", "jdbc:mysql://");
        break;
      }
      case "org.mariadb.jdbc.Driver": {
        validateStart(jdbcUrl, "MariaDB", "jdbc:mariadb://");
        break;
      }
      case "com.ibm.db2.jcc.DB2Driver": {
        validateStart(jdbcUrl, "Db2 Express-C", "jdbc:db2://");
        break;
      }
      case "com.sap.db.jdbc.Driver": {
        validateStart(jdbcUrl, "SAP HANA", "jdbc:sap://");
        break;
      }
      case "com.informix.jdbc.IfxDriver": {
        validateStart(jdbcUrl, "Informix", "jdbc:informix-sqli://");
        break;
      }
      case "org.hsqldb.jdbc.JDBCDriver": {
        validateStart(jdbcUrl, "HSQLDB", "jdbc:hsqldb:mem:", "jdbc:hsqldb:file:", "jdbc:hsqldb:res:",
            "jdbc:hsqldb:hsql:", "jdbc:hsqldb:hsqls:", "jdbc:hsqldb:http:", "jdbc:hsqldb:https:");
        break;
      }
      case "org.h2.Driver": {
        // Too many to go into details, just do the first part
        validateStart(jdbcUrl, "H2", "jdbc:h2:");
        break;
      }
      case "org.apache.derby.jdbc.EmbeddedDriver": {
        validateStart(jdbcUrl, "Derby Embedded", "jdbc:derby:");
        break;
      }
      case "org.apache.derby.jdbc.ClientDriver": {
        validateStart(jdbcUrl, "Derby Client", "jdbc:derby://");
        break;
      }
      case "org.sqlite.JDBC": {
        validateStart(jdbcUrl, "SQLite", "jdbc:sqlite:");
        break;
      }
      case "org.firebird.jdbc.FBDriver": {
        validateStart(jdbcUrl, "Firebird", "jdbc:firebirdsql://");
        break;
      }
    }
  }

  private static void validateStart(String jdbcUrl, String dbProvider, String... startAlt) throws MalformedURLException {
    boolean isValid = false;
    for (String start : startAlt) {
      if (jdbcUrl.startsWith(start)) {
        isValid = true;
        break;
      }
    }
    if (!isValid) {
      String invalidStart = String.join(" or ", startAlt);
      throw new MalformedURLException("A " + dbProvider + " jdbc url must start with " + invalidStart);
    }
  }
}
