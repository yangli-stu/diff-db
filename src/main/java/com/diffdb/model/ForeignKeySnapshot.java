package com.diffdb.model;

/**
 * Snapshot of a single foreign key.
 */
public class ForeignKeySnapshot {

    private String name;
    private String pkTable;
    private String pkColumn;
    private String fkColumn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkTable() {
        return pkTable;
    }

    public void setPkTable(String pkTable) {
        this.pkTable = pkTable;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getFkColumn() {
        return fkColumn;
    }

    public void setFkColumn(String fkColumn) {
        this.fkColumn = fkColumn;
    }
}
