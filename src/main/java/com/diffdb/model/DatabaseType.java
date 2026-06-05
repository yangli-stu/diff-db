package com.diffdb.model;

/**
 * Supported relational databases. Each value carries the metadata needed to
 * build a JDBC URL and locate the driver, plus the Liquibase short name used by
 * the diff engine.
 */
public enum DatabaseType {

    MYSQL(
            "MySQL",
            3306,
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://%s:%d/%s",
            "mysql"),

    POSTGRESQL(
            "PostgreSQL",
            5432,
            "org.postgresql.Driver",
            "jdbc:postgresql://%s:%d/%s",
            "postgresql");

    private final String displayName;
    private final int defaultPort;
    private final String driverClassName;
    private final String urlTemplate;
    private final String liquibaseShortName;

    DatabaseType(String displayName, int defaultPort, String driverClassName,
                 String urlTemplate, String liquibaseShortName) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
        this.driverClassName = driverClassName;
        this.urlTemplate = urlTemplate;
        this.liquibaseShortName = liquibaseShortName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public String getLiquibaseShortName() {
        return liquibaseShortName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
