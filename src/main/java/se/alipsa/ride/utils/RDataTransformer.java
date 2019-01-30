package se.alipsa.ride.utils;

import org.renjin.primitives.Types;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RDataTransformer {

  public static List<String> toColumnList(ListVector df) {
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
    return colList;
  }
  public static List<List<Object>> toRowlist(ListVector df) {
    List<Vector> table = new ArrayList<>();
    for(SEXP col : df) {
      Vector column = (Vector)col;
      table.add(column);
    }
    List<List<Object>> rowList = transpose(table);
    return rowList;
  }

  public static List<List<Object>> transpose(List<Vector> table) {
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
