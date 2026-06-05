# DiffDB

An IntelliJ IDEA plugin that compares the **structure of two databases**
(MySQL / PostgreSQL), directly or over an **SSH tunnel**, shows the differences
as a colour-coded tree, and generates **dialect-aware migration SQL**.

Works in both **Community** and Ultimate editions — unlike IDEA's built-in
"Compare Structure" (Ultimate-only) and JPA Buddy's diff (Ultimate + JPA-bound).

## Features

- Compare two databases' schemas (tables, columns, indexes, keys, FKs, views…)
- Visual diff tree: green = to create, red = to drop, orange = changed
- Generate migration SQL in the target's dialect; copy or export `.sql`
- Direct or SSH-tunnelled connections
- Secrets stored in IntelliJ PasswordSafe (never in plain text)

## Tech stack

| Concern | Choice |
|---------|--------|
| Build | Gradle + IntelliJ Platform Gradle Plugin 2.x |
| Language | Java 17 |
| Diff engine | Liquibase Core |
| SSH | mwiede/jsch |
| JDBC drivers | MySQL + PostgreSQL bundled (~3 MB); optional jar override |
| Tests | JUnit 5 + H2 |

See [DESIGN.md](DESIGN.md) for the full architecture and class responsibilities.

## Documentation

| Doc | Content |
|-----|---------|
| [docs/LOCAL_RUN.md](docs/LOCAL_RUN.md) | 本地环境、Gradle 命令、沙箱调试、手动验证 |
| [docs/RELEASE.md](docs/RELEASE.md) | Marketplace 上架流程、签名发布、产品 TODO |
| [DESIGN.md](DESIGN.md) | 技术选型与类级别职责 |

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

## JDBC drivers

MySQL and PostgreSQL drivers are **bundled** with the plugin so **Test Connection**
works without extra setup. Use **Driver jar (override, optional)** only when you
need a specific driver version.

## Publish to JetBrains Marketplace

See **[docs/RELEASE.md](docs/RELEASE.md)** for the full checklist, first-time manual upload,
signing, `publishPlugin`, and the product TODO backlog.

Quick reference:

1. Build & sign:
   ```bash
   export CERTIFICATE_CHAIN=... PRIVATE_KEY=... PRIVATE_KEY_PASSWORD=...
   ./gradlew signPlugin buildPlugin
   ```
2. First upload is manual at <https://plugins.jetbrains.com/>.
3. Later releases:
   ```bash
   export PUBLISH_TOKEN=...   # Marketplace → My Tokens
   ./gradlew publishPlugin
   ```

Individual developers can publish (Non-trader for a free plugin).

## License

Apache 2.0
