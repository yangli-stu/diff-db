package com.diffdb.diff;

import com.diffdb.connection.ConnectionManager;
import com.diffdb.connection.ManagedConnection;
import com.diffdb.model.*;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Two-step schema diff: JDBC bulk read → compare → fix diff tables via SHOW → re-compare.
 *
 * <p>Performance: SHOW commands are per-table and expensive over SSH.
 * We only run them on tables that show differences in the initial fast comparison.
 * For a typical 5-table diff on 44 tables: 7 bulk + 10 SHOW = 17 queries (~2s).
 */
public class TwoStepDiffService implements SchemaDiffService {

    private static final Logger LOG = Logger.getInstance(TwoStepDiffService.class);
    private static final Pattern SIZE_PATTERN = Pattern.compile("\\((\\d+)\\)");
    private final FastSchemaDiffService reader = new FastSchemaDiffService();

    @Override
    public SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target,
                                  SecretResolver secrets, ProgressListener listener)
            throws Exception {
        step(listener, "Connecting to source database...");
        try (ManagedConnection sc = ConnectionManager.open(
                source, secrets.dbPassword(source), secrets.sshSecret(source))) {
            step(listener, "Connecting to target database...");
            try (ManagedConnection tc = ConnectionManager.open(
                    target, secrets.dbPassword(target), secrets.sshSecret(target))) {

                step(listener, "Reading source schema...");
                SchemaSnapshot ss = reader.readSchemaPublic(sc.getConnection(), source.getSchema());
                step(listener, "Reading target schema...");
                SchemaSnapshot ts = reader.readSchemaPublic(tc.getConnection(), target.getSchema());

                // Fast comparison
                step(listener, "Comparing...");
                List<DiffNode> roots = reader.compareSnapshotsPublic(ss, ts);

                // Only SHOW-correct tables that have diffs (performance-critical over SSH)
                step(listener, "Correcting differences...");
                Set<String> diffTables = collectDiffTables(roots);
                if (!diffTables.isEmpty()) {
                    correctTables(ss, sc.getConnection(), diffTables);
                    correctTables(ts, tc.getConnection(), diffTables);
                    roots = reader.compareSnapshotsPublic(ss, ts);
                }

                String srcName = reader.effectiveSchemaPublic(source, sc.getConnection());
                String tgtName = reader.effectiveSchemaPublic(target, tc.getConnection());
                return new SchemaDiffResult(roots, null, roots.isEmpty(),
                        srcName, tgtName, ss, ts);
            }
        }
    }

    public SchemaDiffResult diff(Connection sc, Connection tc, String schema, String dbType)
            throws Exception {
        SchemaSnapshot ss = reader.readSchemaPublic(sc, schema);
        SchemaSnapshot ts = reader.readSchemaPublic(tc, schema);
        List<DiffNode> roots = reader.compareSnapshotsPublic(ss, ts);
        Set<String> diffTables = collectDiffTables(roots);
        if (!diffTables.isEmpty()) {
            correctTables(ss, sc, diffTables);
            correctTables(ts, tc, diffTables);
            roots = reader.compareSnapshotsPublic(ss, ts);
        }
        String s = reader.resolveSchemaPublic(sc, schema);
        return new SchemaDiffResult(roots, null, roots.isEmpty(), s, s, ss, ts);
    }

    // ---- SHOW correction (only on specified tables) ----

    /**
     * Uses SHOW COLUMNS + SHOW INDEX to fix stale JDBC metadata and remove
     * phantom columns/indexes on the given tables.
     */
    static void correctTables(SchemaSnapshot schema, Connection conn, Set<String> tables) {
        int fixedNull = 0, fixedDef = 0, fixedSize = 0, phantomCols = 0, phantomIdxs = 0;

        try (Statement stmt = conn.createStatement()) {
            for (String tableName : tables) {
                TableSnapshot table = schema.getTables().get(tableName);
                if (table == null) continue;

                // SHOW COLUMNS
                Map<String, Boolean> showNulls = new HashMap<>();
                Map<String, String> showDefs = new HashMap<>();
                Map<String, String> showTypes = new HashMap<>();
                try (ResultSet rs = stmt.executeQuery(
                        "SHOW COLUMNS FROM `" + tableName + "`")) {
                    while (rs.next()) {
                        String name = rs.getString("Field").toLowerCase();
                        showNulls.put(name, "YES".equalsIgnoreCase(rs.getString("Null")));
                        String def = rs.getString("Default");
                        showDefs.put(name, def != null ? def : "");
                        showTypes.put(name, rs.getString("Type"));
                    }
                } catch (SQLException ignored) { continue; }

                // Remove phantom columns
                Iterator<Map.Entry<String, ColumnSnapshot>> ci =
                        table.getColumns().entrySet().iterator();
                while (ci.hasNext()) {
                    if (!showNulls.containsKey(ci.next().getValue().getName().toLowerCase())) {
                        ci.remove();
                        phantomCols++;
                    }
                }

                // Fix stale attributes
                for (ColumnSnapshot col : table.getColumns().values()) {
                    String key = col.getName().toLowerCase();
                    Boolean sn = showNulls.get(key);
                    if (sn != null && sn != col.isNullable()) { col.setNullable(sn); fixedNull++; }
                    String sd = showDefs.get(key);
                    if (sd != null && !sd.equals(col.getDefaultValue() == null ? "" : col.getDefaultValue())) {
                        col.setDefaultValue(sd.isEmpty() ? null : sd); fixedDef++;
                    }
                    String st = showTypes.get(key);
                    if (st != null) {
                        col.setTypeDefinition(st);
                        java.util.regex.Matcher m = SIZE_PATTERN.matcher(st);
                        if (m.find()) {
                            int realSize = Integer.parseInt(m.group(1));
                            if (col.getSize() != realSize) { col.setSize(realSize); fixedSize++; }
                        }
                    }
                }

                // SHOW INDEX
                Map<String, IndexSnapshot> realIdx = new LinkedHashMap<>();
                try (ResultSet rs = stmt.executeQuery(
                        "SHOW INDEX FROM `" + tableName + "`")) {
                    while (rs.next()) {
                        String n = rs.getString("Key_name");
                        if (n == null) continue;
                        final String idxName = n;
                        IndexSnapshot idx = realIdx.computeIfAbsent(idxName, k -> {
                            IndexSnapshot i = new IndexSnapshot();
                            i.setName(k);
                            try { i.setUnique(!rs.getBoolean("Non_unique")); }
                            catch (SQLException ignored) {}
                            return i;
                        });
                        String c = rs.getString("Column_name");
                        if (c != null) idx.addColumn(c);
                    }
                } catch (SQLException ignored) { continue; }

                phantomIdxs += table.getIndexes().size() - realIdx.size();
                table.getIndexes().clear();
                for (Map.Entry<String, IndexSnapshot> e : realIdx.entrySet()) {
                    if (!"PRIMARY".equalsIgnoreCase(e.getKey())) {
                        table.getIndexes().put(e.getKey(), e.getValue());
                    }
                }

                IndexSnapshot pk = realIdx.get("PRIMARY");
                // H2 returns "PRIMARY_KEY_XX" instead of "PRIMARY"
                if (pk == null) {
                    for (Map.Entry<String, IndexSnapshot> e : realIdx.entrySet()) {
                        if (e.getKey().toUpperCase().startsWith("PRIMARY")) {
                            pk = e.getValue();
                            table.getIndexes().remove(e.getKey()); // don't treat as regular index
                            break;
                        }
                    }
                }
                if (pk != null) {
                    table.setPrimaryKey("PRIMARY");
                    table.getPrimaryKeyColumns().clear();
                    table.getPrimaryKeyColumns().addAll(pk.getColumns());
                }
            }
        } catch (SQLException ignored) {}

        if (phantomCols + phantomIdxs + fixedNull + fixedDef + fixedSize > 0) {
            LOG.warn("correctTables: cols=" + phantomCols + " idxs=" + phantomIdxs
                    + " null=" + fixedNull + " def=" + fixedDef + " size=" + fixedSize);
        }
    }

    // ---- Helpers ----

    private static Set<String> collectDiffTables(List<DiffNode> roots) {
        Set<String> tables = new HashSet<>();
        for (DiffNode root : roots) {
            for (DiffNode typeNode : root.getChildren()) {
                for (DiffNode item : typeNode.getChildren()) {
                    int dot = item.getName().lastIndexOf('.');
                    if (dot > 0) tables.add(item.getName().substring(0, dot));
                    else tables.add(item.getName());
                }
            }
        }
        return tables;
    }

    private static void step(ProgressListener l, String m) { if (l != null) l.onStep(m); }
}
