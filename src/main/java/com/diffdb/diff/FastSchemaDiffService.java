package com.diffdb.diff;

import com.diffdb.connection.ConnectionManager;
import com.diffdb.connection.ManagedConnection;
import com.diffdb.model.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A fast schema diff implementation that bypasses Liquibase's per-table snapshot
 * and uses bulk {@link DatabaseMetaData} queries instead.
 *
 * <p>For a schema with 100 tables, Liquibase issues ~500 round-trip queries
 * (getColumns × 100, getIndexInfo × 100, getPrimaryKeys × 100, etc.).
 * Over SSH each query adds ~100ms latency → 50s+ total. This implementation
 * issues only 5 bulk queries with {@code "%"} table wildcard, reducing the
 * round-trip count from hundreds to single digits.
 *
 * <p>Currently compares: tables, columns, indexes, primary keys, foreign keys.
 */
public class FastSchemaDiffService implements SchemaDiffService {

    @Override
    public SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target, SecretResolver secrets,
                                 ProgressListener listener) throws Exception {
        step(listener, "Connecting to source database...");
        try (ManagedConnection srcConn = ConnectionManager.open(source, secrets.dbPassword(source), secrets.sshSecret(source))) {
            step(listener, "Connecting to target database...");
            try (ManagedConnection tgtConn = ConnectionManager.open(target, secrets.dbPassword(target), secrets.sshSecret(target))) {

                step(listener, "Reading source schema...");
                SchemaSnapshot sourceSnapshot = readSchema(srcConn.getConnection(), source.getSchema());

                step(listener, "Reading target schema...");
                SchemaSnapshot targetSnapshot = readSchema(tgtConn.getConnection(), target.getSchema());

                step(listener, "Comparing schemas...");
                List<DiffNode> roots = compareSnapshots(sourceSnapshot, targetSnapshot);

                step(listener, "Building diff result...");
                String srcSchemaName = effectiveSchema(source, srcConn.getConnection());
                String tgtSchemaName = effectiveSchema(target, tgtConn.getConnection());
                return new SchemaDiffResult(roots, null, roots.isEmpty(), srcSchemaName, tgtSchemaName,
                        sourceSnapshot, targetSnapshot);
            }
        }
    }

    /**
     * Direct-connection variant for tests (no ConnectionConfig/SSH needed).
     */
    public SchemaDiffResult diff(Connection sourceConn, Connection targetConn, String schema) throws Exception {
        SchemaSnapshot sourceSnapshot = readSchema(sourceConn, schema);
        SchemaSnapshot targetSnapshot = readSchema(targetConn, schema);
        List<DiffNode> roots = compareSnapshots(sourceSnapshot, targetSnapshot);
        String s = resolveSchema(sourceConn, schema);
        return new SchemaDiffResult(roots, null, roots.isEmpty(), s, s, sourceSnapshot, targetSnapshot);
    }

    private static String effectiveSchema(ConnectionConfig config, Connection conn) throws SQLException {
        String configured = config.getEffectiveSchema();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return resolveSchema(conn, null);
    }

    private static void step(ProgressListener listener, String message) {
        if (listener != null) {
            listener.onStep(message);
        }
    }

    /** Exposed for TwoStepDiffService. */
    public String resolveSchemaPublic(Connection conn, String schemaName) throws SQLException {
        return resolveSchema(conn, schemaName);
    }

    /** Exposed for TwoStepDiffService. */
    public SchemaSnapshot readSchemaPublic(Connection conn, String schemaName) throws SQLException {
        return readSchema(conn, schemaName);
    }

    /** Exposed for TwoStepDiffService. */
    public String effectiveSchemaPublic(ConnectionConfig config, Connection conn) throws SQLException {
        return effectiveSchema(config, conn);
    }

    /** Exposed for TwoStepDiffService. */
    public List<DiffNode> compareSnapshotsPublic(SchemaSnapshot source, SchemaSnapshot target) {
        return compareSnapshots(source, target);
    }

    private static String resolveSchema(Connection conn, String schemaName) throws SQLException {
        if (schemaName != null && !schemaName.isBlank()) {
            return schemaName;
        }
        // H2/PostgreSQL: getSchema() returns the schema name (e.g. "PUBLIC")
        String dbSchema = conn.getSchema();
        if (dbSchema != null && !dbSchema.isBlank()) {
            return dbSchema;
        }
        // MySQL: getSchema() is null, getCatalog() returns the database name
        String catalog = conn.getCatalog();
        if (catalog != null && !catalog.isBlank()) {
            return catalog;
        }
        return null;
    }

    private SchemaSnapshot readSchema(Connection conn, String schemaName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String schema = resolveSchema(conn, schemaName);
        String dbProduct = meta.getDatabaseProductName();
        boolean isMySQL = dbProduct != null && (dbProduct.toLowerCase().contains("mysql") || dbProduct.toLowerCase().contains("mariadb"));
        boolean isPostgres = dbProduct != null && dbProduct.toLowerCase().contains("postgresql");

        SchemaSnapshot result = new SchemaSnapshot();

        // For MySQL: flush table cache to avoid information_schema staleness,
        // then get the real table list via SHOW TABLES.
        Set<String> realMySQLTables = new HashSet<>();
        if (isMySQL) {
            try (Statement stmt = conn.createStatement()) {
                try { stmt.execute("FLUSH TABLES"); }
                catch (SQLException ignored) {}
                String showSql = schema != null ? "SHOW TABLES FROM `" + schema + "`" : "SHOW TABLES";
                try (ResultSet rs = stmt.executeQuery(showSql)) {
                    while (rs.next()) {
                        realMySQLTables.add(rs.getString(1).toLowerCase());
                    }
                }
            }
        }

        // 1. Bulk load all tables (one query)
        try (ResultSet rs = meta.getTables(null, schema, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Filter out stale entries from information_schema cache (MySQL)
                if (isMySQL && !realMySQLTables.contains(tableName.toLowerCase())) {
                    continue;
                }
                TableSnapshot table = new TableSnapshot();
                table.setName(tableName);
                result.getTables().put(tableName, table);
            }
        }

        // 2. Bulk load all columns (one query for ALL tables)
        try (ResultSet rs = meta.getColumns(null, schema, "%", null)) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Filter out stale entries from information_schema cache (MySQL)
                if (isMySQL && !realMySQLTables.contains(tableName.toLowerCase())) {
                    continue;
                }
                TableSnapshot table = result.getTables().get(tableName);
                if (table == null) continue;

                ColumnSnapshot col = new ColumnSnapshot();
                col.setName(rs.getString("COLUMN_NAME"));
                col.setType(rs.getString("TYPE_NAME"));
                col.setSize(rs.getInt("COLUMN_SIZE"));
                col.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                col.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.setDefaultValue(rs.getString("COLUMN_DEF"));
                col.setOrdinal(rs.getInt("ORDINAL_POSITION"));
                col.setRemarks(rs.getString("REMARKS"));
                String auto = rs.getString("IS_AUTOINCREMENT");
                col.setAutoIncrement("YES".equals(auto));
                table.getColumns().put(col.getName(), col);
            }
        }

        // 2b. For MySQL/PostgreSQL: read full type definitions (ENUM values, etc.) from information_schema
        if (isMySQL || isPostgres) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                String typeSql = isMySQL
                    ? "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND DATA_TYPE IN ('enum', 'set')"
                    : "SELECT table_name, column_name, udt_name || '(' || (SELECT string_agg(enumlabel, ',') FROM pg_enum WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = c.udt_name)) || ')' AS column_type FROM information_schema.columns c WHERE table_schema = '" + schema + "' AND data_type = 'USER-DEFINED'";
                try (ResultSet rs = stmt.executeQuery(typeSql)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        TableSnapshot table = result.getTables().get(tableName);
                        if (table == null) continue;
                        String colName = rs.getString("COLUMN_NAME");
                        ColumnSnapshot col = table.getColumns().get(colName);
                        if (col != null) {
                            col.setTypeDefinition(rs.getString("COLUMN_TYPE"));
                        }
                    }
                }
            }
        }

        // 3. Load indexes (per-table for H2; bulk wildcard for others)
        Map<String, IndexSnapshot> indexMap = new HashMap<>();
        boolean bulkIndexesWorked = false;
        try (ResultSet rs = meta.getIndexInfo(null, schema, "%", false, false)) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Filter out stale entries from information_schema cache (MySQL)
                if (isMySQL && !realMySQLTables.contains(tableName.toLowerCase())) {
                    continue;
                }
                String indexName = rs.getString("INDEX_NAME");
                if (indexName == null) continue;
                TableSnapshot table = result.getTables().get(tableName);
                if (table == null) continue;
                bulkIndexesWorked = true;
                String key = tableName + "." + indexName;
                IndexSnapshot idx = indexMap.get(key);
                if (idx == null) {
                    idx = new IndexSnapshot();
                    idx.setName(indexName);
                    idx.setUnique(!rs.getBoolean("NON_UNIQUE"));
                    indexMap.put(key, idx);
                }
                idx.addColumn(rs.getString("COLUMN_NAME"));
            }
        }
        if (!bulkIndexesWorked) {
            // H2 does not support wildcard in getIndexInfo; query per table.
            for (TableSnapshot table : result.getTables().values()) {
                try (ResultSet rs = meta.getIndexInfo(null, schema, table.getName(), false, false)) {
                    while (rs.next()) {
                        String indexName = rs.getString("INDEX_NAME");
                        if (indexName == null) continue;
                        String key = table.getName() + "." + indexName;
                        IndexSnapshot idx = indexMap.get(key);
                        if (idx == null) {
                            idx = new IndexSnapshot();
                            idx.setName(indexName);
                            idx.setUnique(!rs.getBoolean("NON_UNIQUE"));
                            indexMap.put(key, idx);
                        }
                        idx.addColumn(rs.getString("COLUMN_NAME"));
                    }
                }
            }
        }
        for (Map.Entry<String, IndexSnapshot> e : indexMap.entrySet()) {
            String key = e.getKey();
            String tableName = key.substring(0, key.lastIndexOf('.'));
            TableSnapshot table = result.getTables().get(tableName);
            if (table != null) {
                table.getIndexes().put(e.getValue().getName(), e.getValue());
            }
        }

        // 4. Bulk load all primary keys via information_schema (one query for ALL tables)
        if (isMySQL || isPostgres) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                // Primary keys
                String pkSql = isMySQL
                    ? "SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = '" + schema + "' AND CONSTRAINT_NAME = 'PRIMARY'"
                    : "SELECT tc.table_name, kcu.column_name, tc.constraint_name FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_schema = '" + schema + "'";
                try (ResultSet rs = stmt.executeQuery(pkSql)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        TableSnapshot table = result.getTables().get(tableName);
                        if (table != null) {
                            table.setPrimaryKey(rs.getString("CONSTRAINT_NAME"));
                            table.getPrimaryKeyColumns().add(rs.getString("COLUMN_NAME"));
                        }
                    }
                }

                // Foreign keys
                String fkSql = isMySQL
                    ? "SELECT TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME, CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = '" + schema + "' AND REFERENCED_TABLE_NAME IS NOT NULL"
                    : "SELECT tc.table_name, kcu.column_name, ccu.table_name AS referenced_table_name, ccu.column_name AS referenced_column_name, tc.constraint_name FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema = '" + schema + "'";
                try (ResultSet rs = stmt.executeQuery(fkSql)) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        TableSnapshot table = result.getTables().get(tableName);
                        if (table != null) {
                            ForeignKeySnapshot fk = new ForeignKeySnapshot();
                            fk.setName(rs.getString("CONSTRAINT_NAME"));
                            fk.setPkTable(rs.getString("REFERENCED_TABLE_NAME"));
                            fk.setPkColumn(rs.getString("REFERENCED_COLUMN_NAME"));
                            fk.setFkColumn(rs.getString("COLUMN_NAME"));
                            table.getForeignKeys().put(fk.getName(), fk);
                        }
                    }
                }
            }
        } else {
            // Fallback: per-table JDBC API for unknown databases
            for (TableSnapshot table : result.getTables().values()) {
                try (ResultSet rs = meta.getPrimaryKeys(null, schema, table.getName())) {
                    while (rs.next()) {
                        table.setPrimaryKey(rs.getString("PK_NAME"));
                        table.getPrimaryKeyColumns().add(rs.getString("COLUMN_NAME"));
                    }
                }
            }
            for (TableSnapshot table : result.getTables().values()) {
                try (ResultSet rs = meta.getImportedKeys(null, schema, table.getName())) {
                    while (rs.next()) {
                        ForeignKeySnapshot fk = new ForeignKeySnapshot();
                        fk.setName(rs.getString("FK_NAME"));
                        fk.setPkTable(rs.getString("PKTABLE_NAME"));
                        fk.setPkColumn(rs.getString("PKCOLUMN_NAME"));
                        fk.setFkColumn(rs.getString("FKCOLUMN_NAME"));
                        table.getForeignKeys().put(fk.getName(), fk);
                    }
                }
            }
        }

        return result;
    }

    private List<DiffNode> compareSnapshots(SchemaSnapshot source, SchemaSnapshot target) {
        List<DiffNode> roots = new ArrayList<>();

        DiffNode missing = DiffNode.container("Missing in target (to create)");
        DiffNode unexpected = DiffNode.container("Only in target (to drop)");
        DiffNode changed = DiffNode.container("Changed");

        // MISSING = source has but target lacks → needs to be created on target
        for (String tableName : source.getTables().keySet()) {
            if (!target.getTables().containsKey(tableName)) {
                DiffNode typeNode = missing.addChild(DiffNode.container("Table"));
                typeNode.addChild(new DiffNode("Table", tableName, DiffCategory.MISSING, ""));
            }
        }

        // UNEXPECTED = target has but source lacks → needs to be dropped from target
        for (String tableName : target.getTables().keySet()) {
            if (!source.getTables().containsKey(tableName)) {
                DiffNode typeNode = unexpected.addChild(DiffNode.container("Table"));
                typeNode.addChild(new DiffNode("Table", tableName, DiffCategory.UNEXPECTED, ""));
                continue;
            }

            TableSnapshot srcTable = source.getTables().get(tableName);
            TableSnapshot tgtTable = target.getTables().get(tableName);

            // Columns: MISSING = source has but target lacks
            for (String colName : srcTable.getColumns().keySet()) {
                if (!tgtTable.getColumns().containsKey(colName)) {
                    DiffNode typeNode = missing.addChild(DiffNode.container("Column"));
                    String type = srcTable.getColumns().get(colName).getSqlType();
                    typeNode.addChild(new DiffNode("Column", tableName + "." + colName, DiffCategory.MISSING, "type: " + type));
                } else {
                    String detail = srcTable.getColumns().get(colName).diffDetail(tgtTable.getColumns().get(colName));
                    if (!detail.isEmpty()) {
                        DiffNode typeNode = changed.addChild(DiffNode.container("Column"));
                        String newType = srcTable.getColumns().get(colName).getSqlType();
                        typeNode.addChild(new DiffNode("Column", tableName + "." + colName,
                                DiffCategory.CHANGED, "sqltype: " + newType + "; " + detail));
                    }
                }
            }
            // Columns: UNEXPECTED = target has but source lacks
            for (String colName : tgtTable.getColumns().keySet()) {
                if (!srcTable.getColumns().containsKey(colName)) {
                    DiffNode typeNode = unexpected.addChild(DiffNode.container("Column"));
                    typeNode.addChild(new DiffNode("Column", tableName + "." + colName, DiffCategory.UNEXPECTED, ""));
                }
            }

            // Indexes: MISSING = source has but target lacks
            for (Map.Entry<String, IndexSnapshot> e : srcTable.getIndexes().entrySet()) {
                String idxName = e.getKey();
                if (!tgtTable.getIndexes().containsKey(idxName)) {
                    DiffNode typeNode = missing.addChild(DiffNode.container("Index"));
                    String cols = String.join(", ", e.getValue().getColumns());
                    typeNode.addChild(new DiffNode("Index", tableName + "." + idxName, DiffCategory.MISSING, "columns: " + cols));
                }
            }
            // Indexes: UNEXPECTED = target has but source lacks
            for (Map.Entry<String, IndexSnapshot> e : tgtTable.getIndexes().entrySet()) {
                String idxName = e.getKey();
                if (!srcTable.getIndexes().containsKey(idxName)) {
                    DiffNode typeNode = unexpected.addChild(DiffNode.container("Index"));
                    typeNode.addChild(new DiffNode("Index", tableName + "." + idxName, DiffCategory.UNEXPECTED, ""));
                }
            }

            // Compare primary keys
            if (!Objects.equals(srcTable.getPrimaryKey(), tgtTable.getPrimaryKey())) {
                DiffNode typeNode = changed.addChild(DiffNode.container("PrimaryKey"));
                typeNode.addChild(new DiffNode("PrimaryKey", tableName, DiffCategory.CHANGED,
                        "pk: " + srcTable.getPrimaryKey() + " -> " + tgtTable.getPrimaryKey()));
            }

            // Foreign keys: MISSING = source has but target lacks
            for (String fkName : srcTable.getForeignKeys().keySet()) {
                if (!tgtTable.getForeignKeys().containsKey(fkName)) {
                    DiffNode typeNode = missing.addChild(DiffNode.container("ForeignKey"));
                    typeNode.addChild(new DiffNode("ForeignKey", tableName + "." + fkName, DiffCategory.MISSING, ""));
                }
            }
            // Foreign keys: UNEXPECTED = target has but source lacks
            for (String fkName : tgtTable.getForeignKeys().keySet()) {
                if (!srcTable.getForeignKeys().containsKey(fkName)) {
                    DiffNode typeNode = unexpected.addChild(DiffNode.container("ForeignKey"));
                    typeNode.addChild(new DiffNode("ForeignKey", tableName + "." + fkName, DiffCategory.UNEXPECTED, ""));
                }
            }
        }

        if (missing.hasChildren()) roots.add(missing);
        if (unexpected.hasChildren()) roots.add(unexpected);
        if (changed.hasChildren()) roots.add(changed);
        return roots;
    }
}
