package com.diffdb.service;

import com.diffdb.model.TableInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TableBrowserService {

    private TableBrowserService() {
    }

    public static List<TableInfo> listTables(Connection connection, String schema) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            String effectiveSchema = schema != null ? schema : connection.getSchema();
            List<TableInfo> tables = new ArrayList<>();
            try (ResultSet rs = meta.getTables(null, effectiveSchema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    int columnCount = countColumns(meta, null, effectiveSchema, tableName);
                    tables.add(new TableInfo(tableName, columnCount));
                }
            }
            tables.sort(Comparator.comparing(TableInfo::getTableName, String.CASE_INSENSITIVE_ORDER));
            return tables;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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