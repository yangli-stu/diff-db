package com.diffdb.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableInfoTest {

    @Test
    void constructorAndGetters() {
        TableInfo info = new TableInfo("products", 6);

        assertEquals("products", info.getTableName());
        assertEquals(6, info.getColumnCount());
    }

    @Test
    void equalsAndHashCode() {
        TableInfo a = new TableInfo("orders", 4);
        TableInfo b = new TableInfo("orders", 4);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentValues() {
        TableInfo a = new TableInfo("orders", 4);
        TableInfo b = new TableInfo("orders", 5);
        TableInfo c = new TableInfo("customers", 4);

        assertNotEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    void toStringFormat() {
        TableInfo info = new TableInfo("products", 6);

        assertEquals("products (6 columns)", info.toString());
    }
}