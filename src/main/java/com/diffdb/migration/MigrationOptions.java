package com.diffdb.migration;

import com.diffdb.model.DatabaseType;

/**
 * Options controlling migration SQL generation.
 */
public class MigrationOptions {

    /** Include DROP statements for objects only present in the target. */
    private boolean includeDrops = true;

    /** Qualify object names with their schema. */
    private boolean includeSchema = false;

    /** Schema/database name to prefix object names with (e.g. "mydb" or "public"). */
    private String targetSchema;

    /** Target database type so the generator can use dialect-aware syntax (e.g. IF EXISTS). */
    private DatabaseType targetDatabaseType;

    public boolean isIncludeDrops() {
        return includeDrops;
    }

    public void setIncludeDrops(boolean includeDrops) {
        this.includeDrops = includeDrops;
    }

    public boolean isIncludeSchema() {
        return includeSchema;
    }

    public void setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    public DatabaseType getTargetDatabaseType() {
        return targetDatabaseType;
    }

    public void setTargetDatabaseType(DatabaseType targetDatabaseType) {
        this.targetDatabaseType = targetDatabaseType;
    }

    public static MigrationOptions defaults() {
        return new MigrationOptions();
    }
}
