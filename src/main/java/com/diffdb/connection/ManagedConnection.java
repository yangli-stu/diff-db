package com.diffdb.connection;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.ssh.SshTunnel;

import java.sql.Connection;

/**
 * A live JDBC {@link Connection} together with the optional {@link SshTunnel} that
 * backs it. Closing this releases both, in the right order.
 */
public class ManagedConnection implements AutoCloseable {

    private final Connection connection;
    private final SshTunnel tunnel; // nullable
    private final ConnectionConfig config;

    public ManagedConnection(Connection connection, SshTunnel tunnel, ConnectionConfig config) {
        this.connection = connection;
        this.tunnel = tunnel;
        this.config = config;
    }

    public Connection getConnection() {
        return connection;
    }

    public ConnectionConfig getConfig() {
        return config;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception ignored) {
            // best-effort close
        } finally {
            if (tunnel != null) {
                tunnel.close();
            }
        }
    }
}
