package com.diffdb.model;

import java.util.Objects;

/**
 * SSH tunnel configuration for one connection.
 *
 * <p>The tunnel forwards a local random port to {@code dbHost:dbPort}, where the
 * database address is expressed <b>relative to the SSH host</b> (often
 * {@code 127.0.0.1}). Secrets (password / key passphrase) are not stored here;
 * they live in PasswordSafe keyed by the owning connection id.
 */
public class SshConfig {

    private String host = "";
    private int port = 22;
    private String user = "";
    private AuthType authType = AuthType.PASSWORD;
    private String privateKeyPath = "";

    /** Database host as reachable from the SSH host. */
    private String dbHost = "127.0.0.1";
    /** Database port as reachable from the SSH host. */
    private int dbPort = 3306;

    public SshConfig() {
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public SshConfig copy() {
        SshConfig c = new SshConfig();
        c.host = host;
        c.port = port;
        c.user = user;
        c.authType = authType;
        c.privateKeyPath = privateKeyPath;
        c.dbHost = dbHost;
        c.dbPort = dbPort;
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SshConfig)) return false;
        SshConfig that = (SshConfig) o;
        return port == that.port
                && dbPort == that.dbPort
                && Objects.equals(host, that.host)
                && Objects.equals(user, that.user)
                && authType == that.authType
                && Objects.equals(privateKeyPath, that.privateKeyPath)
                && Objects.equals(dbHost, that.dbHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, user, authType, privateKeyPath, dbHost, dbPort);
    }
}
