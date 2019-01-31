package se.alipsa.ride.model;

import java.util.List;

public class TableMetaData {

  private String tableName;
  private String tableType;
  private String columnName;
  private Integer ordinalPosition;
  private String isNullable;
  private String dataType;
  private Integer characterMaximumLength;
  private Integer numericPrecisionRadix;
  private Integer numericScale;
  private String collationName;


  public TableMetaData(List<Object> row) {
    this.tableName = (String)row.get(0);
    this.tableType = (String)row.get(1);
    this.columnName =  (String)row.get(2);
    this.ordinalPosition = (Integer)row.get(3);
    this.isNullable = (String)row.get(4);
    this.dataType =  (String)row.get(5);
    this.characterMaximumLength = (Integer)row.get(6);
    this.numericPrecisionRadix = (Integer)row.get(7);
    this.numericScale = (Integer)row.get(8);
    this.collationName =  (String)row.get(9);
  }

  public TableMetaData(Object tableName, Object tableType, Object columnName, Object ordinalPosition,
                       Object isNullable, Object dataType, Object characterMaximumLength, Object numericPrecisionRadix,
                       Object numericScale, Object collationName) {

    this.tableName = (String)tableName;
    this.tableType = (String)tableType;
    this.columnName =  (String)columnName;
    this.ordinalPosition = (Integer)ordinalPosition;
    this.isNullable = (String)isNullable;
    this.dataType =  (String)dataType;
    this.characterMaximumLength = (Integer)characterMaximumLength;
    this.numericPrecisionRadix = (Integer)numericPrecisionRadix;
    this.numericScale = (Integer)numericScale;
    this.collationName =  (String)collationName;
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
    this.dataType = dataType;
  }

  public Integer getCharacterMaximumLength() {
    return characterMaximumLength;
  }

  public void setCharacterMaximumLength(Integer characterMaximumLength) {
    this.characterMaximumLength = characterMaximumLength;
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
}
