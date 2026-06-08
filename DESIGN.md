# DiffDB —— 数据库结构对比与迁移 SQL 生成插件 设计文档

> 一个面向 **IntelliJ IDEA Community / Ultimate** 的数据库结构对比插件。
> 直连或经 SSH 隧道对比两个数据库(MySQL / PostgreSQL)的结构差异，
> 以可视化差异表格展示，并生成可编辑、可导出的迁移 SQL。

---

## 1. 产品定位

| 维度 | 说明 |
|------|------|
| 解决的痛点 | IDEA 原生「Compare Structure」是 **Ultimate 付费**；JPA Buddy 的 diff 也需 Ultimate 且绑定 JPA 生态 |
| 差异化 | **Community 也能用** 的纯 JDBC schema diff，不依赖 JPA/Flyway/Liquibase 工程结构 |
| 核心能力 | 双库结构对比 → 可视化差异表格 → 生成目标库方言迁移 SQL → 导出 / 复制 |
| 首发数据库 | **MySQL、PostgreSQL** |
| 远程支持 | SSH 本地端口转发隧道 |
| 分发 | IntelliJ IDEA 插件（免费开源） |

---

## 2. 整体技术选型

### 2.1 选型总览

| 层 | 选型 | 理由 |
|----|------|------|
| 语言 | **Java 17**（源码级别，运行于 IDE 自带 JBR 17/21） | 平台要求，兼容性好 |
| 构建 | **Gradle (Kotlin DSL) + IntelliJ Platform Gradle Plugin 2.x** | 官方推荐，支持 `runIde` / `buildPlugin` |
| Diff 引擎 | **Liquibase Core 4.x** | 内置多库 snapshot + `DiffResult`，开源 Apache 2.0，省 80% 工作 |
| 迁移 SQL | Liquibase `DiffToChangeLog` + `SqlGeneratorFactory` | 同一 `DiffResult` 直接出目标库方言 SQL |
| 元数据 | Liquibase snapshot（底层 JDBC `DatabaseMetaData` / `information_schema`） | 表/列/索引/约束/外键/视图齐全 |
| SSH 隧道 | **com.github.mwiede:jsch**（JSch 维护分支） | 支持现代算法，轻量(~300KB)，原版 jsch 已停更 |
| JDBC 驱动 | MySQL / PostgreSQL 驱动 **运行时按需**，不强制打包 | 控制体积，规避许可证 |
| 凭据存储 | IntelliJ **PasswordSafe** | 密码/私钥口令不落明文 |
| 配置持久化 | **PersistentStateComponent**（仅存非敏感字段） | 连接列表随项目/应用保存 |
| UI | IntelliJ Platform Swing（ToolWindow / `DialogWrapper` / `Table` / `DiffManager`） | 与 IDE 风格一致 |
| 测试 | JUnit 5 + H2 | 内存双库做 diff 单测 |
| 许可证 | Apache 2.0 | 开源友好 |

### 2.2 为什么不选 Calcite

Calcite 定位是 SQL 查询解析/优化，**核心不支持 DDL**，server 模块的 DDL 也缺索引/外键/注释，方言要自己抄代码。它不为「元数据建模 + 结构 diff」而生，故不采用。

### 2.3 体积控制策略

- JDBC 驱动用 `compileOnly`，运行时由用户提供/下载，不打包。
- `liquibase-core` 排除用不到的传递依赖（如 `picocli`、按需排除 `snakeyaml`）。
- 复用 IDE 平台已自带库（Gson、commons-lang3 等）。
- 预期插件包 ~10–20MB，远低于平台 400MB 上限。

---

## 3. 架构分层

```
+-------------------------------------------------------------+
|                          UI 层                               |
|  ToolWindow / DiffDbPanel / ConnectionDialog                |
|  DiffResultTable(差异表格) / MigrationPreview(SQL 预览编辑)  |
+-------------------------------------------------------------+
|                        应用服务层                            |
|  ConnectionStorageService(持久化) / CredentialService(凭据)  |
+-------------------------------------------------------------+
|                        核心领域层                            |
|  SchemaDiffService(对比) / MigrationSqlGenerator(迁移 SQL)    |
|  SchemaDiffResult / DiffNode(UI 解耦模型)                     |
+-------------------------------------------------------------+
|                        基础设施层                            |
|  ConnectionManager / JdbcUrlBuilder / DriverProvider         |
|  SshTunnel(端口转发)                                          |
+-------------------------------------------------------------+
|                  外部依赖：Liquibase / JSch / JDBC           |
+-------------------------------------------------------------+
```

设计原则：
- **UI 与引擎解耦**：UI 只依赖 `SchemaDiffResult`/`DiffNode`，不直接依赖 Liquibase 类型，便于后期替换引擎。
- **隧道透明**：SSH 层只产出 `localhost:port`，对 diff 引擎不可见。
- **接口先行**：`SchemaDiffService`、`MigrationSqlGenerator` 接口化，实现可替换。

---

## 4. 任务拆分（里程碑）

### Phase 0 工程骨架
- [x] 设计文档
- [ ] Gradle + IntelliJ Platform 插件工程
- [ ] plugin.xml / 图标 / ToolWindow 注册

### Phase 1 MVP（核心闭环）
- [ ] model：连接配置 + SSH 配置数据模型
- [ ] infra：JDBC URL 构建、驱动加载、连接管理、SSH 隧道
- [ ] core：`SchemaDiffService`（双库 → `SchemaDiffResult`）
- [ ] core：`MigrationSqlGenerator`（diff → 目标方言 SQL）
- [ ] ui：连接对话框 + 差异表格 + SQL 预览
- [ ] service：连接持久化 + PasswordSafe 凭据

### Phase 2 增强
- [ ] 危险操作（DROP）高亮 / 仅增量开关
- [ ] 重命名识别（DROP+ADD → RENAME 合并）
- [ ] 导出 Flyway / Liquibase changelog
- [ ] 对象类型过滤（表/索引/外键/视图）

### Phase 3 执行与导出
- [ ] 直连库执行迁移（二次确认 + 事务）
- [ ] 导出 Flyway / Liquibase changelog 格式
- [ ] 对象类型过滤（表/索引/外键/视图）

---

## 5. 类级别职责设计

> 包根：`com.diffdb`

### 5.1 model（数据模型，无逻辑依赖）

| 类 | 职责 |
|----|------|
| `DatabaseType`（enum） | MYSQL / POSTGRESQL；持有默认端口、驱动类名、JDBC URL 模板、Liquibase 短名 |
| `AuthType`（enum） | SSH 认证方式：PASSWORD / KEY |
| `SshConfig` | SSH 主机/端口/用户/认证方式/私钥路径；以及隧道目标 dbHost/dbPort |
| `ConnectionConfig` | 一个数据库连接的完整配置：id、显示名、`DatabaseType`、host/port/database/schema/user、是否启用 SSH、`SshConfig`。**不持久化明文密码**（密码走 PasswordSafe，按 id 关联） |

### 5.2 infra/ssh

| 类 | 职责 |
|----|------|
| `SshTunnel`（`AutoCloseable`） | 按 `SshConfig` 建立 SSH 会话并做本地端口转发；暴露 `getLocalHost()/getLocalPort()`；`close()` 释放 |

### 5.3 infra/connection

| 类 | 职责 |
|----|------|
| `JdbcUrlBuilder` | 按 `DatabaseType` + 实际 host/port/database 组装 JDBC URL |
| `DriverProvider` | 加载 JDBC 驱动：优先用户指定 jar（URLClassLoader），回退 classpath；缓存已加载驱动 |
| `ConnectionManager` | 给定 `ConnectionConfig`：若启用 SSH 先开 `SshTunnel`，再用真实地址建 `java.sql.Connection`；返回 `ManagedConnection`(持有 Connection + 可选 tunnel，统一关闭) |
| `ManagedConnection`（`AutoCloseable`） | 包装 `Connection` 与可选 `SshTunnel`，`close()` 一并释放 |

### 5.4 core/diff

| 类 | 职责 |
|----|------|
| `DiffCategory`（enum） | MISSING（目标缺，需新增）/ UNEXPECTED（目标多，需删除）/ CHANGED（有差异） |
| `DiffNode` | UI 解耦的差异节点：对象类型(table/column/index...)、名称、类别、详情；含 children 支持层级。**不引用 Liquibase 类型** |
| `SchemaDiffResult` | 一次对比的结果：根 `DiffNode` 列表 + 原始 `DiffResult`（供生成 SQL 用，封装在 core 内不外泄到 UI） |
| `SchemaDiffService`（接口） | `SchemaDiffResult diff(ConnectionConfig source, ConnectionConfig target)` |
| `LiquibaseSchemaDiffService`（实现） | 用 `ConnectionManager` 取连接 → Liquibase `DatabaseFactory`/`DiffGeneratorFactory.compare` → 映射成 `DiffNode` 列表 |

### 5.5 core/migration

| 类 | 职责 |
|----|------|
| `MigrationOptions` | 生成选项：是否包含 DROP、是否含 schema 限定、对象类型过滤等 |
| `MigrationSqlGenerator`（接口） | `String generate(SchemaDiffResult diff, MigrationOptions options)` |
| `LiquibaseMigrationSqlGenerator`（实现） | `DiffToChangeLog.generateChangeSets()` → 逐 `Change` 经 `SqlGeneratorFactory` 按目标库方言生成 SQL 文本 |

### 5.6 service（IDE 集成）

| 类 | 职责 |
|----|------|
| `ConnectionStorageService`（`@State` + `PersistentStateComponent`） | 保存/读取 `ConnectionConfig` 列表（不含密码） |
| `CredentialService` | 封装 `PasswordSafe`：按连接 id 存取数据库密码、SSH 口令/私钥口令 |

### 5.7 ui

| 类 | 职责 |
|----|------|
| `DiffDbToolWindowFactory`（`ToolWindowFactory`） | 注册 ToolWindow，创建 `DiffDbPanel` |
| `DiffDbPanel` | 主面板：源/目标连接选择、操作按钮（编辑连接/对比/生成 SQL）、承载差异表格与 SQL 预览的分栏 |
| `ConnectionDialog`（`DialogWrapper`） | 编辑单个 `ConnectionConfig`：基础参数 + SSH 折叠区；测试连接；保存（密码写 PasswordSafe） |
| `DiffResultTablePanel` | 用 `SchemaDiffResult` 渲染差异表格；Object / Type / Change / Detail 四列展示 |
| `DiffResultTableModel` | 表格模型：扁平化 `DiffNode`，过滤无意义的 ordering / defaultValue 变化 |
| `MigrationPreviewPanel` | 展示生成的迁移 SQL（可编辑 EditorTextField）；复制 / 导出 .sql |

### 5.8 action

| 类 | 职责 |
|----|------|
| `RunDiffAction` | 触发 `SchemaDiffService.diff` 并刷新差异表格（后台任务 `Task.Backgroundable`） |
| `GenerateMigrationAction` | 触发 `MigrationSqlGenerator.generate` 并填充预览面板 |

---

## 6. 关键流程

### 6.1 对比流程
```
用户选 源/目标连接 → RunDiffAction
  → ConnectionManager 建连接(必要时开 SSH 隧道)
  → LiquibaseSchemaDiffService.diff()
      → DatabaseFactory 包装两个 Connection
      → DiffGeneratorFactory.compare() 得 DiffResult
      → 映射为 DiffNode 列表 → SchemaDiffResult
  → DiffResultTablePanel 渲染
  → 关闭连接与隧道
```

### 6.2 迁移 SQL 流程
```
用户点「生成迁移 SQL」 → GenerateMigrationAction
  → LiquibaseMigrationSqlGenerator.generate(diffResult, options)
      → DiffToChangeLog.generateChangeSets()
      → 逐 Change.generateStatements(targetDb)
      → SqlGeneratorFactory.generateSql(stmt, targetDb)
  → MigrationPreviewPanel 显示(可编辑/复制/导出)
```

---

## 7. 安全与合规

| 项 | 处理 |
|----|------|
| 数据库密码 / SSH 口令 | 仅存 PasswordSafe，配置文件不落明文 |
| SSH Host Key | MVP 先 `StrictHostKeyChecking=no`，Phase 2 接入 known_hosts 指纹校验 |
| 危险 DDL | DROP 类语句 UI 红色高亮 + 「仅生成增量」开关 |
| 直连执行 | 默认只预览/导出；执行需二次确认 |
| 隐私 | 不收集用户数据，声明「No data collected」 |

---

## 8. 目录结构

```
diff-db/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── DESIGN.md
├── README.md
├── docs/
│   └── LOCAL_RUN.md        # 本地运行与沙箱调试
└── src/
    ├── main/
    │   ├── java/com/diffdb/
    │   │   ├── model/      DatabaseType, AuthType, SshConfig, ConnectionConfig
    │   │   ├── ssh/        SshTunnel
    │   │   ├── connection/ JdbcUrlBuilder, DriverProvider, ConnectionManager, ManagedConnection
    │   │   ├── diff/       DiffCategory, DiffNode, SchemaDiffResult, SchemaDiffService, LiquibaseSchemaDiffService
    │   │   ├── migration/  MigrationOptions, MigrationSqlGenerator, LiquibaseMigrationSqlGenerator
    │   │   ├── service/    ConnectionStorageService, CredentialService
│   │   └── ui/         DiffDbToolWindowFactory, DiffDbPanel, ConnectionDialog,
│   │                   DbManagerPanel, ShowTablesDialog,
│   │                   DiffResultTablePanel, DiffResultTableModel, MigrationPreviewPanel
    │   └── resources/
    │       └── META-INF/plugin.xml, pluginIcon.svg
    └── test/
        ├── java/com/diffdb/
        │   ├── SchemaDiffFixture.java   # 加载 SQL fixture 并跑 Liquibase diff
        │   ├── SchemaDiffCaseTest.java  # case1/2/3 场景断言
        │   └── JdbcUrlBuilderTest.java
        └── resources/
            ├── case1/  origin_db.sql, target_db.sql   # 目标缺表
            ├── case2/  origin_db.sql, target_db.sql   # 列/索引差异
            └── case3/  origin_db.sql, target_db.sql   # 目标多表
```
