package com.diffdb.service;

import com.diffdb.model.AuthType;
import com.diffdb.model.ConnectionConfig;
import com.diffdb.model.DatabaseType;
import com.diffdb.model.SshConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConnectionYamlService {

    private ConnectionYamlService() {
    }

    public static String exportConnections(List<ConnectionConfig> connections) {
        StringBuilder sb = new StringBuilder();
        sb.append("# DiffDB Connection Configurations\n");
        sb.append("# Export date: ").append(LocalDate.now()).append("\n");
        sb.append("\nconnections:\n");

        for (ConnectionConfig c : connections) {
            sb.append("  - name: ").append(c.getName())
                    .append("                    # Display name for this connection\n");
            sb.append("    type: ").append(c.getDatabaseType().name())
                    .append("                         # Database type: MYSQL or POSTGRESQL\n");
            sb.append("    host: ").append(c.getHost())
                    .append("                    # Database server hostname or IP\n");
            sb.append("    port: ").append(c.getPort())
                    .append("                          # Database server port (default: 3306 for MySQL, 5432 for PostgreSQL)\n");
            sb.append("    database: ").append(c.getDatabase())
                    .append("                # Database name to connect to\n");
            sb.append("    schema: ").append(c.getSchema() == null || c.getSchema().isEmpty() ? "null" : c.getSchema())
                    .append("                        # Schema name (PostgreSQL only; leave null for MySQL)\n");
            sb.append("    user: ").append(c.getUser())
                    .append("                         # Database login username\n");
            sb.append("    password: null                      # Not exported by default; set after import or edit YAML\n");
            sb.append("    driver_jar: ").append(c.getDriverJarPath() == null || c.getDriverJarPath().isEmpty() ? "null" : c.getDriverJarPath())
                    .append("                    # Optional: custom JDBC driver jar path (null = use bundled driver)\n");

            sb.append("    ssh:\n");
            if (c.isUseSsh()) {
                SshConfig ssh = c.getSshConfig();
                sb.append("      enabled: true\n");
                sb.append("      host: ").append(ssh.getHost())
                        .append("                    # SSH tunnel host\n");
                sb.append("      port: ").append(ssh.getPort())
                        .append("                          # SSH tunnel port\n");
                sb.append("      user: ").append(ssh.getUser())
                        .append("                         # SSH login username\n");
                sb.append("      auth_type: ").append(ssh.getAuthType().name())
                        .append("                    # SSH auth type: PASSWORD or KEY\n");
                sb.append("      ssh_password: null                 # Not exported by default; set after import or edit YAML\n");
                sb.append("      ssh_key_passphrase: null            # Not exported by default; set after import or edit YAML\n");
                sb.append("      private_key_path: ").append(ssh.getPrivateKeyPath())
                        .append("       # Path to private key file (for KEY auth)\n");
                sb.append("      db_host: ").append(ssh.getDbHost())
                        .append("                   # Database host as reachable from SSH host\n");
                sb.append("      db_port: ").append(ssh.getDbPort())
                        .append("                       # Database port as reachable from SSH host\n");
            } else {
                sb.append("      enabled: false\n");
            }
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static YamlImportResult importConnections(String yamlText) {
        YamlImportResult result = new YamlImportResult();
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(new StringReader(yamlText));

        if (root == null || !root.containsKey("connections")) {
            return result;
        }

        List<Map<String, Object>> entries = (List<Map<String, Object>>) root.get("connections");
        if (entries == null) {
            return result;
        }

        Set<String> seenNames = new LinkedHashSet<>();

        for (Map<String, Object> entry : entries) {
            if (entry == null) {
                continue;
            }

            String name = entry.get("name") instanceof String s ? s : null;
            String typeStr = entry.get("type") instanceof String s ? s : null;
            String host = entry.get("host") instanceof String s ? s : null;
            String database = entry.get("database") instanceof String s ? s : null;
            String user = entry.get("user") instanceof String s ? s : null;

            if (name == null || name.isEmpty()) {
                result.getWarnings().add("Skipping connection with missing required field: name");
                continue;
            }
            if (host == null || host.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'host'");
                continue;
            }
            if (database == null || database.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'database'");
                continue;
            }
            if (user == null || user.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'user'");
                continue;
            }

            if (typeStr == null) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'type'");
                continue;
            }

            DatabaseType databaseType;
            try {
                databaseType = DatabaseType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                result.getWarnings().add("Skipping connection '" + name + "': unknown database type '" + typeStr + "'");
                continue;
            }

            if (seenNames.contains(name)) {
                result.getWarnings().add("Skipping duplicate connection name: " + name);
                continue;
            }
            seenNames.add(name);

            ConnectionConfig config = new ConnectionConfig();
            config.setName(name);
            config.setDatabaseType(databaseType);
            config.setHost(host);

            int port;
            Object portObj = entry.get("port");
            if (portObj instanceof Number) {
                port = ((Number) portObj).intValue();
            } else {
                port = databaseType.getDefaultPort();
            }
            config.setPort(port);

            config.setDatabase(database);
            config.setSchema(entry.get("schema") instanceof String s ? s : "");
            config.setUser(user);
            config.setDriverJarPath(entry.get("driver_jar") instanceof String s ? s : "");

            Object pwdObj = entry.get("password");
            if (pwdObj != null) {
                String pwd = pwdObj.toString();
                if (!pwd.isEmpty() && !pwd.equals("***") && !pwd.equals("null")) {
                    result.getDbPasswords().put(name, pwd);
                }
            }

            Object sshObj = entry.get("ssh");
            if (sshObj instanceof Map<?, ?> sshMap) {
                Map<String, Object> ssh = (Map<String, Object>) sshMap;
                boolean sshEnabled = ssh.get("enabled") instanceof Boolean b ? b : false;
                config.setUseSsh(sshEnabled);

                if (sshEnabled) {
                    SshConfig sshConfig = new SshConfig();
                    sshConfig.setHost(ssh.get("host") instanceof String s ? s : "");
                    sshConfig.setPort(ssh.get("port") instanceof Number n ? n.intValue() : 22);
                    sshConfig.setUser(ssh.get("user") instanceof String s ? s : "");
                    sshConfig.setAuthType(ssh.get("auth_type") instanceof String s ? AuthType.valueOf(s) : AuthType.PASSWORD);
                    sshConfig.setPrivateKeyPath(ssh.get("private_key_path") instanceof String s ? s : "");
                    sshConfig.setDbHost(ssh.get("db_host") instanceof String s ? s : "127.0.0.1");
                    sshConfig.setDbPort(ssh.get("db_port") instanceof Number n ? n.intValue() : databaseType.getDefaultPort());
                    config.setSshConfig(sshConfig);

                    Object sshPwdObj = ssh.get("ssh_password");
                    if (sshPwdObj != null) {
                        String sp = sshPwdObj.toString();
                        if (!sp.isEmpty() && !sp.equals("***") && !sp.equals("null")) {
                            result.getSshSecrets().put(name, sp);
                        }
                    }
                    Object keyPassObj = ssh.get("ssh_key_passphrase");
                    if (keyPassObj != null) {
                        String kp = keyPassObj.toString();
                        if (!kp.isEmpty() && !kp.equals("***") && !kp.equals("null")) {
                            result.getSshSecrets().put(name, kp);
                        }
                    }
                }
            } else {
                config.setUseSsh(false);
            }

            result.getConnections().add(config);
        }

        return result;
    }
}