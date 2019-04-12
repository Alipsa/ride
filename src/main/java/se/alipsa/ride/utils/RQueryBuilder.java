package se.alipsa.ride.utils;

import se.alipsa.ride.environment.connections.ConnectionInfo;

public class RQueryBuilder {

  public static StringBuilder baseRQueryString(ConnectionInfo con, String command, String sql) {
    StringBuilder str = new StringBuilder();
    String user = con.getUser() == null ? "" : con.getUser().trim();
    String userString = "";
    if (!"".equals(user)) {
      userString = ", user = '" + user + "'";
    }
    String password = con.getPassword() == null ? "" : con.getPassword().trim();
    String passwordString = "";
    if (!"".equals(password)) {
      passwordString = ", password = '" + password + "'";
    }
    str.append("library('DBI')\n library('se.alipsa:R2JDBC')\n")
        .append("RQueryBuilderDrv <- JDBC('").append(con.getDriver()).append("')\n")
        .append("RQueryBuilderCon <- dbConnect(RQueryBuilderDrv, url = '").append(con.getUrl()).append("'")
        .append(userString)
        .append(passwordString)
        .append(")\n")
        .append(command).append("(RQueryBuilderCon, \"").append(sql).append("\")");
    return str;
  }

  public static StringBuilder cleanupRQueryString() {
    StringBuilder str = new StringBuilder();
    str.append("dbDisconnect(RQueryBuilderCon); rm(RQueryBuilderDrv); rm(RQueryBuilderCon);");
    return str;
  }
}
