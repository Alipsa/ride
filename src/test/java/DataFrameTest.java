import org.junit.jupiter.api.Test;
import org.renjin.eval.Context;
import org.renjin.primitives.Types;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.*;

import javax.script.ScriptException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataFrameTest {

  @Test
  public void testDataFrame() throws ScriptException {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    RenjinScriptEngine engine = factory.getScriptEngine();
    engine.eval("df <- data.frame('SN' = 1:3, 'Age' = c(48, 17, 49), 'Name' = c('Per','Ian', 'Louise'))");

    Environment global = engine.getSession().getGlobalEnvironment();
    Context topContext = engine.getSession().getTopLevelContext();

    ListVector df = (ListVector)global.getVariable(topContext, "df");

    List<String> colList = new ArrayList<>();
    if (df.hasAttributes()) {
      AttributeMap attributes = df.getAttributes();
      Map<Symbol, SEXP> attrMap = attributes.toMap();
      Symbol s = attrMap.keySet().stream().filter(p -> "names".equals(p.getPrintName())).findAny().orElse(null);
      Vector colNames = (Vector)attrMap.get(s);
      for(int i = 0; i < colNames.length(); i++) {
        colList.add(colNames.getElementAsString(i));
      }
    }

    List<Vector> table = new ArrayList<>();
    for(SEXP col : df) {
      Vector column = (Vector)col;
      table.add(column);
    }
    List<List<Object>> rowList = transpose(table);

    assertThat("First row, SN does not match", rowList.get(0).get(0), is(1));
    assertThat("First row, Name does not match", rowList.get(0).get(1), is(48.0));
    assertThat("First row, Name does not match", rowList.get(0).get(2), is("Per"));

    assertThat("Last row, SN does not match", rowList.get(2).get(0), is(3));
    assertThat("Last row, Name does not match", rowList.get(2).get(1), is(49.0));
    assertThat("Last row, Name does not match", rowList.get(2).get(2), is("Louise"));

  }

  private List<List<Object>> transpose(List<Vector> table) {
    List<List<Object>> ret = new ArrayList<>();
    final int N = table.get(0).length();
    for (int i = 0; i < N; i++) {
      List<Object> row = new ArrayList<>();
      for (Vector col : table) {
        if (Types.isFactor(col)) {
          AttributeMap attributes = col.getAttributes();
          Map<Symbol, SEXP> attrMap = attributes.toMap();
          Symbol s = attrMap.keySet().stream().filter(p -> "levels".equals(p.getPrintName())).findAny().orElse(null);
          Vector vec = (Vector)attrMap.get(s);
          row.add(vec.getElementAsObject(col.getElementAsInt(i)-1));
        } else {
          row.add(col.getElementAsObject(i));
        }
      }
      ret.add(row);
    }
    return ret;
  }
}
