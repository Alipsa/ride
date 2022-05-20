package se.alipsa.ride.code.rtab;

import org.renjin.parser.*;
import se.alipsa.ride.utils.Alerts;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class SyntaxChecker {

  public void check(String rCode) {
    ParseOptions options = ParseOptions.defaults();
    ParseState state = new ParseState();
    Reader reader = new StringReader(rCode + "\n");
    RLexer lexer = new RLexer(options, state, reader);
    RParser parser = new RParser(options, state, lexer);
    int row = 0;
    try {
      while(!parser.isEof()) {
        row++;
        boolean isParseable = parser.parse();
        if (!isParseable) {
          Alerts.warn("R code is not parsable", "Syntax error on line " + row);
          return;
        }
        if (RParser.StatusResult.ERROR.equals(parser.getResultStatus())) {
          Alerts.warn("Syntax error detected", "Error on line " + row + ": " + parser.getResult());
          return;
        }
      }
    } catch (IOException e) {
      ExceptionAlert.showAlert("Failed to parse R code", e);
    } catch (ParseException p) {
      ExceptionAlert.showAlert("Parse Exception parsing R code: " + p.getMessage(), p);
    }
    Alerts.info("SyntaxChecker parsing result", "Parsing successful");
  }
}
