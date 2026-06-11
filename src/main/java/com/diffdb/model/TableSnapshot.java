package com.diffdb.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Snapshot of a single table.
 */
public class TableSnapshot {

    private String name;
    private final Map<String, ColumnSnapshot> columns = new LinkedHashMap<>();
    private final Map<String, IndexSnapshot> indexes = new HashMap<>();
    private String primaryKey;
    private final List<String> primaryKeyColumns = new ArrayList<>();
    private final Map<String, ForeignKeySnapshot> foreignKeys = new HashMap<>();

    public TableSnapshot() {
    }

    public TableSnapshot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ColumnSnapshot> getColumns() {
        return columns;
    }

    public Map<String, IndexSnapshot> getIndexes() {
        return indexes;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public Map<String, ForeignKeySnapshot> getForeignKeys() {
        return foreignKeys;
    }
}
