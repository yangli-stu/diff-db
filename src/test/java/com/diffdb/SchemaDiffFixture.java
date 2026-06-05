package com.diffdb;

import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.migration.LiquibaseMigrationSqlGenerator;
import com.diffdb.migration.MigrationOptions;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Loads {@code origin_db.sql} / {@code target_db.sql} from {@code src/test/resources/<case>/}
 * into two H2 in-memory databases and runs a Liquibase schema diff.
 *
 * <p>Direction convention: <b>origin</b> is the old database to be upgraded,
 * <b>target</b> is the desired/new state. Liquibase is called with
 * {@code reference = target} and {@code comparison = origin}, so:
 * <ul>
 *   <li>{@link #hasMissing} = present in target but not origin = needs to be created on origin</li>
 *   <li>{@link #hasUnexpected} = present in origin but not target = needs to be dropped from origin</li>
 *   <li>{@link #upgradeSql} = the SQL that migrates origin up to target</li>
 * </ul>
 */
public final class SchemaDiffFixture {

    private SchemaDiffFixture() {
    }

    @FunctionalInterface
    private interface DiffFunction<T> {
        T apply(DiffResult diff) throws Exception;
    }

    private static <T> T withDiff(String caseName, DiffFunction<T> fn) throws Exception {
        String id = caseName + "-" + java.util.UUID.randomUUID();
        try (Connection origin = h2(id + "-origin");
             Connection target = h2(id + "-target")) {

            applyScript(origin, loadScript(caseName, "origin_db.sql"));
            applyScript(target, loadScript(caseName, "target_db.sql"));

            // reference = desired (target), comparison = to-be-upgraded (origin)
            Database referenceDb = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(target));
            Database comparisonDb = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(origin));

            DiffResult diff = DiffGeneratorFactory.getInstance()
                    .compare(referenceDb, comparisonDb, new CompareControl());

            return fn.apply(diff);
        }
    }

    /** Runs the diff for a case and returns the raw result (connections already closed). */
    public static DiffResult diffCase(String caseName) throws Exception {
        return withDiff(caseName, d -> d);
    }

    /**
     * Generates the migration SQL that upgrades {@code origin_db} to {@code target_db}.
     *
     * @param includeDrops whether to emit DROP statements for objects only in origin
     */
    public static String upgradeSql(String caseName, boolean includeDrops) throws Exception {
        return withDiff(caseName, diff -> {
            MigrationOptions options = new MigrationOptions();
            options.setIncludeDrops(includeDrops);
            return new LiquibaseMigrationSqlGenerator()
                    .generate(new SchemaDiffResult(null, diff, false), options);
        });
    }

    public static boolean hasMissing(DiffResult diff, String typeSimpleName, String objectName) {
        return diff.getMissingObjects().stream()
                .anyMatch(o -> matches(o, typeSimpleName, objectName));
    }

    public static boolean hasUnexpected(DiffResult diff, String typeSimpleName, String objectName) {
        return diff.getUnexpectedObjects().stream()
                .anyMatch(o -> matches(o, typeSimpleName, objectName));
    }

    public static boolean hasAnyMissingOfType(DiffResult diff, String typeSimpleName) {
        return diff.getMissingObjects().stream()
                .anyMatch(o -> typeSimpleName.equals(o.getClass().getSimpleName()));
    }

    public static boolean hasAnyChanged(DiffResult diff) {
        return !diff.getChangedObjects().isEmpty();
    }

    public static boolean hasChangedOn(DiffResult diff, String typeSimpleName, String objectName) {
        return diff.getChangedObjects().keySet().stream()
                .anyMatch(o -> matches(o, typeSimpleName, objectName));
    }

    private static boolean matches(DatabaseObject o, String typeSimpleName, String objectName) {
        return typeSimpleName.equals(o.getClass().getSimpleName())
                && objectName.equalsIgnoreCase(String.valueOf(o.getName()));
    }

    private static String loadScript(String caseName, String fileName) throws Exception {
        String path = caseName + "/" + fileName;
        try (InputStream in = SchemaDiffFixture.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void applyScript(Connection conn, String script) throws Exception {
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
        // No DB_CLOSE_DELAY: the in-memory db is dropped when its single connection closes,
        // so each fixture run is fully isolated.
        return DriverManager.getConnection(
                "jdbc:h2:mem:" + name + ";MODE=MySQL", "sa", "");
    }
}
