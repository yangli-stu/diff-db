# DiffDB

An IntelliJ IDEA plugin that compares the **structure of two databases**
(MySQL / PostgreSQL), directly or over an **SSH tunnel**, shows the differences
in a **colour-coded table**, and generates **dialect-aware migration SQL**.

Works in both **Community** and Ultimate editions — unlike IDEA's built-in
"Compare Structure" (Ultimate-only) and JPA Buddy's diff (Ultimate + JPA-bound).

## Features

- **Schema comparison**: tables, columns, indexes, keys, foreign keys, views, and more
- **Visual diff table**: ADD (green) / DROP (red) / MODIFY (orange) — clearly categorised
- **Migration SQL generation**: target-dialect-aware DDL, with copy and export support
- **SSH tunnel support**: connect through local-port-forwarding tunnels
- **Connection management**: add, edit, delete, import/export connection profiles
- **Safe by design**: passwords and secrets are never stored in plain text
- **Read-only safety**: the plugin only reads database schema metadata; it never executes DDL/DML or writes to your database

## Privacy & Security

- **No data collection**: the plugin does not send any data to external servers
- **Passwords are encrypted**: all secrets (database passwords, SSH passphrases) are stored in IntelliJ's **PasswordSafe** — never written to disk in plain text
- **Local-only processing**: schema comparison and SQL generation happen entirely within the IDE; your database schemas never leave your machine
- **No telemetry or analytics**: we do not track usage, crashes, or behaviour
- **Read-only guarantee**: the plugin only reads `information_schema` / `pg_catalog` metadata. It never creates, drops, or modifies any database object. Generated SQL is for preview/copy only — execution is entirely manual and outside the plugin.

## Tech Stack

| Concern | Choice |
|---------|--------|
| Build | Gradle + IntelliJ Platform Gradle Plugin 2.x |
| Language | Java 17 |
| Diff engine | Liquibase Core |
| SSH | mwiede/jsch |
| JDBC drivers | MySQL + PostgreSQL bundled (~3 MB); optional jar override |
| Tests | JUnit 5 + H2 |

See [DESIGN.md](DESIGN.md) for the full architecture and class responsibilities.

## Develop

```bash
# Run a sandbox IDE with the plugin loaded
./gradlew runIde

# Run tests (H2 fixtures: src/test/resources/case1|2|3/)
./gradlew test

# Build the distributable zip (build/distributions)
./gradlew buildPlugin

# Verify platform compatibility
./gradlew verifyPlugin
```

> First Gradle run downloads the IntelliJ Platform SDK; it may take a while.

## JDBC Drivers

MySQL and PostgreSQL drivers are **bundled** with the plugin so **Test Connection**
works without extra setup. Use **Driver jar (override, optional)** only when you
need a specific driver version.

## License

Apache 2.0
