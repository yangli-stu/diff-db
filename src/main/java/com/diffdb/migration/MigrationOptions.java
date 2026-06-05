package com.diffdb.migration;

/**
 * Options controlling migration SQL generation.
 */
public class MigrationOptions {

    /** Include DROP statements for objects only present in the target. */
    private boolean includeDrops = true;

    /** Qualify object names with their schema. */
    private boolean includeSchema = false;

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

    public static MigrationOptions defaults() {
        return new MigrationOptions();
    }
}
