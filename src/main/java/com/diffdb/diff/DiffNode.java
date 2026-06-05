package com.diffdb.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * UI-decoupled representation of one difference (or a grouping container).
 *
 * <p>Deliberately free of any Liquibase types so the UI layer never depends on the
 * diff engine, keeping the engine replaceable.
 */
public class DiffNode {

    private final String objectType;   // e.g. "Table", "Column", "Index", "Foreign Key"
    private final String name;         // object name
    private final DiffCategory category;
    private final String detail;       // human-readable description of the difference
    private final List<DiffNode> children = new ArrayList<>();

    public DiffNode(String objectType, String name, DiffCategory category, String detail) {
        this.objectType = objectType;
        this.name = name;
        this.category = category;
        this.detail = detail;
    }

    public static DiffNode container(String label) {
        return new DiffNode(label, "", DiffCategory.CONTAINER, "");
    }

    public DiffNode addChild(DiffNode child) {
        children.add(child);
        return child;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }

    public DiffCategory getCategory() {
        return category;
    }

    public String getDetail() {
        return detail;
    }

    public List<DiffNode> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /** Label used by the tree renderer. */
    public String displayLabel() {
        if (category == DiffCategory.CONTAINER) {
            return objectType;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(objectType).append(": ").append(name);
        if (detail != null && !detail.isBlank()) {
            sb.append("  (").append(detail).append(')');
        }
        return sb.toString();
    }
}
