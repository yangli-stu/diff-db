package com.diffdb;

import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.diff.TwoStepDiffService;
import com.diffdb.migration.FastMigrationSqlGenerator;
import com.diffdb.migration.MigrationOptions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

    /**
     * Loads {@code origin_db.sql} / {@code target_db.sql} from {@code src/test/resources/<case>/}
     * into two H2 in-memory databases and runs a schema diff.
     *
     * <p>Direction convention: <b>origin</b> is the old database to be upgraded,
     * <b>target</b> is the desired/new state.
     * <ul>
     *   <li>{@link #hasMissing} = present in target (desired) but not origin (old) = needs to be created on origin</li>
     *   <li>{@link #hasUnexpected} = present in origin (old) but not target (desired) = needs to be dropped from origin</li>
     *   <li>{@link #upgradeSql} = the SQL that migrates origin up to target</li>
     * </ul>
     */
public final class SchemaDiffFixture {

    private SchemaDiffFixture() {
    }

    /** Runs the diff for a case and returns the result (connections already closed). */
    public static SchemaDiffResult diffCase(String caseName) throws Exception {
        String id = caseName + "-" + java.util.UUID.randomUUID();
        try (Connection origin = h2(id + "-origin");
             Connection target = h2(id + "-target")) {

            applyScript(origin, loadScript(caseName, "origin_db.sql"));
            applyScript(target, loadScript(caseName, "target_db.sql"));

            TwoStepDiffService service = new TwoStepDiffService();
            // source = target (desired/new), target = origin (old/current)
            // H2 MySQL mode → use "mysql" Liquibase short name
            return service.diff(target, origin, "PUBLIC", "mysql");
        }
    }

    /**
     * Generates the migration SQL that upgrades {@code origin_db} to {@code target_db}.
     *
     * @param includeDrops whether to emit DROP statements for objects only in origin
     */
    public static String upgradeSql(String caseName, boolean includeDrops) throws Exception {
        SchemaDiffResult diff = diffCase(caseName);
        MigrationOptions options = new MigrationOptions();
        options.setIncludeDrops(includeDrops);
        return new FastMigrationSqlGenerator().generate(diff, options);
    }

    public static boolean hasMissing(SchemaDiffResult diff, String typeSimpleName, String objectName) {
        return hasInCategory(diff, com.diffdb.diff.DiffCategory.MISSING, typeSimpleName, objectName);
    }

    public static boolean hasUnexpected(SchemaDiffResult diff, String typeSimpleName, String objectName) {
        return hasInCategory(diff, com.diffdb.diff.DiffCategory.UNEXPECTED, typeSimpleName, objectName);
    }

    public static boolean hasAnyMissingOfType(SchemaDiffResult diff, String typeSimpleName) {
        for (com.diffdb.diff.DiffNode root : diff.getRoots()) {
            for (com.diffdb.diff.DiffNode typeNode : root.getChildren()) {
                if (!typeSimpleName.equals(typeNode.getObjectType())) continue;
                for (com.diffdb.diff.DiffNode item : typeNode.getChildren()) {
                    if (item.getCategory() == com.diffdb.diff.DiffCategory.MISSING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasAnyChanged(SchemaDiffResult diff) {
        for (com.diffdb.diff.DiffNode root : diff.getRoots()) {
            for (com.diffdb.diff.DiffNode typeNode : root.getChildren()) {
                for (com.diffdb.diff.DiffNode item : typeNode.getChildren()) {
                    if (item.getCategory() == com.diffdb.diff.DiffCategory.CHANGED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasChangedOn(SchemaDiffResult diff, String typeSimpleName, String objectName) {
        return hasInCategory(diff, com.diffdb.diff.DiffCategory.CHANGED, typeSimpleName, objectName);
    }

    private static boolean hasInCategory(SchemaDiffResult diff, com.diffdb.diff.DiffCategory category,
                                          String typeSimpleName, String objectName) {
        for (com.diffdb.diff.DiffNode root : diff.getRoots()) {
            for (com.diffdb.diff.DiffNode typeNode : root.getChildren()) {
                if (!typeSimpleName.equals(typeNode.getObjectType())) continue;
                for (com.diffdb.diff.DiffNode item : typeNode.getChildren()) {
                    if (item.getCategory() == category && objectName.equalsIgnoreCase(item.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String loadScript(String caseName, String fileName) throws Exception {
        String path = caseName + "/" + fileName;
        try (InputStream in = SchemaDiffFixture.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static void applyScript(Connection conn, String script) throws Exception {
        // Sanitize first (drop comments / MySQL-only table options), THEN split on ';'.
        String cleaned = sanitizeSql(script);
        for (String part : cleaned.split(";")) {
            String sql = part.trim();
            if (sql.isEmpty()) {
                continue;
            }
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            }
        }
    }

    /**
     * Makes a (possibly real) MySQL dump digestible by H2's MySQL mode:
     * <ul>
     *   <li>removes block comments {@code /* ... *&#47;} including executable {@code /*! ... *&#47;}</li>
     *   <li>removes line comments ({@code --}, {@code #}) and MySQL session statements ({@code SET NAMES ...})</li>
     *   <li>rewrites {@code enum(...)} columns to {@code varchar(255)} (H2 has no ENUM)</li>
     *   <li>normalizes bit literals {@code b'0'} -> {@code 0}</li>
     *   <li>strips column-level {@code CHARACTER SET x [COLLATE y]} and stray {@code COLLATE y}</li>
     *   <li>strips MySQL-only table options after the closing paren
     *       ({@code ) ENGINE=InnoDB DEFAULT CHARSET=... COLLATE=... AUTO_INCREMENT=...})</li>
     * </ul>
     * Note: H2's MySQL mode covers a large subset, but not every dump construct
     * (triggers, generated columns, spatial types, ...). For production-grade
     * fidelity prefer a real MySQL via Testcontainers; here we approximate so the
     * structural diff can run. ENUM->VARCHAR is a lossy but symmetric mapping
     * (applied to both sides), so it does not create false diffs.
     */
    static String sanitizeSql(String script) {
        // 1. block comments (also /*!40101 ... */ executable comments)
        String s = script.replaceAll("(?s)/\\*.*?\\*/", "");

        // 2. drop line comments (--, #) and MySQL session statements (SET ...)
        StringBuilder sb = new StringBuilder();
        for (String line : s.lines().toList()) {
            String t = line.trim();
            if (t.startsWith("--") || t.startsWith("#")) {
                continue;
            }
            if (t.toUpperCase().startsWith("SET ")) {
                // e.g. "SET NAMES utf8mb4;" — session setting, not DDL
                continue;
            }
            sb.append(line).append('\n');
        }
        s = sb.toString();

        // 3. enum(...) / set(...) -> varchar(255)  (must run before CHARACTER SET strip)
        s = s.replaceAll("(?is)\\benum\\s*\\([^)]*\\)", "varchar(255)");
        s = s.replaceAll("(?is)\\bset\\s*\\([^)]*\\)", "varchar(255)");

        // 4. bit literals: DEFAULT b'0' -> DEFAULT 0
        s = s.replaceAll("(?i)\\bb'([01]+)'", "$1");

        // 5. column-level CHARACTER SET x [COLLATE y]  -> removed
        s = s.replaceAll("(?i)\\s+CHARACTER\\s+SET\\s+\\w+(\\s+COLLATE\\s+\\w+)?", "");
        // 6. stray column-level COLLATE y (no '='; table-level uses COLLATE=...)
        s = s.replaceAll("(?i)\\s+COLLATE\\s+(?!=)\\w+", "");

        // 7. table options after the closing ')': ") ENGINE=... [until ;]" -> ")"
        s = s.replaceAll("(?is)\\)\\s*ENGINE=[^;]*", ")");
        return s;
    }

    private static Connection h2(String name) throws Exception {
        return DriverManager.getConnection(
                "jdbc:h2:mem:" + name + ";MODE=MySQL", "sa", "");
    }

    /** H2 connection that survives after close (shared in-memory database). */
    public static Connection persistentH2(String name) throws Exception {
        return DriverManager.getConnection(
                "jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    public static String dumpDiffSummary(SchemaDiffResult diff) {
        StringBuilder sb = new StringBuilder();
        if (diff.getRoots() != null) {
            for (com.diffdb.diff.DiffNode root : diff.getRoots()) {
                dumpNode(sb, root, 0);
            }
        }
        return sb.toString();
    }

    private static void dumpNode(StringBuilder sb, com.diffdb.diff.DiffNode node, int depth) {
        String prefix = "  ".repeat(depth);
        if (node.getCategory() == com.diffdb.diff.DiffCategory.CONTAINER) {
            sb.append(prefix).append("[").append(node.getObjectType()).append("]\n");
            for (com.diffdb.diff.DiffNode child : node.getChildren()) {
                dumpNode(sb, child, depth + 1);
            }
        } else {
            sb.append(prefix).append(node.getCategory()).append(" ")
                    .append(node.getObjectType()).append(" ").append(node.getName())
                    .append(" detail=").append(node.getDetail()).append("\n");
        }
    }
}
