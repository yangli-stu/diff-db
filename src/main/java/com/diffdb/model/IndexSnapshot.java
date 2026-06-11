package com.diffdb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of a single index.
 */
public class IndexSnapshot {

    private String name;
    private boolean unique;
    private final List<String> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public List<String> getColumns() {
        return columns;
    }

    /** Adds a column if it is not already present (preserves order, deduplicates). */
    public void addColumn(String column) {
        if (!columns.contains(column)) {
            columns.add(column);
        }
    }
}
