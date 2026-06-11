package com.diffdb.connection;

import com.diffdb.model.DatabaseType;

/**
 * Builds JDBC URLs from a {@link DatabaseType} and the effective host/port/database.
 *
 * <p>The host/port passed in are the <b>effective</b> ones the driver should dial,
 * i.e. when a tunnel is active they are {@code 127.0.0.1:<localPort>}.
 *
 * <p>Database-specific parameters are appended to speed up metadata queries:
 * <ul>
 *   <li>MySQL: {@code useInformationSchema=true} — use {@code information_schema} directly
 *       instead of {@code SHOW} commands; significantly faster for large schemas.</li>
 *   <li>PostgreSQL: {@code applicationName=DiffDB} — identify the connection.</li>
 * </ul>
 */
public final class JdbcUrlBuilder {

    private JdbcUrlBuilder() {
    }

    public static String build(DatabaseType type, String host, int port, String database) {
        String db = database == null ? "" : database;
        return String.format(type.getUrlTemplate(), host, port, db);
    }

    /**
     * Builds a JDBC URL with performance/stability parameters appended.
     * Used for actual connections; tests use {@link #build} for clean URLs.
     */
    public static String buildWithParams(DatabaseType type, String host, int port, String database) {
        String base = build(type, host, port, database);
        return switch (type) {
            case MYSQL -> base + "?useInformationSchema=true"
                    + "&useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&connectTimeout=10000"
                    + "&socketTimeout=600000"
                    + "&cacheServerConfiguration=true"
                    + "&useLocalSessionState=true"
                    + "&elideSetAutoCommits=true"
                    + "&alwaysSendSetIsolation=false"
                    + "&maintainTimeStats=false"
                    + "&cacheResultSetMetadata=true"
                    + "&useServerPrepStmts=false"
                    + "&autoReconnect=false";
            case POSTGRESQL -> base + "?applicationName=DiffDB"
                    + "&sslmode=prefer"
                    + "&connectTimeout=10"
                    + "&socketTimeout=300"
                    + "&assumeMinServerVersion=9.0"
                    + "&preferQueryMode=simple"
                    + "&autoCommit=false";
        };
    }
}
