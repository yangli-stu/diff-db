package com.diffdb.service;

import com.diffdb.model.TableInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableBrowserServiceTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE orders (id INT PRIMARY KEY, product VARCHAR(100), quantity INT)");
            st.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255))");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    void listTablesFromH2Connection() {
        List<TableInfo> tables = TableBrowserService.listTables(conn, null);

        assertEquals(2, tables.size());

        assertEquals("ORDERS", tables.get(0).getTableName());
        assertEquals(3, tables.get(0).getColumnCount());

        assertEquals("USERS", tables.get(1).getTableName());
        assertEquals(3, tables.get(1).getColumnCount());
    }
}