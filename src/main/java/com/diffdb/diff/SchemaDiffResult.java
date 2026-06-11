package com.diffdb.diff;

import com.diffdb.model.SchemaSnapshot;
import liquibase.diff.DiffResult;

import java.util.List;

/**
 * Result of one schema comparison.
 *
 * <p>Exposes a UI-friendly {@link DiffNode} tree, and retains the raw Liquibase
 * {@link DiffResult} for the migration SQL generator. The raw result is core-internal;
 * UI code should only touch {@link #getRoots()}.
 */
public class SchemaDiffResult {

    private final List<DiffNode> roots;
    private final DiffResult rawDiff;
    private final boolean empty;
    private final String sourceSchema;
    private final String targetSchema;
    private final SchemaSnapshot sourceSnapshot;
    private final SchemaSnapshot targetSnapshot;

    public SchemaDiffResult(List<DiffNode> roots, DiffResult rawDiff, boolean empty,
                            String sourceSchema, String targetSchema) {
        this(roots, rawDiff, empty, sourceSchema, targetSchema, null, null);
    }

    public SchemaDiffResult(List<DiffNode> roots, DiffResult rawDiff, boolean empty,
                            String sourceSchema, String targetSchema,
                            SchemaSnapshot sourceSnapshot, SchemaSnapshot targetSnapshot) {
        this.roots = roots;
        this.rawDiff = rawDiff;
        this.empty = empty;
        this.sourceSchema = sourceSchema;
        this.targetSchema = targetSchema;
        this.sourceSnapshot = sourceSnapshot;
        this.targetSnapshot = targetSnapshot;
    }

    public List<DiffNode> getRoots() {
        return roots;
    }

    /** Core-internal: used by the migration generator. */
    public DiffResult getRawDiff() {
        return rawDiff;
    }

    public boolean isEmpty() {
        return empty;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public SchemaSnapshot getSourceSnapshot() {
        return sourceSnapshot;
    }

    public SchemaSnapshot getTargetSnapshot() {
        return targetSnapshot;
    }
}
