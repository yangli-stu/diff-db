package com.diffdb.model;

import java.util.Objects;

/**
 * Snapshot of a single column.
 */
public class ColumnSnapshot {

    private String name;
    /** JDBC type name (e.g. "VARCHAR", "DECIMAL", "ENUM"). */
    private String type;
    /** Full type definition, e.g. "ENUM('A','B','C')" or "DECIMAL(10,2)". */
    private String typeDefinition;
    private int size;
    private int decimalDigits;
    private boolean nullable;
    private String defaultValue;
    private int ordinal;
    private String remarks;
    private boolean autoIncrement;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDefinition() {
        return typeDefinition;
    }

    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    /**
     * Returns the best SQL type definition for this column.
     * Prefers the full typeDefinition if present, otherwise builds from type + size + decimalDigits.
     */
    public String getSqlType() {
        if (typeDefinition != null && !typeDefinition.isBlank()) {
            return typeDefinition;
        }
        String normalized = normalizeType(type);
        StringBuilder sb = new StringBuilder();
        sb.append(normalized);
        if (size > 0 && !isIntegerLike(normalized)) {
            sb.append('(').append(size);
            if (decimalDigits > 0) {
                sb.append(',').append(decimalDigits);
            }
            sb.append(')');
        }
        return sb.toString();
    }

    private static boolean isIntegerLike(String type) {
        if (type == null) return false;
        String t = type.toLowerCase();
        return t.contains("int") || t.contains("serial") || t.contains("text") || t.contains("blob")
                || t.contains("date") || t.contains("time") || t.contains("json")
                || t.contains("boolean") || t.contains("bool") || t.contains("bytea")
                || t.equals("enum") || t.equals("set");
    }

    /** Normalize JDBC type names to SQL-standard forms. */
    private static String normalizeType(String type) {
        if (type == null) return "VARCHAR(255)";
        String upper = type.toUpperCase();
        if (upper.equals("CHARACTER VARYING") || upper.equals("CHAR") || upper.equals("CHARACTER")) {
            return "VARCHAR";
        }
        if (upper.startsWith("CHARACTER VARYING(")) {
            return "VARCHAR" + type.substring("CHARACTER VARYING".length());
        }
        if (upper.equals("INTEGER") || upper.equals("INT4")) {
            return "INT";
        }
        if (upper.equals("DECIMAL")) {
            return "DECIMAL";
        }
        return type;
    }

    /** Produces a human-readable diff description against another column. */
    public String diffDetail(ColumnSnapshot other) {
        StringBuilder sb = new StringBuilder();

        // Compare using normalized SQL types (handles ENUM, CHARACTER VARYING, etc.)
        String thisSql = this.getSqlType();
        String otherSql = other.getSqlType();
        if (!Objects.equals(thisSql, otherSql)) {
            append(sb, "type", thisSql, otherSql);
        }

        // Only compare size/digits for types that use them
        String baseType = normalizeType(type);
        boolean hasSize = !isIntegerLike(baseType);
        boolean hasDigits = "DECIMAL".equalsIgnoreCase(baseType)
                || "NUMERIC".equalsIgnoreCase(baseType);

        if (hasSize && size != other.size) {
            append(sb, "size", size, other.size);
        }
        if (hasDigits && decimalDigits != other.decimalDigits) {
            append(sb, "decimalDigits", decimalDigits, other.decimalDigits);
        }
        if (nullable != other.nullable) {
            append(sb, "nullable", nullable, other.nullable);
        }

        // Normalize defaults: "NULL" / null / "" are equivalent
        String thisDef = normalizeDefault(defaultValue);
        String otherDef = normalizeDefault(other.defaultValue);
        if (!Objects.equals(thisDef, otherDef)) {
            append(sb, "defaultValue", defaultValue, other.defaultValue);
        }

        // Normalize remarks: blank == null
        String thisRem = emptyToNull(remarks);
        String otherRem = emptyToNull(other.remarks);
        if (!Objects.equals(thisRem, otherRem)) {
            append(sb, "remarks", remarks, other.remarks);
        }

        if (autoIncrement != other.autoIncrement) {
            append(sb, "autoIncrement", autoIncrement, other.autoIncrement);
        }
        return sb.toString();
    }

    private static String normalizeDefault(String dv) {
        if (dv == null) return null;
        String t = dv.trim();
        // JDBC metadata may return "NULL", "null", "'NULL'", or empty for implicit NULL
        if (t.isEmpty()) return null;
        String upper = t.toUpperCase();
        if (upper.equals("NULL") || upper.equals("'NULL'")) return null;
        return t;
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static void append(StringBuilder sb, String field, Object a, Object b) {
        if (sb.length() > 0) sb.append("; ");
        sb.append(field).append(": ").append(a).append(" -> ").append(b);
    }
}
