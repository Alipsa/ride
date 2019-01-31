package se.alipsa.ride.utils;

import se.alipsa.ride.environment.connections.ConnectionInfo;

public class RQueryBuilder {

  public static StringBuilder baseRQueryString(ConnectionInfo con, String command, String sql) {
    StringBuilder str = new StringBuilder();
    str.append("library('DBI')\n library('org.renjin.cran:RJDBC')\n")
        .append("RQueryBuilderDrv <- JDBC('").append(con.getDriver()).append("')\n")
        .append("RQueryBuilderCon <- dbConnect(RQueryBuilderDrv, url='").append(con.getUrl()).append("')\n")
        .append(command).append("(RQueryBuilderCon, \"").append(sql).append("\")");
    return str;
  }

  public static StringBuilder cleanupRQueryString() {
    StringBuilder str = new StringBuilder();
    str.append("dbDisconnect(RQueryBuilderCon); rm(RQueryBuilderDrv); rm(RQueryBuilderCon);");
    return str;
  }
}
