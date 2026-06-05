package com.diffdb.service;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

/**
 * Stores and retrieves secrets (database passwords, SSH passwords / key passphrases)
 * in IntelliJ's PasswordSafe, keyed by connection id.
 */
public final class CredentialService {

    private static final String SERVICE = "DiffDB";

    private CredentialService() {
    }

    private static CredentialAttributes attrs(String connectionId, String kind) {
        String key = CredentialAttributesKt.generateServiceName(SERVICE, connectionId + ":" + kind);
        return new CredentialAttributes(key);
    }

    public static void setDbPassword(String connectionId, String password) {
        store(attrs(connectionId, "db"), password);
    }

    public static String getDbPassword(String connectionId) {
        return read(attrs(connectionId, "db"));
    }

    public static void setSshSecret(String connectionId, String secret) {
        store(attrs(connectionId, "ssh"), secret);
    }

    public static String getSshSecret(String connectionId) {
        return read(attrs(connectionId, "ssh"));
    }

    public static void clear(String connectionId) {
        PasswordSafe.getInstance().set(attrs(connectionId, "db"), null);
        PasswordSafe.getInstance().set(attrs(connectionId, "ssh"), null);
    }

    private static void store(CredentialAttributes attributes, String value) {
        if (value == null || value.isEmpty()) {
            PasswordSafe.getInstance().set(attributes, null);
        } else {
            PasswordSafe.getInstance().set(attributes, new Credentials(null, value));
        }
    }

    private static String read(CredentialAttributes attributes) {
        return PasswordSafe.getInstance().getPassword(attributes);
    }
}
