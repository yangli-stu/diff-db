package com.diffdb.service;

import com.diffdb.model.ConnectionConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlImportResult {

    private final List<ConnectionConfig> connections = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, String> dbPasswords = new HashMap<>();
    private final Map<String, String> sshSecrets = new HashMap<>();

    public List<ConnectionConfig> getConnections() {
        return connections;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<String, String> getDbPasswords() {
        return dbPasswords;
    }

    public Map<String, String> getSshSecrets() {
        return sshSecrets;
    }
}