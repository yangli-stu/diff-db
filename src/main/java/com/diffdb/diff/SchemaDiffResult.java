package com.diffdb.diff;

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

    public SchemaDiffResult(List<DiffNode> roots, DiffResult rawDiff, boolean empty) {
        this.roots = roots;
        this.rawDiff = rawDiff;
        this.empty = empty;
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
}
