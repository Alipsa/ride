package se.alipsa.ride.model;

import static se.alipsa.ride.utils.RDataTransformer.*;

import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;

import java.util.ArrayList;
import java.util.List;

public class Table {

  List<String> colList;
  List<List<Object>> rowList;

  public Table(ListVector df) {
    colList = toColumnList(df);
    rowList = toRowlist(df);
  }

  public Table(Matrix mat) {
    String type = mat.getVector().getTypeName();
    colList = new ArrayList<>();
    for (int i = 0; i < mat.getNumCols(); i++) {
      String colName = mat.getColName(i) == null ? i + "" : mat.getColName(i);
      colList.add(colName);
    }

    rowList = new ArrayList<>();

    List<Object> row;
    for (int i = 0; i < mat.getNumRows(); i++) {
      row = new ArrayList<>();
      for (int j = 0; j < mat.getNumCols(); j++) {
        if ("integer".equals(type)) {
          row.add(mat.getElementAsInt(i, j));
        } else {
          row.add(mat.getElementAsDouble(i, j));
        }
      }
      rowList.add(row);
    }
  }

  public Table(Vector vec) {
    colList = new ArrayList<>();
    colList.add(vec.getTypeName());

    List<Vector> values = new ArrayList<>();
    values.add(vec);

    rowList = transpose(values);
  }

  public List<String> getColList() {
    return colList;
  }

  public List<List<Object>> getRowList() {
    return rowList;
  }
}
