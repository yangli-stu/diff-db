package com.diffdb.service;

import com.diffdb.model.AuthType;
import com.diffdb.model.ConnectionConfig;
import com.diffdb.model.DatabaseType;
import com.diffdb.model.SshConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionYamlServiceTest {

    private ConnectionConfig makeMySqlConfig() {
        ConnectionConfig c = new ConnectionConfig();
        c.setName("prod-mysql");
        c.setDatabaseType(DatabaseType.MYSQL);
        c.setHost("10.0.1.100");
        c.setPort(3306);
        c.setDatabase("myapp_prod");
        c.setSchema("");
        c.setUser("admin");
        c.setUseSsh(false);
        c.setDriverJarPath("");
        return c;
    }

    private ConnectionConfig makeMySqlConfigWithSsh() {
        ConnectionConfig c = makeMySqlConfig();
        c.setName("prod-mysql-ssh");
        c.setUseSsh(true);
        SshConfig ssh = new SshConfig();
        ssh.setHost("bastion.example.com");
        ssh.setPort(22);
        ssh.setUser("sshadmin");
        ssh.setAuthType(AuthType.KEY);
        ssh.setPrivateKeyPath("/home/sshadmin/.ssh/id_rsa");
        ssh.setDbHost("127.0.0.1");
        ssh.setDbPort(3306);
        c.setSshConfig(ssh);
        return c;
    }

    @Test
    void exportRoundTripsBasicFields() {
        ConnectionConfig c = makeMySqlConfig();
        String yaml = ConnectionYamlService.exportConnections(List.of(c));

        assertTrue(yaml.contains("name: prod-mysql"));
        assertTrue(yaml.contains("type: MYSQL"));
        assertTrue(yaml.contains("host: 10.0.1.100"));
        assertTrue(yaml.contains("port: 3306"));
        assertTrue(yaml.contains("database: myapp_prod"));
        assertTrue(yaml.contains("user: admin"));
    }

    @Test
    void exportIncludesComments() {
        ConnectionConfig c = makeMySqlConfig();
        String yaml = ConnectionYamlService.exportConnections(List.of(c));

        assertTrue(yaml.contains("# Database type: MYSQL or POSTGRESQL"));
        assertTrue(yaml.contains("# Not exported by default; set after import or edit YAML"));
        assertTrue(yaml.contains("# DiffDB Connection Configurations"));
    }

    @Test
    void exportOmitsSshWhenDisabled() {
        ConnectionConfig c = makeMySqlConfig();
        c.setUseSsh(false);
        String yaml = ConnectionYamlService.exportConnections(List.of(c));

        assertTrue(yaml.contains("ssh:"));
        assertTrue(yaml.contains("enabled: false"));
    }

    @Test
    void exportIncludesSshWhenEnabled() {
        ConnectionConfig c = makeMySqlConfigWithSsh();
        String yaml = ConnectionYamlService.exportConnections(List.of(c));

        assertTrue(yaml.contains("ssh:"));
        assertTrue(yaml.contains("enabled: true"));
        assertTrue(yaml.contains("host: bastion.example.com"));
        assertTrue(yaml.contains("private_key_path: /home/sshadmin/.ssh/id_rsa"));
        assertTrue(yaml.contains("auth_type: KEY"));
    }

    @Test
    void importParsesBasicConnection() {
        String yamlText = """
                connections:
                  - name: prod-mysql
                    type: MYSQL
                    host: 10.0.1.100
                    port: 3306
                    database: myapp_prod
                    schema: ""
                    user: admin
                    driver_jar: ""
                    ssh:
                      enabled: true
                      host: bastion.example.com
                      port: 22
                      user: sshadmin
                      auth_type: KEY
                      private_key_path: /home/sshadmin/.ssh/id_rsa
                      db_host: 127.0.0.1
                      db_port: 3306
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertEquals(0, result.getWarnings().size());

        ConnectionConfig c = result.getConnections().get(0);
        assertEquals("prod-mysql", c.getName());
        assertEquals(DatabaseType.MYSQL, c.getDatabaseType());
        assertEquals("10.0.1.100", c.getHost());
        assertEquals(3306, c.getPort());
        assertEquals("myapp_prod", c.getDatabase());
        assertEquals("admin", c.getUser());
        assertTrue(c.isUseSsh());
        assertEquals("bastion.example.com", c.getSshConfig().getHost());
        assertEquals(AuthType.KEY, c.getSshConfig().getAuthType());
        assertEquals("/home/sshadmin/.ssh/id_rsa", c.getSshConfig().getPrivateKeyPath());
    }

    @Test
    void importMissingRequiredFieldProducesWarning() {
        String yamlText = """
                connections:
                  - type: MYSQL
                    host: 10.0.1.100
                    database: mydb
                    user: admin
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(0, result.getConnections().size());
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().get(0).contains("name"));
    }

    @Test
    void importUsesDefaultPort() {
        String yamlText = """
                connections:
                  - name: prod-pg
                    type: POSTGRESQL
                    host: 10.0.1.200
                    database: myapp_prod
                    user: admin
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        ConnectionConfig c = result.getConnections().get(0);
        assertEquals(5432, c.getPort());
    }

    @Test
    void importDuplicateNameProducesWarning() {
        String yamlText = """
                connections:
                  - name: prod-mysql
                    type: MYSQL
                    host: 10.0.1.100
                    database: myapp_prod
                    user: admin
                  - name: prod-mysql
                    type: MYSQL
                    host: 10.0.1.101
                    database: myapp_prod2
                    user: admin
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("duplicate")));
    }

    @Test
    void importParsesPasswords() {
        String yamlText = """
                connections:
                  - name: prod-mysql
                    type: MYSQL
                    host: 10.0.1.100
                    database: myapp_prod
                    user: admin
                    password: mysecret
                    ssh:
                      enabled: true
                      host: bastion.example.com
                      port: 22
                      user: sshadmin
                      auth_type: PASSWORD
                      ssh_password: sshsecret
                      db_host: 127.0.0.1
                      db_port: 3306
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertEquals("mysecret", result.getDbPasswords().get("prod-mysql"));
        assertEquals("sshsecret", result.getSshSecrets().get("prod-mysql"));
    }

    @Test
    void importParsesSshKeyPassphrase() {
        String yamlText = """
                connections:
                  - name: key-db
                    type: POSTGRESQL
                    host: 10.0.1.200
                    database: mydb
                    user: pguser
                    ssh:
                      enabled: true
                      host: jump.example.com
                      user: jumphost
                      auth_type: KEY
                      private_key_path: /home/jumphost/.ssh/id_rsa
                      ssh_key_passphrase: keypass123
                      db_host: 127.0.0.1
                      db_port: 5432
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertEquals("keypass123", result.getSshSecrets().get("key-db"));
    }

    @Test
    void importIgnoresPasswordPlaceholder() {
        String yamlText = """
                connections:
                  - name: prod-mysql
                    type: MYSQL
                    host: 10.0.1.100
                    database: myapp_prod
                    user: admin
                    password: "***"
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertTrue(result.getDbPasswords().isEmpty());
    }

    @Test
    void importParsesNumericPassword() {
        String yamlText = """
                connections:
                  - name: numeric-pwd
                    type: MYSQL
                    host: 10.0.1.100
                    database: myapp_prod
                    user: admin
                    password: 123456
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertEquals("123456", result.getDbPasswords().get("numeric-pwd"));
    }

    @Test
    void importParsesPasswordWithQuotes() {
        String yamlText = """
                connections:
                  - name: quoted-pwd
                    type: MYSQL
                    host: 10.0.1.100
                    database: myapp_prod
                    user: admin
                    password: "mysecret123"
                """;

        YamlImportResult result = ConnectionYamlService.importConnections(yamlText);
        assertEquals(1, result.getConnections().size());
        assertEquals("mysecret123", result.getDbPasswords().get("quoted-pwd"));
    }
}