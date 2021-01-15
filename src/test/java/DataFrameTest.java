import org.junit.jupiter.api.Test;
import org.renjin.eval.Context;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.*;

import javax.script.ScriptException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static se.alipsa.renjin.client.datautils.RDataTransformer.toRowlist;

public class DataFrameTest {

  @Test
  public void testDataFrame() throws ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();
    engine.eval("df <- data.frame('SN' = 1:3, 'Age' = c(48, 17, 49), 'Name' = c('Per','Ian', 'Louise'))");

    Environment global = engine.getSession().getGlobalEnvironment();
    Context topContext = engine.getSession().getTopLevelContext();

    ListVector df = (ListVector)global.getVariable(topContext, "df");

    List<List<Object>> rowList = toRowlist(df);

    assertThat("First row, SN does not match", rowList.get(0).get(0), is(1));
    assertThat("First row, Name does not match", rowList.get(0).get(1), is(48.0));
    assertThat("First row, Name does not match", rowList.get(0).get(2), is("Per"));

    assertThat("Last row, SN does not match", rowList.get(2).get(0), is(3));
    assertThat("Last row, Name does not match", rowList.get(2).get(1), is(49.0));
    assertThat("Last row, Name does not match", rowList.get(2).get(2), is("Louise"));

  }

}
