package com.diffdb.service;

import com.diffdb.model.TableInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableBrowserService {

    private TableBrowserService() {
    }

    public static List<TableInfo> listTables(Connection connection, String schema) {
        try {
            String dbName = connection.getMetaData().getDatabaseProductName();
            boolean isMySQL = dbName != null && (dbName.toLowerCase().contains("mysql") || dbName.toLowerCase().contains("mariadb"));
            boolean isPostgres = dbName != null && dbName.toLowerCase().contains("postgresql");

            if (isMySQL) {
                return listTablesMySQL(connection, schema);
            } else if (isPostgres) {
                // Ensure we see the latest state (avoid stale transaction snapshot)
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                return listTablesGeneric(connection, schema);
            }
            return listTablesGeneric(connection, schema);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * MySQL-specific: uses {@code SHOW FULL TABLES} to bypass the
     * {@code information_schema} cache that can return stale (already-deleted) tables.
     */
    private static List<TableInfo> listTablesMySQL(Connection connection, String schema) throws SQLException {
        String effectiveSchema = schema != null && !schema.isBlank() ? schema : connection.getCatalog();
        String sql = "SHOW FULL TABLES";
        if (effectiveSchema != null && !effectiveSchema.isBlank()) {
            sql = "SHOW FULL TABLES FROM `" + effectiveSchema + "` WHERE Table_type = 'BASE TABLE'";
        } else {
            sql = "SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'";
        }
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<TableInfo> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString(1);
                int colCount = countColumnsMySQL(connection, effectiveSchema, tableName);
                if (colCount >= 0) {
                    tables.add(new TableInfo(tableName, colCount));
                }
            }
            tables.sort(Comparator.comparing(TableInfo::getTableName, String.CASE_INSENSITIVE_ORDER));
            return tables;
        }
    }

    private static int countColumnsMySQL(Connection connection, String schema, String tableName) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?")) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return -1;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    private static List<TableInfo> listTablesGeneric(Connection connection, String schema) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String effectiveSchema = resolveSchema(connection, schema);
        Map<String, TableInfo> tableMap = new LinkedHashMap<>();
        try (ResultSet rs = meta.getTables(null, effectiveSchema, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                if (tableMap.containsKey(tableName)) {
                    continue;
                }
                int columnCount = countColumns(meta, null, effectiveSchema, tableName);
                if (columnCount < 0) {
                    continue;
                }
                tableMap.put(tableName, new TableInfo(tableName, columnCount));
            }
        }
        List<TableInfo> tables = new ArrayList<>(tableMap.values());
        tables.sort(Comparator.comparing(TableInfo::getTableName, String.CASE_INSENSITIVE_ORDER));
        return tables;
    }

    private static String resolveSchema(Connection connection, String schema) throws SQLException {
        if (schema != null && !schema.isBlank()) {
            return schema;
        }
        String dbSchema = connection.getSchema();
        if (dbSchema != null && !dbSchema.isBlank()) {
            return dbSchema;
        }
        String catalog = connection.getCatalog();
        if (catalog != null && !catalog.isBlank()) {
            return catalog;
        }
        return null;
    }

    static int countColumns(DatabaseMetaData meta, String catalog, String schema, String tableName) {
        try (ResultSet rs = meta.getColumns(catalog, schema, tableName, "%")) {
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        } catch (SQLException e) {
            return -1;
        }
    }
}
