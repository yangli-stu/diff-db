# DB Management Module & YAML Import/Export — Design Spec

> Date: 2026-06-05

## Overview

Add a standalone DB management module at the top of the DiffDB Tool Window, supporting CRUD operations on database connections, inline expansion to show tables under each DB, and YAML-based import/export of connection configurations.

## UI Architecture

### Layout: Fixed Top Panel (Option A)

The Tool Window is divided into two zones:

```
┌─────────────────────────────────────────────┐
│  🗄 Databases                    [+ Add] [⬇ Import] [⬆ Export]  │
│  ┌─────────────────────────────────────────┐│
│  │ ▼ dev-postgres  [connected] [🔌 ✏️ 📋 🗑]  ││
│  │   📋 users — 12 cols                     ││
│  │   📋 orders — 8 cols                    ││
│  │   📋 products — 6 cols                  ││
│  │   4 tables total                         ││
│  ├─────────────────────────────────────────┤│
│  │ ▶ prod-mysql  [🔌 ✏️ 📋 🗑]                ││
│  ├─────────────────────────────────────────┤│
│  │ ▶ staging-mysql  [🔌 ✏️ 📋 🗑]              ││
│  └─────────────────────────────────────────┘│
├─────────────────────────────────────────────┤
│  Source: [prod-mysql ▾]  Target: [dev-postgres ▾]  [Compare] [Generate SQL]  │
│  ☐ Include DROPs                            │
├─────────────────────────────────────────────┤
│  Diff Tree          │  Migration SQL Preview │
│                     │                        │
└─────────────────────────────────────────────┘
```

### DB Management Panel Components

**Toolbar bar:**
- Title "Databases" (left-aligned)
- Buttons: `+ Add`, `⬇ Import`, `⬆ Export` (right-aligned)

**Connection card (collapsed):**
- Expand arrow `▶` + connection name + badge showing DB type & host
- Action icons: 🔌 Test Connection, ✏️ Edit, 📋 Duplicate, 🗑 Delete

**Connection card (expanded):**
- Collapse arrow `▼` + connection name + status badge ("connected" in green, or "disconnected")
- Same action icons as collapsed
- Sub-list of tables under this connection: table name + column count
- Footer: "N tables total"

**Table expansion behavior:**
- Clicking the expand/collapse arrow toggles the inline table list
- Table list is loaded on-demand: expand triggers a background JDBC metadata query
- While loading, show a "Loading..." placeholder
- If connection fails, show error message inline (red text)
- Table expansion is independent of any diff operation — purely for browsing

### Diff Section Integration

- Source/Target dropdowns auto-populate from the connections in the DB management panel
- When the user clicks a connection name in the DB management panel, it doesn't change Source/Target (avoid accidental overwrites)
- The dropdown format shows: `name (type - host)`, e.g., `prod-mysql (MySQL - 10.0.1.100)`

## CRUD Operations

### Add (`+ Add` button)
- Opens the existing `ConnectionDialog` to create a new `ConnectionConfig`
- On save, persists to `ConnectionStorageService` and credentials to `CredentialService`
- The new connection appears in the DB management panel immediately

### Edit (`✏️` icon)
- Opens `ConnectionDialog` pre-filled with the selected connection's data
- Saves changes back to `ConnectionStorageService` and `CredentialService`

### Duplicate (`📋` icon)
- Creates a copy of the selected connection with `name` appended with " (copy)"
- Generates a new `id` for the copy
- Opens `ConnectionDialog` pre-filled with the copy for the user to adjust before saving

### Delete (`🗑` icon)
- Confirmation dialog: "Remove connection '{name}'? This cannot be undone."
- Removes from `ConnectionStorageService` and clears credentials from `CredentialService`
- If the deleted connection was selected in Source/Target dropdowns, clear those selections

### Test Connection (`🔌` icon)
- Runs `ConnectionManager.testConnection()` in background
- Shows result inline on the connection card (green "connected" badge or red error)

## YAML Import/Export

### Export Format

```yaml
# DiffDB Connection Configurations
# Export date: 2026-06-05

connections:
  - name: prod-mysql                    # Display name for this connection
    type: MYSQL                         # Database type: MYSQL or POSTGRESQL
    host: 10.0.1.100                    # Database server hostname or IP
    port: 3306                          # Database server port (default: 3306 for MySQL, 5432 for PostgreSQL)
    database: myapp_prod                # Database name to connect to
    schema: null                        # Schema name (PostgreSQL only; leave null for MySQL)
    user: admin                         # Database login username
    # password: ***                     # Not exported; stored in IntelliJ PasswordSafe
    driver_jar: null                    # Optional: custom JDBC driver jar path (null = use bundled driver)
    ssh:                                # SSH tunnel config (omit section if no SSH)
      enabled: true                     # Whether to use SSH tunnel
      host: bastion.example.com         # SSH server hostname
      port: 22                          # SSH server port
      user: deploy                      # SSH login username
      auth_type: KEY                    # SSH auth: PASSWORD or KEY
      # ssh_password: ***               # Not exported
      private_key_path: /home/user/.ssh/id_rsa  # Path to SSH private key (for KEY auth)
      # ssh_key_passphrase: ***         # Not exported
      db_host: 127.0.0.1               # Database host as seen from SSH server
      db_port: 3306                     # Database port as seen from SSH server
```

**Key decisions:**
- Passwords and SSH secrets are **never** exported. YAML contains a comment `# Not exported; stored in IntelliJ PasswordSafe`
- Each parameter has an inline comment (`#`) explaining its meaning
- `ssh:` is omitted entirely when `ssh.enabled: false` or no SSH is configured
- `driver_jar: null` when using bundled driver
- `schema: null` for MySQL (PostgreSQL requires it)

### Import Behavior

1. User clicks `⬇ Import` → file chooser dialog (`.yaml` / `.yml` filter)
2. Parse the YAML file and validate:
   - Required fields: `name`, `type`, `host`, `database`, `user`
   - `type` must be `MYSQL` or `POSTGRESQL` (case-insensitive)
   - If `port` is missing, use default based on `type`
   - If `type` is `POSTGRESQL` and `schema` is missing, default to `public`
   - If `ssh.enabled` is true, `ssh.host` is required
3. Show preview dialog: list of connections to import with any warnings
4. For each connection:
   - Generate a new `id` (UUID)
   - Validate `type` maps to `DatabaseType` enum
   - Create `ConnectionConfig` and `SshConfig` objects
   - Save to `ConnectionStorageService`
   - **Passwords must be entered manually after import** — they cannot be round-tripped through YAML
5. Show result: "Imported N connections. Please edit each connection to set passwords."

### Import Conflict Handling

- If a connection with the same `name` already exists:
  - Skip it (default) with a warning
  - Optionally auto-rename by appending a suffix

## New Classes

### `DbManagerPanel` (`ui/`)
- The top panel containing the DB management UI
- Contains: toolbar, scrollable connection list, connection cards
- Each connection card is a `DbConnectionCard` component

### `DbConnectionCard` (`ui/`)
- Represents a single connection in the DB management list
- Expandable/collapsible to show tables
- Action buttons: Test, Edit, Duplicate, Delete
- Table list loaded on-demand via `TableBrowserService`

### `TableBrowserService` (`service/`)
- Given a `ConnectionConfig`, fetches table names and column counts via JDBC metadata
- Runs in background with progress indicator
- Returns `List<TableInfo>` where `TableInfo` has `tableName` and `columnCount`

### `ConnectionYamlService` (`service/`)
- `exportConnections(List<ConnectionConfig>)` → YAML string with comments
- `importConnections(String yaml)` → `ImportResult` containing `List<ConnectionConfig>` and `List<String> warnings`
- Uses SnakeYAML (already available via Liquibase dependency)

### `ConnectionConfig` changes
- No structural changes — YAML import/export works with existing fields
- `id` is generated fresh on import (not preserved from YAML)

## File Changes Summary

| File | Change |
|------|--------|
| `ui/DbManagerPanel.java` | **New** — DB management panel component |
| `ui/DbConnectionCard.java` | **New** — Single connection card component |
| `ui/DiffDbPanel.java` | **Modify** — Add `DbManagerPanel` at top, update Source/Target combo model |
| `service/ConnectionYamlService.java` | **New** — YAML import/export logic |
| `service/TableBrowserService.java` | **New** — Table listing via JDBC metadata |
| `service/ConnectionStorageService.java` | **Modify** — Add lookup-by-name, conflict detection methods |
| `model/ConnectionConfig.java` | **Minor** — May add convenience method `getDisplayInfo()` for UI labels |
| `model/TableInfo.java` | **New** — Simple POJO: `tableName`, `columnCount` |

## YAML Library

- Use **SnakeYAML** (already a transitive dependency via Liquibase)
- No additional dependency needed
- For YAML output with comments, use `Yaml` with custom `Representer` and write comment lines manually (SnakeYAML doesn't natively support comments in output)

## Implementation Order

1. **`TableInfo` model + `TableBrowserService`** — foundation for table listing
2. **`ConnectionYamlService`** — YAML import/export logic
3. **`DbConnectionCard`** — individual connection UI component
4. **`DbManagerPanel`** — assemble the panel with toolbar + scrollable card list
5. **`DiffDbPanel`** modification — integrate `DbManagerPanel` and update Source/Target dropdowns
6. **`ConnectionStorageService`** enhancements — add lookup helpers
7. **Testing** — unit tests for YAML round-trip, integration test for table browsing