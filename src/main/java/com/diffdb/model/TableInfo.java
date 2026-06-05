package com.diffdb.model;

import java.util.Objects;

public class TableInfo {

    private final String tableName;
    private final int columnCount;

    public TableInfo(String tableName, int columnCount) {
        this.tableName = tableName;
        this.columnCount = columnCount;
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableInfo)) return false;
        TableInfo that = (TableInfo) o;
        return columnCount == that.columnCount && Objects.equals(tableName, that.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnCount);
    }

    @Override
    public String toString() {
        return tableName + " (" + columnCount + " columns)";
    }
}