package com.diffdb.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight snapshot of an entire database schema (tables, columns, indexes, keys).
 * Used by the fast diff engine and the migration SQL generator.
 */
public class SchemaSnapshot {

    private final Map<String, TableSnapshot> tables = new HashMap<>();

    public Map<String, TableSnapshot> getTables() {
        return tables;
    }
}
