package utils;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.alipsa.ride.utils.JdbcUrlParser.validate;

public class JdbcUrlParserTest {

  @Test
  public void testPostgres() throws MalformedURLException {
    String driver = "org.postgresql.Driver";
    validate(driver, "jdbc:postgresql://localhost:5432/mydatabase");
    assertThrows(MalformedURLException.class, () -> validate(driver, "jdbc:postgres://localhost:5432/mydatabase"));
  }

  @Test
  public void testOracle() throws MalformedURLException {
    String driver = "oracle.jdbc.OracleDriver";
    validate(driver, "jdbc:oracle:thin:@localhost:1521/orclpdb1");
    assertThrows(MalformedURLException.class, () -> validate(driver, "jdbc:oracle://localhost:5432/mydatabase"));
  }
}
