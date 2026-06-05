package com.diffdb.connection;

import com.diffdb.ssh.SshTunnel;

import java.sql.Connection;

/**
 * A live JDBC {@link Connection} together with the optional {@link SshTunnel} that
 * backs it. Closing this releases both, in the right order.
 */
public class ManagedConnection implements AutoCloseable {

    private final Connection connection;
    private final SshTunnel tunnel; // nullable

    public ManagedConnection(Connection connection, SshTunnel tunnel) {
        this.connection = connection;
        this.tunnel = tunnel;
    }

    public Connection getConnection() {
        return connection;
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
