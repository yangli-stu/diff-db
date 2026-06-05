# DB Management Module & YAML Import/Export — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a standalone DB management panel at the top of the DiffDB Tool Window with CRUD for connections, inline table expansion, and YAML import/export.

**Architecture:** New `DbManagerPanel` sits atop `DiffDbPanel` with a scrollable list of `DbConnectionCard` components. A `TableBrowserService` fetches table metadata via JDBC. A `ConnectionYamlService` handles YAML serialization/deserialization with inline comments. The existing `DiffDbPanel` toolbar buttons (New/Edit/Delete) move into `DbManagerPanel`, and Source/Target combos get their model from the same storage.

**Tech Stack:** Java 17, IntelliJ Platform SDK (Swing UI), SnakeYAML (transitive via Liquibase), JUnit 5 + H2 for tests.

---

### Task 1: TableInfo model

**Files:**
- Create: `src/main/java/com/diffdb/model/TableInfo.java`
- Test: `src/test/java/com/diffdb/model/TableInfoTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.diffdb.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TableInfoTest {

    @Test
    void constructorAndGetters() {
        TableInfo info = new TableInfo("users", 12);
        assertEquals("users", info.getTableName());
        assertEquals(12, info.getColumnCount());
    }

    @Test
    void equalsAndHashCode() {
        TableInfo a = new TableInfo("orders", 8);
        TableInfo b = new TableInfo("orders", 8);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringFormat() {
        TableInfo info = new TableInfo("products", 6);
        assertEquals("products (6 columns)", info.toString());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.diffdb.model.TableInfoTest" 2>&1 | tail -20`
Expected: FAIL — `TableInfo` class not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.diffdb.model;

import java.util.Objects;

public class TableInfo {

    private final String tableName;
    private final int columnCount;

    public TableInfo(String tableName, int columnCount) {
        this.tableName = tableName;
        this.columnCount = columnCount;
    }

    public String getTableName() {
        return tableName;
    }

    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableInfo)) return false;
        TableInfo that = (TableInfo) o;
        return columnCount == that.columnCount && Objects.equals(tableName, that.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnCount);
    }

    @Override
    public String toString() {
        return tableName + " (" + columnCount + " columns)";
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.diffdb.model.TableInfoTest" 2>&1 | tail -5`
Expected: GREEN

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/diffdb/model/TableInfo.java src/test/java/com/diffdb/model/TableInfoTest.java
git commit -m "feat: add TableInfo model for table metadata display"
```

---

### Task 2: TableBrowserService

**Files:**
- Create: `src/main/java/com/diffdb/service/TableBrowserService.java`
- Test: `src/test/java/com/diffdb/service/TableBrowserServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.diffdb.service;

import com.diffdb.model.TableInfo;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TableBrowserServiceTest {

    @Test
    void listTablesFromH2Connection() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "sa", "")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))");
                stmt.execute("CREATE TABLE orders (id INT PRIMARY KEY, amount DECIMAL(10,2))");
            }
            List<TableInfo> tables = TableBrowserService.listTables(conn, null);
            assertTrue(tables.size() >= 2);
            boolean foundUsers = tables.stream().anyMatch(t -> t.getTableName().equalsIgnoreCase("users"));
            boolean foundOrders = tables.stream().anyMatch(t -> t.getTableName().equalsIgnoreCase("orders"));
            assertTrue(foundUsers, "Should find 'users' table");
            assertTrue(foundOrders, "Should find 'orders' table");
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.diffdb.service.TableBrowserServiceTest" 2>&1 | tail -10`
Expected: FAIL — `TableBrowserService` class not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.diffdb.service;

import com.diffdb.model.TableInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class TableBrowserService {

    private TableBrowserService() {
    }

    public static List<TableInfo> listTables(Connection connection, String schema) {
        List<TableInfo> tables = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            String[] types = {"TABLE"};
            try (ResultSet rs = meta.getTables(null, schema, "%", types)) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    int columnCount = countColumns(meta, schema, tableName);
                    tables.add(new TableInfo(tableName, columnCount));
                }
            }
            tables.sort((a, b) -> a.getTableName().compareToIgnoreCase(b.getTableName()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to list tables: " + e.getMessage(), e);
        }
        return tables;
    }

    private static int countColumns(DatabaseMetaData meta, String schema, String tableName) {
        try (ResultSet cols = meta.getColumns(null, schema, tableName, "%")) {
            int count = 0;
            while (cols.next()) {
                count++;
            }
            return count;
        } catch (Exception e) {
            return -1;
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.diffdb.service.TableBrowserServiceTest" 2>&1 | tail -5`
Expected: GREEN

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/diffdb/service/TableBrowserService.java src/test/java/com/diffdb/service/TableBrowserServiceTest.java
git commit -m "feat: add TableBrowserService to list tables via JDBC metadata"
```

---

### Task 3: ConnectionYamlService

**Files:**
- Create: `src/main/java/com/diffdb/service/ConnectionYamlService.java`
- Create: `src/main/java/com/diffdb/service/YamlImportResult.java`
- Test: `src/test/java/com/diffdb/service/ConnectionYamlServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.diffdb.service;

import com.diffdb.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionYamlServiceTest {

    @Test
    void exportRoundTripsBasicFields() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("prod-mysql");
        config.setDatabaseType(DatabaseType.MYSQL);
        config.setHost("10.0.1.100");
        config.setPort(3306);
        config.setDatabase("myapp_prod");
        config.setSchema("");
        config.setUser("admin");
        config.setDriverJarPath("");

        String yaml = ConnectionYamlService.exportConnections(List.of(config));
        assertTrue(yaml.contains("name: prod-mysql"));
        assertTrue(yaml.contains("type: MYSQL"));
        assertTrue(yaml.contains("host: 10.0.1.100"));
        assertTrue(yaml.contains("port: 3306"));
        assertTrue(yaml.contains("database: myapp_prod"));
        assertTrue(yaml.contains("user: admin"));
        assertTrue(yaml.contains("# Display name"));
    }

    @Test
    void exportIncludesComments() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("test-db");
        config.setDatabaseType(DatabaseType.POSTGRESQL);
        config.setHost("localhost");
        config.setDatabase("testdb");
        config.setUser("dev");
        config.setPort(5432);
        config.setSchema("public");

        String yaml = ConnectionYamlService.exportConnections(List.of(config));
        assertTrue(yaml.contains("# Database type: MYSQL or POSTGRESQL"));
        assertTrue(yaml.contains("# Not exported; stored in IntelliJ PasswordSafe"));
    }

    @Test
    void exportOmitsSshWhenDisabled() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("no-ssh-db");
        config.setDatabaseType(DatabaseType.MYSQL);
        config.setHost("localhost");
        config.setDatabase("testdb");
        config.setUser("root");
        config.setUseSsh(false);

        String yaml = ConnectionYamlService.exportConnections(List.of(config));
        assertFalse(yaml.contains("ssh:"));
    }

    @Test
    void exportIncludesSshWhenEnabled() {
        ConnectionConfig config = new ConnectionConfig();
        config.setName("ssh-db");
        config.setDatabaseType(DatabaseType.MYSQL);
        config.setHost("localhost");
        config.setDatabase("testdb");
        config.setUser("root");
        config.setUseSsh(true);
        SshConfig ssh = config.getSshConfig();
        ssh.setHost("bastion.example.com");
        ssh.setPort(22);
        ssh.setUser("deploy");
        ssh.setAuthType(AuthType.KEY);
        ssh.setPrivateKeyPath("/home/user/.ssh/id_rsa");
        ssh.setDbHost("127.0.0.1");
        ssh.setDbPort(3306);

        String yaml = ConnectionYamlService.exportConnections(List.of(config));
        assertTrue(yaml.contains("ssh:"));
        assertTrue(yaml.contains("host: bastion.example.com"));
        assertTrue(yaml.contains("auth_type: KEY"));
        assertTrue(yaml.contains("private_key_path: /home/user/.ssh/id_rsa"));
    }

    @Test
    void importParsesBasicConnection() {
        String yaml = """
            connections:
              - name: dev-pg
                type: POSTGRESQL
                host: 192.168.1.50
                port: 5432
                database: myapp_dev
                schema: public
                user: dev_user
                driver_jar: null
                ssh:
                  enabled: true
                  host: bastion.example.com
                  port: 22
                  user: deploy
                  auth_type: KEY
                  private_key_path: /home/user/.ssh/id_rsa
                  db_host: 127.0.0.1
                  db_port: 5432
            """;

        YamlImportResult result = ConnectionYamlService.importConnections(yaml);
        assertTrue(result.getWarnings().isEmpty());
        assertEquals(1, result.getConnections().size());
        ConnectionConfig c = result.getConnections().get(0);
        assertEquals("dev-pg", c.getName());
        assertEquals(DatabaseType.POSTGRESQL, c.getDatabaseType());
        assertEquals("192.168.1.50", c.getHost());
        assertEquals(5432, c.getPort());
        assertEquals("myapp_dev", c.getDatabase());
        assertEquals("public", c.getSchema());
        assertEquals("dev_user", c.getUser());
        assertTrue(c.isUseSsh());
        assertEquals("bastion.example.com", c.getSshConfig().getHost());
        assertEquals(AuthType.KEY, c.getSshConfig().getAuthType());
        assertNotEquals("", c.getId());
    }

    @Test
    void importMissingRequiredFieldProducesWarning() {
        String yaml = """
            connections:
              - host: localhost
                database: testdb
                user: root
            """;

        YamlImportResult result = ConnectionYamlService.importConnections(yaml);
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("name")));
    }

    @Test
    void importUsesDefaultPort() {
        String yaml = """
            connections:
              - name: test-mysql
                type: MYSQL
                host: localhost
                database: testdb
                user: root
            """;

        YamlImportResult result = ConnectionYamlService.importConnections(yaml);
        ConnectionConfig c = result.getConnections().get(0);
        assertEquals(3306, c.getPort());
    }

    @Test
    void importDuplicateNameProducesWarning() {
        String yaml = """
            connections:
              - name: mydb
                type: MYSQL
                host: localhost
                database: testdb
                user: root
              - name: mydb
                type: MYSQL
                host: 192.168.1.1
                database: otherdb
                user: admin
            """;

        YamlImportResult result = ConnectionYamlService.importConnections(yaml);
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("Duplicate")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.diffdb.service.ConnectionYamlServiceTest" 2>&1 | tail -10`
Expected: FAIL — classes not found

- [ ] **Step 3: Write YamlImportResult**

```java
package com.diffdb.service;

import com.diffdb.model.ConnectionConfig;
import java.util.ArrayList;
import java.util.List;

public class YamlImportResult {

    private final List<ConnectionConfig> connections = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public List<ConnectionConfig> getConnections() {
        return connections;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
```

- [ ] **Step 4: Write ConnectionYamlService**

```java
package com.diffdb.service;

import com.diffdb.model.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

public final class ConnectionYamlService {

    private ConnectionYamlService() {
    }

    public static String exportConnections(List<ConnectionConfig> connections) {
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        opts.setPrettyFlow(true);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("# DiffDB Connection Configurations");
        pw.println("# Export date: " + LocalDate.now());
        pw.println();
        pw.println("connections:");

        for (ConnectionConfig c : connections) {
            pw.println("  - name: " + c.getName() + "                    # Display name for this connection");
            pw.println("    type: " + c.getDatabaseType().name() + "                         # Database type: MYSQL or POSTGRESQL");
            pw.println("    host: " + c.getHost() + "                    # Database server hostname or IP");
            pw.println("    port: " + c.getPort() + "                          # Database server port (default: 3306 for MySQL, 5432 for PostgreSQL)");
            pw.println("    database: " + c.getDatabase() + "                # Database name to connect to");
            String schemaVal = c.getSchema().isEmpty() ? "null" : c.getSchema();
            pw.println("    schema: " + schemaVal + "                        # Schema name (PostgreSQL only; leave null for MySQL)");
            pw.println("    user: " + c.getUser() + "                         # Database login username");
            pw.println("    # password: ***                     # Not exported; stored in IntelliJ PasswordSafe");
            String driverJar = c.getDriverJarPath().isEmpty() ? "null" : c.getDriverJarPath();
            pw.println("    driver_jar: " + driverJar + "            # Optional: custom JDBC driver jar path (null = use bundled driver)");

            if (c.isUseSsh()) {
                SshConfig ssh = c.getSshConfig();
                pw.println("    ssh:                                # SSH tunnel config (omit section if no SSH)");
                pw.println("      enabled: true                     # Whether to use SSH tunnel");
                pw.println("      host: " + ssh.getHost() + "               # SSH server hostname");
                pw.println("      port: " + ssh.getPort() + "                          # SSH server port");
                pw.println("      user: " + ssh.getUser() + "                # SSH login username");
                pw.println("      auth_type: " + ssh.getAuthType().name() + "                    # SSH auth: PASSWORD or KEY");
                pw.println("      # ssh_password: ***               # Not exported");
                String keyPath = ssh.getPrivateKeyPath().isEmpty() ? "null" : ssh.getPrivateKeyPath();
                pw.println("      private_key_path: " + keyPath + "    # Path to SSH private key (for KEY auth)");
                pw.println("      # ssh_key_passphrase: ***         # Not exported");
                pw.println("      db_host: " + ssh.getDbHost() + "              # Database host as seen from SSH server");
                pw.println("      db_port: " + ssh.getDbPort() + "              # Database port as seen from SSH server");
            } else {
                pw.println("    ssh:                                # SSH tunnel config (omit section if no SSH)");
                pw.println("      enabled: false");
            }
        }
        pw.flush();
        return sw.toString();
    }

    @SuppressWarnings("unchecked")
    public static YamlImportResult importConnections(String yamlText) {
        YamlImportResult result = new YamlImportResult();
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(new StringReader(yamlText));

        if (root == null || !root.containsKey("connections")) {
            result.getWarnings().add("No 'connections' key found in YAML");
            return result;
        }

        List<Map<String, Object>> connList = (List<Map<String, Object>>) root.get("connections");
        if (connList == null) {
            return result;
        }

        Set<String> seenNames = new HashSet<>();

        for (Map<String, Object> item : connList) {
            String name = asString(item.get("name"));
            if (name == null || name.isEmpty()) {
                result.getWarnings().add("Skipping connection: missing required field 'name'");
                continue;
            }

            if (seenNames.contains(name)) {
                result.getWarnings().add("Duplicate connection name in YAML: " + name + " (skipped)");
                continue;
            }
            seenNames.add(name);

            String typeStr = asString(item.get("type"));
            if (typeStr == null) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'type'");
                continue;
            }

            DatabaseType dbType;
            try {
                dbType = DatabaseType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.getWarnings().add("Skipping connection '" + name + "': unknown type '" + typeStr + "'");
                continue;
            }

            String host = asString(item.get("host"));
            if (host == null || host.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'host'");
                continue;
            }

            String database = asString(item.get("database"));
            if (database == null || database.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'database'");
                continue;
            }

            String user = asString(item.get("user"));
            if (user == null || user.isEmpty()) {
                result.getWarnings().add("Skipping connection '" + name + "': missing required field 'user'");
                continue;
            }

            ConnectionConfig config = new ConnectionConfig();
            config.setName(name);
            config.setDatabaseType(dbType);
            config.setHost(host);
            config.setPort(item.containsKey("port") ? ((Number) item.get("port")).intValue() : dbType.getDefaultPort());
            config.setDatabase(database);
            config.setUser(user);
            config.setSchema(nullableString(item.get("schema")));
            config.setDriverJarPath(nullableString(item.get("driver_jar")));

            Map<String, Object> sshMap = (Map<String, Object>) item.get("ssh");
            if (sshMap != null) {
                boolean sshEnabled = sshMap.containsKey("enabled") && Boolean.TRUE.equals(sshMap.get("enabled"));
                config.setUseSsh(sshEnabled);
                if (sshEnabled) {
                    SshConfig ssh = config.getSshConfig();
                    ssh.setHost(asStringOrDefault(sshMap.get("host"), ""));
                    ssh.setPort(sshMap.containsKey("port") ? ((Number) sshMap.get("port")).intValue() : 22);
                    ssh.setUser(asStringOrDefault(sshMap.get("user"), ""));
                    String authTypeStr = asStringOrDefault(sshMap.get("auth_type"), "PASSWORD");
                    try {
                        ssh.setAuthType(AuthType.valueOf(authTypeStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        ssh.setAuthType(AuthType.PASSWORD);
                    }
                    ssh.setPrivateKeyPath(nullableString(sshMap.get("private_key_path")));
                    ssh.setDbHost(asStringOrDefault(sshMap.get("db_host"), "127.0.0.1"));
                    ssh.setDbPort(sshMap.containsKey("db_port") ? ((Number) sshMap.get("db_port")).intValue() : dbType.getDefaultPort());
                }
            } else {
                config.setUseSsh(false);
            }

            result.getConnections().add(config);
        }

        return result;
    }

    private static String asString(Object obj) {
        if (obj == null) return null;
        return obj.toString();
    }

    private static String asStringOrDefault(Object obj, String defaultValue) {
        if (obj == null) return defaultValue;
        return obj.toString();
    }

    private static String nullableString(Object obj) {
        if (obj == null) return "";
        String val = obj.toString();
        return "null".equals(val) ? "" : val;
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew test --tests "com.diffdb.service.ConnectionYamlServiceTest" 2>&1 | tail -10`
Expected: GREEN

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/diffdb/service/ConnectionYamlService.java src/main/java/com/diffdb/service/YamlImportResult.java src/test/java/com/diffdb/service/ConnectionYamlServiceTest.java
git commit -m "feat: add ConnectionYamlService for YAML import/export with comments"
```

---

### Task 4: ConnectionStorageService enhancements

**Files:**
- Modify: `src/main/java/com/diffdb/service/ConnectionStorageService.java`
- Modify: `src/test/java/com/diffdb/service/ConnectionStorageServiceTest.java` (create)

- [ ] **Step 1: Write the failing test**

```java
package com.diffdb.service;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.model.DatabaseType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionStorageServiceTest {

    @Test
    void findByNameReturnsMatching() {
        ConnectionStorageService service = new ConnectionStorageService();
        ConnectionConfig c = new ConnectionConfig();
        c.setName("my-test-db");
        c.setDatabaseType(DatabaseType.MYSQL);
        service.save(c);
        ConnectionConfig found = service.findByName("my-test-db");
        assertNotNull(found);
        assertEquals("my-test-db", found.getName());
    }

    @Test
    void findByNameReturnsNullIfNotFound() {
        ConnectionStorageService service = new ConnectionStorageService();
        assertNull(service.findByName("nonexistent"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.diffdb.service.ConnectionStorageServiceTest" 2>&1 | tail -10`
Expected: FAIL — `findByName` method not found

- [ ] **Step 3: Add `findByName` to ConnectionStorageService**

Add the following method to `ConnectionStorageService.java` after the `findById` method:

```java
@Nullable
public ConnectionConfig findByName(String name) {
    return state.connections.stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElse(null);
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew test --tests "com.diffdb.service.ConnectionStorageServiceTest" 2>&1 | tail -5`
Expected: GREEN

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/diffdb/service/ConnectionStorageService.java src/test/java/com/diffdb/service/ConnectionStorageServiceTest.java
git commit -m "feat: add findByName to ConnectionStorageService"
```

---

### Task 5: DbConnectionCard UI component

**Files:**
- Create: `src/main/java/com/diffdb/ui/DbConnectionCard.java`

This is a UI-only component, no automated test. Manual verification via `./gradlew runIde`.

- [ ] **Step 1: Write DbConnectionCard**

```java
package com.diffdb.ui;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.model.TableInfo;
import com.diffdb.service.CredentialService;
import com.diffdb.connection.ConnectionManager;
import com.diffdb.service.TableBrowserService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class DbConnectionCard extends JPanel {

    private static final Logger LOG = Logger.getInstance(DbConnectionCard.class);

    private final ConnectionConfig config;
    private final Project project;
    private final Runnable onRefresh;

    private final JLabel expandIcon = new JLabel("▶");
    private final JLabel nameLabel = new JLabel();
    private final JLabel infoLabel = new JLabel();
    private final JPanel tablePanel = new JPanel();

    private boolean expanded = false;
    private List<TableInfo> cachedTables = null;

    public DbConnectionCard(ConnectionConfig config, Project project, Runnable onRefresh) {
        this.config = config;
        this.project = project;
        this.onRefresh = onRefresh;
        initComponents();
    }

    public ConnectionConfig getConfig() {
        return config;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new LineBorder(new Color(70, 70, 70), 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        left.setOpaque(false);

        expandIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        expandIcon.setToolTipText("Click to expand/collapse tables");
        expandIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleExpand();
            }
        });
        left.add(expandIcon);

        nameLabel.setText(config.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        left.add(nameLabel);

        String typeStr = config.getDatabaseType().getDisplayName();
        String hostStr = config.getHost();
        infoLabel.setText(typeStr + " · " + hostStr + ":" + config.getPort());
        infoLabel.setForeground(new Color(140, 140, 140));
        left.add(infoLabel);

        header.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);
        actions.add(makeActionButton("🔌", "Test Connection", e -> testConnection()));
        actions.add(makeActionButton("✏️", "Edit", e -> editConnection()));
        actions.add(makeActionButton("📋", "Duplicate", e -> duplicateConnection()));
        actions.add(makeActionButton("🗑️", "Delete", e -> deleteConnection()));
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setVisible(false);
        add(tablePanel, BorderLayout.CENTER);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton makeActionButton(String text, String tooltip, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setMargin(new Insets(0, 4, 0, 4));
        btn.setFocusable(false);
        btn.addActionListener(listener);
        return btn;
    }

    private void toggleExpand() {
        expanded = !expanded;
        if (expanded) {
            expandIcon.setText("▼");
            loadTables();
        } else {
            expandIcon.setText("▶");
            tablePanel.setVisible(false);
        }
    }

    private void loadTables() {
        if (cachedTables != null) {
            showTables(cachedTables);
            return;
        }

        tablePanel.removeAll();
        JBLabel loadingLabel = new JBLabel("Loading tables...");
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 4));
        tablePanel.add(loadingLabel);
        tablePanel.setVisible(true);

        new Task.Backgroundable(project, "Loading tables for " + config.getName(), true) {
            private List<TableInfo> tables;
            private String errorMsg;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    String dbPwd = CredentialService.getDbPassword(config.getId());
                    String sshSecret = CredentialService.getSshSecret(config.getId());
                    try (var mc = ConnectionManager.open(config, dbPwd, sshSecret)) {
                        tables = TableBrowserService.listTables(mc.getConnection(), config.getSchema().isEmpty() ? null : config.getSchema());
                    }
                } catch (Exception e) {
                    errorMsg = e.getMessage() == null ? e.toString() : e.getMessage();
                    LOG.error("Failed to load tables for " + config.getName(), e);
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (errorMsg != null) {
                        tablePanel.removeAll();
                        JBLabel errLabel = new JBLabel("Error: " + errorMsg);
                        errLabel.setForeground(new Color(239, 83, 80));
                        errLabel.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 4));
                        tablePanel.add(errLabel);
                        tablePanel.setVisible(true);
                    } else {
                        cachedTables = tables;
                        showTables(tables);
                    }
                });
            }
        }.queue();
    }

    private void showTables(List<TableInfo> tables) {
        tablePanel.removeAll();
        for (TableInfo t : tables) {
            JLabel label = new JLabel("📋 " + t.getTableName() + " — " + t.getColumnCount() + " columns");
            label.setForeground(new Color(79, 195, 247));
            label.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 4));
            tablePanel.add(label);
        }
        JBLabel footerLabel = new JBLabel(tables.size() + " tables total");
        footerLabel.setForeground(new Color(140, 140, 140));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(2, 20, 4, 4));
        tablePanel.add(footerLabel);
        tablePanel.setVisible(true);
    }

    private void testConnection() {
        String dbPwd = CredentialService.getDbPassword(config.getId());
        String sshSecret = CredentialService.getSshSecret(config.getId());
        String error = ConnectionManager.testConnection(config, dbPwd, sshSecret);
        if (error == null) {
            Messages.showInfoMessage(project, "Connection successful.", "DiffDB");
        } else {
            Messages.showErrorDialog(project, "Connection failed:\n" + error, "DiffDB");
        }
    }

    private void editConnection() {
        ConnectionConfig copy = config.copy();
        ConnectionDialog dialog = new ConnectionDialog(project, copy);
        if (dialog.showAndGet()) {
            com.diffdb.service.ConnectionStorageService.getInstance().save(copy);
            onRefresh.run();
        }
    }

    private void duplicateConnection() {
        ConnectionConfig dup = config.copy();
        dup.setId(java.util.UUID.randomUUID().toString());
        dup.setName(config.getName() + " (copy)");
        ConnectionDialog dialog = new ConnectionDialog(project, dup);
        if (dialog.showAndGet()) {
            com.diffdb.service.ConnectionStorageService.getInstance().save(dup);
            onRefresh.run();
        }
    }

    private void deleteConnection() {
        int answer = Messages.showYesNoDialog(project,
                "Remove connection \"" + config.getName() + "\"?\nThis cannot be undone.",
                "DiffDB", null);
        if (answer == Messages.YES) {
            com.diffdb.service.ConnectionStorageService.getInstance().delete(config.getId());
            CredentialService.clear(config.getId());
            onRefresh.run();
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/diffdb/ui/DbConnectionCard.java
git commit -m "feat: add DbConnectionCard UI component with expand/collapse and CRUD"
```

---

### Task 6: DbManagerPanel UI component

**Files:**
- Create: `src/main/java/com/diffdb/ui/DbManagerPanel.java`

- [ ] **Step 1: Write DbManagerPanel**

```java
package com.diffdb.ui;

import com.diffdb.model.ConnectionConfig;
import com.diffdb.service.ConnectionStorageService;
import com.diffdb.service.ConnectionYamlService;
import com.diffdb.service.YamlImportResult;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DbManagerPanel extends JPanel {

    private final Project project;
    private final Runnable onConnectionsChanged;

    private final JPanel cardsPanel = new JPanel();
    private final JBScrollPane scrollPane;

    public DbManagerPanel(Project project, Runnable onConnectionsChanged) {
        this.project = project;
        this.onConnectionsChanged = onConnectionsChanged;
        setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

        JLabel titleLabel = new JLabel("🗄 Databases");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        toolbar.add(titleLabel);
        toolbar.add(Box.createHorizontalStrut(8));

        JButton addButton = new JButton("+ Add");
        addButton.addActionListener(e -> onAdd());
        toolbar.add(addButton);

        JButton importButton = new JButton("⬇ Import");
        importButton.addActionListener(e -> onImport());
        toolbar.add(importButton);

        JButton exportButton = new JButton("⬆ Export");
        exportButton.addActionListener(e -> onExport());
        toolbar.add(exportButton);

        add(toolbar, BorderLayout.NORTH);

        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JBScrollPane(cardsPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        refreshCards();
    }

    public void refreshCards() {
        cardsPanel.removeAll();
        List<ConnectionConfig> connections = ConnectionStorageService.getInstance().getConnections();
        for (ConnectionConfig config : connections) {
            DbConnectionCard card = new DbConnectionCard(config, project, () -> {
                refreshCards();
                onConnectionsChanged.run();
            });
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
            cardsPanel.add(card);
            cardsPanel.add(Box.createVerticalStrut(4));
        }
        if (connections.isEmpty()) {
            JLabel emptyLabel = new JLabel("No database connections. Click \"+ Add\" to create one.");
            emptyLabel.setForeground(new Color(140, 140, 140));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            cardsPanel.add(emptyLabel);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void onAdd() {
        ConnectionConfig config = new ConnectionConfig();
        ConnectionDialog dialog = new ConnectionDialog(project, config);
        if (dialog.showAndGet()) {
            ConnectionStorageService.getInstance().save(config);
            refreshCards();
            onConnectionsChanged.run();
        }
    }

    private void onImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files (*.yaml, *.yml)", "yaml", "yml"));
        chooser.setDialogTitle("Import Connections from YAML");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            String yamlText = Files.readString(chooser.getSelectedFile().toPath());
            YamlImportResult importResult = ConnectionYamlService.importConnections(yamlText);

            if (!importResult.getWarnings().isEmpty()) {
                StringBuilder sb = new StringBuilder("Import warnings:\n\n");
                for (String w : importResult.getWarnings()) {
                    sb.append("• ").append(w).append("\n");
                }
                sb.append("\nProceed with importing ")
                        .append(importResult.getConnections().size()).append(" connection(s)?");
                int answer = Messages.showYesNoDialog(project, sb.toString(), "DiffDB Import", null);
                if (answer != Messages.YES) return;
            }

            for (ConnectionConfig config : importResult.getConnections()) {
                ConnectionConfig existing = ConnectionStorageService.getInstance().findByName(config.getName());
                if (existing != null) {
                    config.setName(config.getName() + " (imported)");
                }
                ConnectionStorageService.getInstance().save(config);
            }

            Messages.showInfoMessage(project,
                    "Imported " + importResult.getConnections().size()
                            + " connection(s).\nPlease edit each connection to set passwords.",
                    "DiffDB Import");
            refreshCards();
            onConnectionsChanged.run();
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to read file:\n" + e.getMessage(), "DiffDB Import");
        }
    }

    private void onExport() {
        List<ConnectionConfig> connections = ConnectionStorageService.getInstance().getConnections();
        if (connections.isEmpty()) {
            Messages.showInfoMessage(project, "No connections to export.", "DiffDB Export");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("YAML files (*.yaml, *.yml)", "yaml", "yml"));
        chooser.setDialogTitle("Export Connections to YAML");
        chooser.setSelectedFile(new java.io.File("diffdb-connections.yaml"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            String yamlText = ConnectionYamlService.exportConnections(connections);
            Files.writeString(chooser.getSelectedFile().toPath(), yamlText);
            Messages.showInfoMessage(project,
                    "Exported " + connections.size() + " connection(s).\nPasswords are not included — they are stored in IntelliJ PasswordSafe.",
                    "DiffDB Export");
        } catch (IOException e) {
            Messages.showErrorDialog(project, "Failed to write file:\n" + e.getMessage(), "DiffDB Export");
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/diffdb/ui/DbManagerPanel.java
git commit -m "feat: add DbManagerPanel with CRUD toolbar and YAML import/export"
```

---

### Task 7: Integrate DbManagerPanel into DiffDbPanel

**Files:**
- Modify: `src/main/java/com/diffdb/ui/DiffDbPanel.java`

- [ ] **Step 1: Refactor DiffDbPanel to add DbManagerPanel at top**

The new `DiffDbPanel` will:
1. Add `DbManagerPanel` as the top component (`BorderLayout.NORTH`)
2. Keep Source/Target combos, Compare/Generate SQL buttons, and Include DROPs checkbox in a middle toolbar
3. Remove the New/Edit/Delete buttons from the combo toolbar (they're now in `DbManagerPanel`)
4. Keep the `JBSplitter` with diff tree and SQL preview in the center

Replace the entire `DiffDbPanel.java` with:

```java
package com.diffdb.ui;

import com.diffdb.diff.LiquibaseSchemaDiffService;
import com.diffdb.diff.SchemaDiffResult;
import com.diffdb.diff.SchemaDiffService;
import com.diffdb.migration.LiquibaseMigrationSqlGenerator;
import com.diffdb.migration.MigrationOptions;
import com.diffdb.migration.MigrationSqlGenerator;
import com.diffdb.model.ConnectionConfig;
import com.diffdb.service.ConnectionStorageService;
import com.diffdb.service.CredentialService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.concurrent.atomic.AtomicReference;

public class DiffDbPanel extends JPanel {

    private static final Logger LOG = Logger.getInstance(DiffDbPanel.class);

    private final Project project;

    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> sourceCombo =
            new com.intellij.openapi.ui.ComboBox<>();
    private final com.intellij.openapi.ui.ComboBox<ConnectionConfig> targetCombo =
            new com.intellij.openapi.ui.ComboBox<>();
    private final JBCheckBox includeDropsCheck = new JBCheckBox("Include DROP statements", false);

    private final DiffTreePanel treePanel = new DiffTreePanel();
    private final MigrationPreviewPanel previewPanel;

    private final SchemaDiffService diffService = new LiquibaseSchemaDiffService();
    private final MigrationSqlGenerator sqlGenerator = new LiquibaseMigrationSqlGenerator();

    private final AtomicReference<SchemaDiffResult> lastResult = new AtomicReference<>();

    private final DbManagerPanel dbManagerPanel;

    public DiffDbPanel(Project project) {
        super(new BorderLayout());
        this.project = project;
        this.previewPanel = new MigrationPreviewPanel(project);

        dbManagerPanel = new DbManagerPanel(project, this::reloadConnections);
        add(dbManagerPanel, BorderLayout.NORTH);

        JBSplitter splitter = new JBSplitter(true, 0.55f);
        splitter.setFirstComponent(treePanel);
        splitter.setSecondComponent(previewPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buildDiffToolbar(), BorderLayout.NORTH);
        centerPanel.add(splitter, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        reloadConnections();
    }

    private JComponent buildDiffToolbar() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JBLabel("Source:"));
        row.add(sourceCombo);
        row.add(new JBLabel("Target:"));
        row.add(targetCombo);

        JButton diffButton = new JButton("Compare");
        JButton sqlButton = new JButton("Generate Migration SQL");
        diffButton.addActionListener(e -> onCompare());
        sqlButton.addActionListener(e -> onGenerateSql());
        row.add(diffButton);
        row.add(sqlButton);
        row.add(includeDropsCheck);

        return row;
    }

    private void reloadConnections() {
        ConnectionConfig prevSource = (ConnectionConfig) sourceCombo.getSelectedItem();
        ConnectionConfig prevTarget = (ConnectionConfig) targetCombo.getSelectedItem();
        sourceCombo.removeAllItems();
        targetCombo.removeAllItems();
        for (ConnectionConfig c : ConnectionStorageService.getInstance().getConnections()) {
            sourceCombo.addItem(c);
            targetCombo.addItem(c);
        }
        if (prevSource != null) sourceCombo.setSelectedItem(prevSource);
        if (prevTarget != null) targetCombo.setSelectedItem(prevTarget);
    }

    private void onCompare() {
        ConnectionConfig source = (ConnectionConfig) sourceCombo.getSelectedItem();
        ConnectionConfig target = (ConnectionConfig) targetCombo.getSelectedItem();
        if (source == null || target == null) {
            Messages.showInfoMessage(project, "Select both source and target.", "DiffDB");
            return;
        }
        if (source.getId().equals(target.getId())) {
            Messages.showInfoMessage(project, "Source and target are the same connection.", "DiffDB");
            return;
        }

        new Task.Backgroundable(project, "Comparing schemas", true) {
            private SchemaDiffResult result;
            private Throwable error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    LOG.info("Compare started: source=" + source.getName() + ", target=" + target.getName());
                    result = diffService.diff(source, target, secretResolver());
                    LOG.info("Compare finished: empty=" + (result != null && result.isEmpty()));
                } catch (Throwable t) {
                    error = t;
                    LOG.error("Compare failed", t);
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Compare failed:\n" + formatError(error), "DiffDB");
                        return;
                    }
                    lastResult.set(result);
                    treePanel.showResult(result);
                    previewPanel.clear();
                });
            }
        }.queue();
    }

    private void onGenerateSql() {
        SchemaDiffResult result = lastResult.get();
        if (result == null) {
            Messages.showInfoMessage(project, "Run Compare first.", "DiffDB");
            return;
        }
        MigrationOptions options = new MigrationOptions();
        options.setIncludeDrops(includeDropsCheck.isSelected());

        new Task.Backgroundable(project, "Generating migration SQL", true) {
            private String sql;
            private Throwable error;

            @Override
            public void run(ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    sql = sqlGenerator.generate(result, options);
                } catch (Throwable t) {
                    error = t;
                    LOG.error("Migration SQL generation failed", t);
                }
            }

            @Override
            public void onFinished() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (error != null) {
                        Messages.showErrorDialog(project,
                                "Generation failed:\n" + formatError(error), "DiffDB");
                        return;
                    }
                    previewPanel.setSql(sql);
                });
            }
        }.queue();
    }

    private SchemaDiffService.SecretResolver secretResolver() {
        return new SchemaDiffService.SecretResolver() {
            @Override
            public String dbPassword(ConnectionConfig config) {
                return CredentialService.getDbPassword(config.getId());
            }

            @Override
            public String sshSecret(ConnectionConfig config) {
                return CredentialService.getSshSecret(config.getId());
            }
        };
    }

    private static String formatError(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = root.toString();
        }
        return msg;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew compileJava 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/diffdb/ui/DiffDbPanel.java
git commit -m "feat: integrate DbManagerPanel into DiffDbPanel with CRUD in top section"
```

---

### Task 8: HTML style demo

**Files:**
- Create: `docs/db-management-demo.html`

- [ ] **Step 1: Write the HTML demo file**

This is a standalone HTML file that demonstrates the target UI visually. Write the full HTML with inline CSS that matches IntelliJ Darcula theme aesthetics.

The demo should show:
1. The DB Management panel at top (with toolbar + connection cards)
2. One expanded connection showing tables
3. Two collapsed connections
4. Source/Target dropdown bar
5. Diff tree and SQL preview split

Write the full standalone HTML with embedded CSS to `docs/db-management-demo.html`.

- [ ] **Step 2: Commit**

```bash
git add docs/db-management-demo.html
git commit -m "docs: add HTML style demo for DB management UI"
```

---

### Task 9: Integration testing and final verification

- [ ] **Step 1: Run all tests**

Run: `./gradlew test 2>&1 | tail -20`
Expected: All tests pass

- [ ] **Step 2: Verify plugin builds**

Run: `./gradlew buildPlugin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit if any fixes needed**

```bash
git add -A
git commit -m "fix: address integration test issues"
```