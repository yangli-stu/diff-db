package com.diffdb;

import org.junit.jupiter.api.Test;

/**
 * Not an assertion test — prints the generated upgrade SQL for each case so the
 * concrete statements (CREATE TABLE / ALTER TABLE ADD / DROP TABLE) are visible
 * in the test output. Run with: ./gradlew test -i
 */
class PrintUpgradeSqlTest {

    @Test
    void printAll() throws Exception {
        for (String c : new String[]{"case1", "case2", "case3"}) {
            System.out.println("======== " + c + " : origin -> target upgrade SQL ========");
            System.out.println(SchemaDiffFixture.upgradeSql(c, true));
        }
    }
}
