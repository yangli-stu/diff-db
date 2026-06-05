package com.diffdb.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Full configuration of a single database connection.
 *
 * <p>The database password is intentionally <b>not</b> a field here: it is stored
 * in PasswordSafe keyed by {@link #getId()} so it never lands in plain text in
 * persisted state.
 */
public class ConnectionConfig {

    private String id = UUID.randomUUID().toString();
    private String name = "New connection";
    private DatabaseType databaseType = DatabaseType.MYSQL;

    private String host = "localhost";
    private int port = DatabaseType.MYSQL.getDefaultPort();
    private String database = "";
    /** Schema/catalog to scope the diff; empty means default. */
    private String schema = "";
    private String user = "";

    private boolean useSsh = false;
    private SshConfig sshConfig = new SshConfig();

    /** Optional path to a user-provided JDBC driver jar; empty uses bundled/classpath. */
    private String driverJarPath = "";

    public ConnectionConfig() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isUseSsh() {
        return useSsh;
    }

    public void setUseSsh(boolean useSsh) {
        this.useSsh = useSsh;
    }

    public SshConfig getSshConfig() {
        return sshConfig;
    }

    public void setSshConfig(SshConfig sshConfig) {
        this.sshConfig = sshConfig;
    }

    public String getDriverJarPath() {
        return driverJarPath;
    }

    public void setDriverJarPath(String driverJarPath) {
        this.driverJarPath = driverJarPath;
    }

    public ConnectionConfig copy() {
        ConnectionConfig c = new ConnectionConfig();
        c.id = id;
        c.name = name;
        c.databaseType = databaseType;
        c.host = host;
        c.port = port;
        c.database = database;
        c.schema = schema;
        c.user = user;
        c.useSsh = useSsh;
        c.sshConfig = sshConfig == null ? new SshConfig() : sshConfig.copy();
        c.driverJarPath = driverJarPath;
        return c;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionConfig)) return false;
        return Objects.equals(id, ((ConnectionConfig) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
