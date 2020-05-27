package view;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Map;
import javax.script.ScriptException;

public class ListVectorContentConversionTest {

  // Not yet a test and not sure if we should do something about Date and POSIXlt for View
  @Ignore
  @Test
  public void testDateConversion() throws ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();

    // See http://docs.renjin.org/en/latest/library/capture.html for how to capture results from the script
    DoubleArrayVector result = (DoubleArrayVector)engine.eval("as.Date('2020-05-27')");
    //System.out.println("Attributes = " + result.getAttributes().));
    System.out.println("Type name = " + result.getTypeName());
    System.out.println("VectorType = " + result.getVectorType());
    System.out.println("Has attributes = " + result.hasAttributes());
    System.out.println("Implicit class = " + result.getImplicitClass());
    Map<Symbol, SEXP> attributes = result.getAttributes().toMap();
    System.out.println("Attributes:");
    attributes.forEach((k,v) -> System.out.println("Key: " + k.getPrintName() + ", Type = " + k.getTypeName() + ": " + k.toString()));
    Symbol className = Symbol.get("class");
    System.out.println("Class attribute = " + result.getAttribute(className));

    SEXP res = (SEXP)engine.eval("'2020-05-27'");
    System.out.println("Class attribute for string: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("2*14");
    System.out.println("Class attribute for int: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXlt('2020-05-27')");
    System.out.println("Class attribute for POSIXlt: " + res  + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXlt('2020-05-27 14:02:44')");
    System.out.println("Class attribute for POSIXlt: " + res  + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("as.POSIXct('2020-05-27 14:02:44')");
    System.out.println("Class attribute for POSIXct: " + res + " = " + res.getAttribute(className));

    res = (SEXP)engine.eval("format(as.POSIXct('2020-05-27 14:02:44'))");
    System.out.println("Class attribute for format(POSIXct): " + res + " = " + res.getAttribute(className));
  }
}
