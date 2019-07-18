package se.alipsa.ride.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {

  private static Logger log = LoggerFactory.getLogger(SqlParser.class);

  public static String[] split(String sql, StringBuilder warnings) {
    try {
      Statements statements = CCJSqlParserUtil.parseStatements(sql);
      List<Statement> statementList = statements.getStatements();
      List<String> list = new ArrayList<>(statementList.size());
      for (Statement stmt : statementList) {
        list.add(stmt.toString());
      }
      return list.toArray(new String[0]);
    } catch (JSQLParserException e) {
      log.warn("Failed to parse sql", e);
      int numlines = org.apache.commons.lang3.StringUtils.countMatches(sql,"\n") + 1;
      warnings.append("Failed to parse statement(s), will try the whole string (" + numlines + " lines, " + sql.length() + " chars)");
      return new String[] {sql};
    }
  }
}
