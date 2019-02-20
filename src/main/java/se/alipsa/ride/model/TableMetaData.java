package se.alipsa.ride.model;

import java.util.List;

public class TableMetaData {

  public static final String COLUMN_META_START = " [ ";
  public static final String COLUMN_META_END = " ]";
  private String tableName;
  private String tableType;
  private String columnName;
  private Integer ordinalPosition;
  private String isNullable;
  private String dataType;
  private Integer characterMaximumLength;
  private Integer numericPrecision;
  private Integer numericPrecisionRadix;
  private Integer numericScale;
  private String collationName;


  public TableMetaData(List<Object> row) {
    setTableName((String) row.get(0));
    setTableType((String) row.get(1));
    setColumnName((String) row.get(2));
    setOrdinalPosition((Integer) row.get(3));
    setIsNullable((String) row.get(4));
    setDataType(row.get(5).toString()); // On h2 this is an INT, on SQL Server it is VARCHAR, toString works in both cases
    setCharacterMaximumLength((Integer) row.get(6));
    setNumericPrecision((Integer) row.get(7));
    setNumericPrecisionRadix((Integer) row.get(8));
    setNumericScale((Integer) row.get(9));
    setCollationName((String) row.get(10));
  }

  public TableMetaData(Object tableName, Object tableType, Object columnName, Object ordinalPosition,
                       Object isNullable, Object dataType, Object characterMaximumLength, Object numericPrecision,
                       Object numericPrecisionRadix, Object numericScale, Object collationName) {

    setTableName((String) tableName);
    setTableType((String) tableType);
    setColumnName((String) columnName);
    setOrdinalPosition((Integer) ordinalPosition);
    setIsNullable((String) isNullable);
    setDataType((String) dataType);
    setCharacterMaximumLength((Integer) characterMaximumLength);
    setNumericPrecision((Integer) numericPrecision);
    setNumericPrecisionRadix((Integer) numericPrecisionRadix);
    setNumericScale((Integer) numericScale);
    setCollationName((String) collationName);
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public Integer getOrdinalPosition() {
    return ordinalPosition;
  }

  public void setOrdinalPosition(Integer ordinalPosition) {
    this.ordinalPosition = ordinalPosition;
  }

  public String getIsNullable() {
    return isNullable;
  }

  public void setIsNullable(String isNullable) {
    this.isNullable = isNullable;
  }

  public String getDataType() {
    return dataType;
  }

  public void setDataType(String dataType) {
    String DATATYPE = dataType == null ? "" : dataType.trim().toUpperCase();
    switch (DATATYPE) {
      case "-6":
        this.dataType = "TINYINT";
        break;
      case "-5":
        this.dataType = "BIGINT";
        break;
      case "-3":
        this.dataType = "BINARY";
        break;
      case "-2":
        this.dataType = "UUID";
        break;
      case "1":
        this.dataType = "CHAR";
        break;
      case "3":
        this.dataType = "DECIMAL";
        break;
      case "4":
        this.dataType = "INT";
        break;
      case "5":
        this.dataType = "SMALLINT";
        break;
      case "7":
        this.dataType = "REAL";
        break;
      case "8":
        this.dataType = "DOUBLE";
        break;
      case "12":
        this.dataType = "VARCHAR";
        break;
      case "16":
        this.dataType = "BOOLEAN";
        break;
      case "91":
        this.dataType = "DATE";
        break;
      case "92":
        this.dataType = "TIME";
        break;
      case "93":
        this.dataType = "TIMESTAMP";
        break;
      case "1111":
        this.dataType = "OTHER";
        break;
      case "2003":
        this.dataType = "ARRAY";
        break;
      case "2004":
        this.dataType = "BLOB";
        break;
      case "2005":
        this.dataType = "CLOB";
        break;
      case "2014":
        this.dataType = "TIMESTAMP WITH TZ";
        break;
      default:
        this.dataType = dataType;
    }
  }

  public Integer getCharacterMaximumLength() {
    return characterMaximumLength;
  }

  public void setCharacterMaximumLength(Integer characterMaximumLength) {
    this.characterMaximumLength = characterMaximumLength;
  }

  public Integer getNumericPrecision() {
    return numericPrecision;
  }

  public void setNumericPrecision(Integer numericPrecision) {
    this.numericPrecision = numericPrecision;
  }

  public Integer getNumericPrecisionRadix() {
    return numericPrecisionRadix;
  }

  public void setNumericPrecisionRadix(Integer numericPrecisionRadix) {
    this.numericPrecisionRadix = numericPrecisionRadix;
  }

  public Integer getNumericScale() {
    return numericScale;
  }

  public void setNumericScale(Integer numericScale) {
    this.numericScale = numericScale;
  }

  public String getCollationName() {
    return collationName;
  }

  public void setCollationName(String collationName) {
    this.collationName = collationName;
  }

  public String asColumnString() {
    String precision = "";
    String DATATYPE = dataType == null ? "" : dataType.trim().toUpperCase();
    if (DATATYPE.contains("VARCHAR") || DATATYPE.equals("CHARACTER VARYING")) {
      precision = "(" + characterMaximumLength + ")";
    } else if (DATATYPE.equals("DECIMAL") || DATATYPE.equals("NUMBER") || DATATYPE.equals("NUMERIC")) {
      precision = "(" + numericPrecision + "," + numericScale + ")";
    }
    String nullable;
    if (isNullable.equalsIgnoreCase("YES")
        || isNullable.equalsIgnoreCase("TRUE")
        || isNullable.equalsIgnoreCase("1")) {
      nullable = "null";
    } else {
      nullable = "not null";
    }
    return columnName + COLUMN_META_START + dataType + precision + ", " + nullable + COLUMN_META_END;
  }
}
