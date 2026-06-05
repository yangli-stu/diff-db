package com.diffdb.connection;

import com.diffdb.model.DatabaseType;

/**
 * Builds JDBC URLs from a {@link DatabaseType} and the effective host/port/database.
 *
 * <p>The host/port passed in are the <b>effective</b> ones the driver should dial,
 * i.e. when a tunnel is active they are {@code 127.0.0.1:<localPort>}.
 */
public final class JdbcUrlBuilder {

    private JdbcUrlBuilder() {
    }

    public static String build(DatabaseType type, String host, int port, String database) {
        String db = database == null ? "" : database;
        return String.format(type.getUrlTemplate(), host, port, db);
    }
}
