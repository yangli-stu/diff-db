package com.diffdb;

import liquibase.diff.DiffResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Schema diff scenarios driven by SQL fixtures under {@code src/test/resources/caseN/}.
 *
 * <p>Each case has {@code origin_db.sql} (old) and {@code target_db.sql} (new/desired).
 * Besides asserting the structural diff, every case also asserts the concrete
 * <b>upgrade SQL</b> that migrates origin up to target.
 */
class SchemaDiffCaseTest {

    /**
     * case1: target adds an {@code orders} table -> upgrade emits CREATE TABLE orders.
     */
    @Test
    void case1_addTable() throws Exception {
        DiffResult diff = SchemaDiffFixture.diffCase("case1");
        assertTrue(SchemaDiffFixture.hasMissing(diff, "Table", "orders"),
                "orders table should be reported as needed on origin");

        String sql = SchemaDiffFixture.upgradeSql("case1", false);
        String lower = sql.toLowerCase();
        assertTrue(lower.contains("create table") && lower.contains("orders"),
                "Expected CREATE TABLE orders, got:\n" + sql);
    }

    /**
     * case2: target adds a {@code price} column (and an index) -> upgrade emits
     * a concrete ALTER TABLE ... ADD price statement.
     */
    @Test
    void case2_addColumn() throws Exception {
        DiffResult diff = SchemaDiffFixture.diffCase("case2");
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

    /**
     * case3: target drops the {@code legacy_log} table -> upgrade with includeDrops
     * emits DROP TABLE legacy_log; without it, no drop appears.
     */
    @Test
    void case3_dropTable() throws Exception {
        DiffResult diff = SchemaDiffFixture.diffCase("case3");
        assertTrue(SchemaDiffFixture.hasUnexpected(diff, "Table", "legacy_log"),
                "legacy_log should be reported as removable from origin");

        String withDrops = SchemaDiffFixture.upgradeSql("case3", true).toLowerCase();
        assertTrue(withDrops.contains("drop table") && withDrops.contains("legacy_log"),
                "Expected DROP TABLE legacy_log when includeDrops=true, got:\n" + withDrops);

        String withoutDrops = SchemaDiffFixture.upgradeSql("case3", false).toLowerCase();
        assertFalse(withoutDrops.contains("drop table"),
                "DROP should be suppressed when includeDrops=false, got:\n" + withoutDrops);
    }

    /**
     * case4: fixtures are full, real MySQL dumps from two different databases
     * (backticks, AUTO_INCREMENT, json/bit/enum columns, inline KEY/UNIQUE KEY,
     * column- and table-level CHARACTER SET/COLLATE, '#' comments, SET NAMES).
     *
     * <p>This is a smoke test: it proves the dumps are parsed end-to-end and that
     * a non-trivial migration can be produced. The two schemas differ in many
     * tables/columns, so assertions stay coarse rather than enumerating every diff.
     */
    @Test
    void case4_realMysqlDump_endToEnd() throws Exception {
        DiffResult diff = SchemaDiffFixture.diffCase("case4");

        boolean hasStructuralDiff = !diff.getMissingObjects().isEmpty()
                || !diff.getUnexpectedObjects().isEmpty()
                || !diff.getChangedObjects().isEmpty();
        assertTrue(hasStructuralDiff, "Two different production schemas should differ");

        String upgrade = SchemaDiffFixture.upgradeSql("case4", true).toLowerCase();
        assertTrue(upgrade.contains("create table") || upgrade.contains("drop table")
                        || upgrade.contains("alter table"),
                "Expected a non-empty migration, got:\n"
                        + upgrade.substring(0, Math.min(upgrade.length(), 500)));
    }
}
