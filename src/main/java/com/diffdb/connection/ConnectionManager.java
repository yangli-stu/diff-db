package com.diffdb.connection;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.ssh.SshTunnel;

/**
 * Turns a {@link ConnectionConfig} into a live {@link ManagedConnection},
 * transparently opening an SSH tunnel first when enabled.
 */
public final class ConnectionManager {

    private ConnectionManager() {
    }

    /**
     * Opens a connection.
     *
     * @param config    the connection definition
     * @param dbPassword database password (from PasswordSafe), may be null
     * @param sshSecret  SSH password or key passphrase (from PasswordSafe), may be null
     */
    public static ManagedConnection open(ConnectionConfig config, String dbPassword, String sshSecret)
            throws Exception {
        SshTunnel tunnel = null;
        String effectiveHost = config.getHost();
        int effectivePort = config.getPort();

        try {
            if (config.isUseSsh()) {
                tunnel = SshTunnel.open(config.getSshConfig(), sshSecret);
                effectiveHost = tunnel.getLocalHost();
                effectivePort = tunnel.getLocalPort();
            }

            String url = JdbcUrlBuilder.build(
                    config.getDatabaseType(), effectiveHost, effectivePort, config.getDatabase());

            java.sql.Connection conn = DriverProvider.connect(
                    config.getDatabaseType(),
                    config.getDriverJarPath(),
                    url,
                    config.getUser(),
                    dbPassword);

            return new ManagedConnection(conn, tunnel);
        } catch (Exception e) {
            if (tunnel != null) {
                tunnel.close();
            }
            throw e;
        }
    }

    /**
     * Opens, validates (1s), and closes a connection. Returns null on success or an
     * error message on failure. Convenience for the "Test connection" button.
     */
    public static String testConnection(ConnectionConfig config, String dbPassword, String sshSecret) {
        try (ManagedConnection mc = open(config, dbPassword, sshSecret)) {
            if (mc.getConnection().isValid(5)) {
                return null;
            }
            return "Connection is not valid.";
        } catch (Exception e) {
            return e.getMessage() == null ? e.toString() : e.getMessage();
        }
    }
}
