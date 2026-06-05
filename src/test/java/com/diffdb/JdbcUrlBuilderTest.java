package com.diffdb;

import com.diffdb.connection.JdbcUrlBuilder;
import com.diffdb.model.DatabaseType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcUrlBuilderTest {

    @Test
    void buildsMysqlUrl() {
        String url = JdbcUrlBuilder.build(DatabaseType.MYSQL, "127.0.0.1", 3307, "shop");
        assertEquals("jdbc:mysql://127.0.0.1:3307/shop", url);
    }

    @Test
    void buildsPostgresUrl() {
        String url = JdbcUrlBuilder.build(DatabaseType.POSTGRESQL, "localhost", 5432, "app");
        assertEquals("jdbc:postgresql://localhost:5432/app", url);
    }
}
