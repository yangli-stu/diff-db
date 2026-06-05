package com.diffdb.diff;

/**
 * Classification of a difference, from the target's point of view.
 */
public enum DiffCategory {
    /** Present in source but missing in target -> needs to be created. */
    MISSING("Missing in target"),
    /** Present in target but not in source -> needs to be dropped. */
    UNEXPECTED("Only in target"),
    /** Present in both but with differing definition. */
    CHANGED("Changed"),
    /** Grouping node, no direct difference. */
    CONTAINER("");

    private final String displayName;

    DiffCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
