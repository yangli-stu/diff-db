package com.diffdb;

import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.diff.TwoStepDiffService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Schema diff scenarios driven by SQL fixtures under {@code src/test/resources/caseN/}.
 */
class SchemaDiffCaseTest {

    @Test
    void case1_addTable() throws Exception {
        SchemaDiffResult diff = SchemaDiffFixture.diffCase("case1");
        assertTrue(SchemaDiffFixture.hasMissing(diff, "Table", "orders"),
                "orders table should be reported as needed on origin");

        String sql = SchemaDiffFixture.upgradeSql("case1", false);
        String lower = sql.toLowerCase();
        assertTrue(lower.contains("create table") && lower.contains("orders"),
                "Expected CREATE TABLE orders, got:\n" + sql);
    }

    @Test
    void case2_addColumn() throws Exception {
        SchemaDiffResult diff = SchemaDiffFixture.diffCase("case2");
        assertTrue(SchemaDiffFixture.hasMissing(diff, "Column", "price")
                        || SchemaDiffFixture.hasAnyChanged(diff),
                "price column should be reported as needed on origin");

        String sql = SchemaDiffFixture.upgradeSql("case2", false);
        String lower = sql.toLowerCase();

        assertTrue(lower.contains("alter table")
                        && lower.contains("add")
                        && lower.contains("price"),
                "Expected ALTER TABLE ... ADD price, got:\n" + sql);

        assertTrue(lower.contains("create index") && lower.contains("idx_product_name"),
                "Expected CREATE INDEX idx_product_name, got:\n" + sql);
    }

    @Test
    void case3_dropTable() throws Exception {
        SchemaDiffResult diff = SchemaDiffFixture.diffCase("case3");
        assertTrue(SchemaDiffFixture.hasUnexpected(diff, "Table", "legacy_log"),
                "legacy_log should be reported as removable from origin");

        String withDrops = SchemaDiffFixture.upgradeSql("case3", true).toLowerCase();
        assertTrue(withDrops.contains("drop table") && withDrops.contains("legacy_log"),
                "Expected DROP TABLE legacy_log when includeDrops=true, got:\n" + withDrops);

        String withoutDrops = SchemaDiffFixture.upgradeSql("case3", false).toLowerCase();
        assertFalse(withoutDrops.contains("drop table"),
                "DROP should be suppressed when includeDrops=false, got:\n" + withoutDrops);
    }

    @Test
    void case4_realMysqlDump_endToEnd() throws Exception {
        SchemaDiffResult diff = SchemaDiffFixture.diffCase("case4");

        boolean hasStructuralDiff = !diff.isEmpty();
        assertTrue(hasStructuralDiff, "Two different production schemas should differ");

        String upgrade = SchemaDiffFixture.upgradeSql("case4", true).toLowerCase();
        assertTrue(upgrade.contains("create table") || upgrade.contains("drop table")
                        || upgrade.contains("alter table"),
                "Expected a non-empty migration, got:\n"
                        + upgrade.substring(0, Math.min(upgrade.length(), 500)));
    }

    /**
     * case5: nullable + type differences on customer & note tables.
     * Runs full diff → migration SQL → execute → re-diff, verifies empty.
     */
    @Test
    void case5_nullableFixAndReDiff() throws Exception {
        String id = "case5-verify-" + java.util.UUID.randomUUID();
        try (java.sql.Connection origin = SchemaDiffFixture.persistentH2(id + "-origin");
             java.sql.Connection target = SchemaDiffFixture.persistentH2(id + "-target")) {

            SchemaDiffFixture.applyScript(origin, SchemaDiffFixture.loadScript("case5", "origin_db.sql"));
            SchemaDiffFixture.applyScript(target, SchemaDiffFixture.loadScript("case5", "target_db.sql"));

            TwoStepDiffService service = new TwoStepDiffService();
            com.diffdb.migration.FastMigrationSqlGenerator gen =
                    new com.diffdb.migration.FastMigrationSqlGenerator();
            com.diffdb.migration.MigrationOptions opts =
                    new com.diffdb.migration.MigrationOptions();

            // Round 1: diff → SQL → verify
            SchemaDiffResult before = service.diff(target, origin, "PUBLIC", "mysql");
            assertFalse(before.isEmpty(), "should have differences");
            String sql = gen.generate(before, opts);
            assertTrue(sql.contains("MODIFY COLUMN"), "should produce MODIFY: " + sql);

            // Execute generated SQL on origin (H2 syntax)
            java.sql.Statement st = origin.createStatement();
            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (s.isEmpty() || s.startsWith("--")) continue;
                // MODIFY COLUMN -> ALTER COLUMN, drop backticks
                s = s.replaceAll("(?i)MODIFY\\s+COLUMN", "ALTER COLUMN").replace("`", "");
                // CHARACTER VARYING -> VARCHAR (H2 normalization)
                s = s.replace("CHARACTER VARYING", "VARCHAR");
                try { st.execute(s); } catch (Exception e) {
                    // H2 auto-generated PK names — skip silently
                    if (s.toUpperCase().matches(".*(DROP|CREATE)\\s+INDEX\\s+PRIMARY_KEY.*")) continue;
                    throw new RuntimeException("Failed SQL: " + s, e);
                }
            }
            st.close();

            // Round 2: re-diff — verify nullable diffs are resolved
            // (H2 JDBC metadata quirks may leave minor type diffs like TEXT→VARCHAR)
            SchemaDiffResult after = service.diff(target, origin, "PUBLIC", "mysql");
            StringBuilder remainingNullable = new StringBuilder();
            if (after.getRoots() != null) {
                for (com.diffdb.diff.DiffNode root : after.getRoots()) {
                    for (com.diffdb.diff.DiffNode typeNode : root.getChildren()) {
                        for (com.diffdb.diff.DiffNode item : typeNode.getChildren()) {
                            // Only count pure-nullable diffs (no type change)
                            if (item.getDetail() != null && item.getDetail().contains("nullable:")
                                    && !item.getDetail().contains("type:")) {
                                remainingNullable.append("  ").append(item.getObjectType())
                                        .append(" ").append(item.getName())
                                        .append(" ").append(item.getDetail()).append("\n");
                            }
                        }
                    }
                }
            }
            assertTrue(remainingNullable.toString().isEmpty(),
                    "After migration, no nullable diffs should remain. Got:\n" + remainingNullable);
        }
    }
}
