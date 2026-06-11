package com.diffdb.diff;

import com.diffdb.model.ConnectionConfig;

/**
 * Compares the schema of two databases.
 *
 * <p>Interface so the engine (Liquibase today) can be swapped later for a lighter
 * implementation without touching the UI.
 */
public interface SchemaDiffService {

    /**
     * @param source   the reference connection (the desired state)
     * @param target   the connection to be brought in line with the source
     * @param secrets  resolves passwords/passphrases per connection id
     * @param listener optional progress callback; may be null
     */
    SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target, SecretResolver secrets,
                          ProgressListener listener)
            throws Exception;

    /**
     * Supplies secrets for a connection without forcing the core to depend on the
     * IDE's PasswordSafe.
     */
    interface SecretResolver {
        /** Database password for the given connection, or null. */
        String dbPassword(ConnectionConfig config);

        /** SSH password or key passphrase for the given connection, or null. */
        String sshSecret(ConnectionConfig config);
    }

    /** Receives fine-grained progress updates during diff. */
    interface ProgressListener {
        /** Called when a new step begins. */
        void onStep(String step);
    }
}
