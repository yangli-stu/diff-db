package com.diffdb.ssh;

import com.diffdb.model.AuthType;
import com.diffdb.model.SshConfig;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Properties;

/**
 * An SSH session with local port forwarding to a remote database.
 *
 * <p>Opens {@code localhost:<random>} forwarded to {@code dbHost:dbPort} relative
 * to the SSH host. JDBC then connects to {@link #getLocalHost()}:{@link #getLocalPort()}.
 * The tunnel is transparent to the diff engine.
 */
public class SshTunnel implements AutoCloseable {

    private static final int CONNECT_TIMEOUT_MS = 15_000;

    private final Session session;
    private final int localPort;

    private SshTunnel(Session session, int localPort) {
        this.session = session;
        this.localPort = localPort;
    }

    /**
     * Opens a tunnel.
     *
     * @param config     SSH parameters (host/user/auth + remote db host/port)
     * @param secret     password (when {@link AuthType#PASSWORD}) or key passphrase
     *                   (when {@link AuthType#KEY}); may be empty
     */
    public static SshTunnel open(SshConfig config, String secret) throws JSchException {
        JSch jsch = new JSch();

        if (config.getAuthType() == AuthType.KEY
                && config.getPrivateKeyPath() != null
                && !config.getPrivateKeyPath().isBlank()) {
            if (secret != null && !secret.isEmpty()) {
                jsch.addIdentity(config.getPrivateKeyPath(), secret);
            } else {
                jsch.addIdentity(config.getPrivateKeyPath());
            }
        }

        Session session = jsch.getSession(config.getUser(), config.getHost(), config.getPort());

        if (config.getAuthType() == AuthType.PASSWORD && secret != null) {
            session.setPassword(secret);
        }

        Properties props = new Properties();
        // MVP: skip host key verification. Phase 2 wires known_hosts fingerprint check.
        props.put("StrictHostKeyChecking", "no");
        session.setConfig(props);

        session.connect(CONNECT_TIMEOUT_MS);

        try {
            // local port 0 -> JSch assigns a free port.
            int localPort = session.setPortForwardingL(0, config.getDbHost(), config.getDbPort());
            return new SshTunnel(session, localPort);
        } catch (JSchException e) {
            session.disconnect();
            throw e;
        }
    }

    public String getLocalHost() {
        return "127.0.0.1";
    }

    public int getLocalPort() {
        return localPort;
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
