package se.alipsa.ride.model;

import static se.alipsa.ride.utils.RDataTransformer.*;

import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated use version in renjin-client-data-utils
 */
@Deprecated
public class Table {

  List<String> colList;
  List<List<Object>> rowList;

  public Table(ListVector df) {
    colList = toColumnList(df);
    rowList = toRowlist(df);
  }

  public Table(List<String> columnList, List<List<Object>> rowList) {
    colList = columnList;
    this.rowList = rowList;
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

  public Table(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    colList = new ArrayList<>();
    int ncols = rsmd.getColumnCount();
    for (int i = 1; i <= ncols; i++) {
      colList.add(rsmd.getColumnName(i));
    }
    rowList = new ArrayList<>();
    while (rs.next()) {
      List<Object> row = new ArrayList<>();
      for (int i = 1; i <= ncols; i++) {
        row.add(rs.getObject(i));
      }
      rowList.add(row);
    }
  }

  public List<String> getColList() {
    return colList;
  }

  public List<List<Object>> getRowList() {
    return rowList;
  }
}
