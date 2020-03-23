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
      Vector colNames = (Vector) attrMap.get(s);
      for (int i = 0; i < colNames.length(); i++) {
        colList.add(colNames.getElementAsString(i));
      }
    }
    return colList;
  }

  public static List<List<Object>> toRowlist(ListVector df) {
    List<Vector> table = new ArrayList<>();
    for (SEXP col : df) {
      Vector column = (Vector) col;
      table.add(column);
    }
    List<List<Object>> rowList = transpose(table);
    return rowList;
  }

  // TODO: this needs some work, col.getElementAsObject(i) returns Integer.MIN_VALUE instead of null for numerics
  public static List<List<Object>> transpose(List<Vector> table) {
    List<List<Object>> ret = new ArrayList<>();
    final int N = table.get(0).length();
    //System.out.println("Transposing a table with " + N + " columns into " + N + " rows");
    for (int i = 0; i < N; i++) {
      List<Object> row = new ArrayList<>();
      for (Vector col : table) {
        if (Types.isFactor(col)) {
          int index = col.getElementAsInt(i) - 1;
          if (index < 0 || index > col.length() -1) {
            /*
            System.err.println("Failed to extract value from factor element, index " + index + " is bigger than vector size " + vec.length());
            System.err.println("Factor vector is " + vec.toString());
            System.err.println("Factor vector names are " + vec.getNames());
            System.err.println("col.getElementAsObject(i) = " + col.getElementAsObject(i));
            System.err.println("col " + (row.size() + 1) + " row " + (i + 1) + " interpreted as null");
             */
            row.add(null);
          } else {
            AttributeMap attributes = col.getAttributes();
            Map<Symbol, SEXP> attrMap = attributes.toMap();
            Symbol s = attrMap.keySet().stream().filter(p -> "levels".equals(p.getPrintName())).findAny().orElse(null);
            Vector vec = (Vector) attrMap.get(s);
            row.add(vec.getElementAsObject(index));
          }
        } else {
          row.add(col.getElementAsObject(i));
        }
      }
      ret.add(row);
    }
    return ret;
  }
}
