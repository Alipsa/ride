package se.alipsa.ride.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.alipsa.ride.environment.connections.ConnectionInfo;

public class RQueryBuilder {

  private static final Logger log = LogManager.getLogger(RQueryBuilder.class);
  public static final String DRIVER_VAR_NAME = "RQueryBuilderDrv";
  public static final String CONNECTION_VAR_NAME = "RQueryBuilderCon";


  public static StringBuilder baseRQueryString(ConnectionInfo con, String command, String sql, boolean... addNAwhenBlank) {
    StringBuilder str = new StringBuilder();

    boolean useNaWhenBlank = addNAwhenBlank.length <= 0 || addNAwhenBlank[0];
    String url = con.getUrl();
    String user = con.getUser() == null ? "" : con.getUser().trim();
    String userString = "";
    if (!StringUtils.isBlank(user) && !con.urlContainsLogin()) {
      userString = ", user = '" + user + "'";
    } else if(useNaWhenBlank) {
      userString = "";
      if (!con.urlContainsLogin()) {
        url = url + ";user=NA";
      }
    }
    String password = con.getPassword() == null ? "" : con.getPassword().trim();
    String passwordString;
    if (!"".equals(password) && !con.urlContainsLogin()) {
      passwordString = ", password = '" + password + "'";
    } else {
      passwordString = "";
      if (!con.urlContainsLogin() && useNaWhenBlank) {
        url = url + ";password=NA";
      }
    }
    str.append("library('DBI')\nlibrary('se.alipsa:R2JDBC')\n")
        .append(DRIVER_VAR_NAME).append(" <- JDBC('").append(con.getDriver()).append("')\n")
        .append(CONNECTION_VAR_NAME).append(" <- dbConnect(RQueryBuilderDrv, url = '").append(url).append("'")
        .append(userString)
        .append(passwordString)
        .append(")\n")
        .append(command).append("(").append(CONNECTION_VAR_NAME).append(", \"").append(sql).append("\")");
    //log.info(str.toString());
    return str;
  }

  public static StringBuilder cleanupRQueryString() {
    StringBuilder str = new StringBuilder();
    str.append("dbDisconnect(").append(CONNECTION_VAR_NAME).append("); rm(").append(DRIVER_VAR_NAME)
        .append("); rm(").append(CONNECTION_VAR_NAME).append(");");
    return str;
  }
}
