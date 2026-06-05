package com.diffdb.model;

/**
 * SSH authentication method.
 */
public enum AuthType {
    PASSWORD("Password"),
    KEY("Key pair");

    private final String displayName;

    AuthType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
