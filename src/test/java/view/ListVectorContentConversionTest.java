package view;

import org.junit.jupiter.api.Test;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import javax.script.ScriptException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListVectorContentConversionTest {

  // Not sure if we should do something about Date and POSIXlt for View
  // This at least shows that it is possible to detect dates
  @Test
  public void testDateConversion() throws ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();

    Symbol className = Symbol.get("class");

    SEXP res = (SEXP)engine.eval("'2020-05-27'");
    assertFalse(isDate(res), "Class attribute for string: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.Date('2020-05-27')");
    assertTrue(isDate(res), "Class attribute for Date: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("2*14");
    assertFalse(isDate(res), "Class attribute for int: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXlt('2020-05-27')");
    assertTrue(isDate(res),"Class attribute for POSIXlt: " + res  + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXlt('2020-05-27 14:02:44')");
    assertTrue(isDate(res),"Class attribute for POSIXlt: " + res  + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXct('2020-05-27 14:02:44')");
    assertTrue(isDate(res),"Class attribute for POSIXct: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("format(as.POSIXct('2020-05-27 14:02:44'))");
    assertFalse(isDate(res),"Class attribute for format(POSIXct): " + res + " = " + res.getAttribute(className));
  }

  private boolean isDate(SEXP value) {
    Symbol className = Symbol.get("class");
    SEXP attribute = value.getAttribute(className);
    if (attribute == null) return false;
    String type = attribute.toString();
    //System.out.println("Type is " + type);
    return type.contains("Date") || type.contains("POSIXt");
  }

}
