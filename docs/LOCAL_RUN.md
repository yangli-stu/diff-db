# DiffDB 本地运行指南

## 环境要求

| 项 | 要求 |
|----|------|
| JDK | **17+**（推荐 17 或 21；与 IntelliJ Platform 一致） |
| 构建 | 项目自带 Gradle Wrapper，无需全局安装 Gradle |
| IDE | IntelliJ IDEA 2024.1+（Community / Ultimate 均可） |
| 网络 | 首次 `./gradlew` 会下载 IntelliJ Platform SDK 与依赖，需可访问 Maven Central |

验证环境：

```bash
java -version    # 应显示 17 或更高
./gradlew --version
```

---

## 1. 导入工程

1. IntelliJ IDEA → **Open** → 选择项目根目录 `diff-db`
2. 等待 Gradle 同步完成（右下角进度条）
3. 若提示 JDK：Settings → Build → Gradle → Gradle JVM 选 **JDK 17+**

> 旧版 `diff-db.iml` 已移除，以 Gradle 工程为准。

---

## 2. 常用 Gradle 命令

在项目根目录执行：

```bash
# 编译主代码
./gradlew compileJava

# 运行全部单元测试（H2 + Liquibase diff）
./gradlew test

# 查看测试报告
open build/reports/tests/test/index.html

# 启动带插件的沙箱 IDEA（开发调试主入口）
./gradlew runIde

# 打包可分发 zip（Marketplace 上传物）
./gradlew buildPlugin
# 产物：build/distributions/diff-db-<version>.zip
```

首次 `runIde` / `buildPlugin` 较慢（下载 IC-2024.1 SDK），后续会走缓存。

---

## 3. 沙箱内手动验证（runIde）

`./gradlew runIde` 会启动独立的 **Sandbox IDE**，已加载当前插件。

### 3.1 打开 DiffDB 面板

> **必须先打开一个项目**（Welcome 界面点 Open 选 `diff-db` 目录，或 File → Open Project）。  
> 未打开项目时 Tool Window 不会出现。

打开方式（任选其一）：

- 右侧工具栏 → **DiffDB**（项目打开后会自动弹出）
- **Tools → DiffDB**
- **View → Tool Windows → DiffDB**
- **Find Action**（⇧⌘A / Ctrl+Shift+A）输入 `DiffDB`

### 3.2 配置连接

1. 点击 **New** 新建连接
2. 填写 Name / Database type（MySQL 或 PostgreSQL）/ Host / Port / Database / User / Password
3. **Driver jar** 可留空（插件已内置 MySQL / PostgreSQL 驱动）；仅在使用特定版本驱动时再指定本地 jar
4. 点击 **Test Connection** 确认连通
5. 确定保存

### 3.3 SSH 隧道（可选）

在连接对话框勾选 **Use SSH tunnel**，填写：

| 字段 | 说明 |
|------|------|
| SSH host / port / user | 跳板机或 DB 服务器 SSH |
| SSH auth | Password 或 Key pair |
| Private key | 私钥文件路径 |
| DB host (from SSH host) | SSH 登录后访问 DB 的地址，常为 `127.0.0.1` |
| DB port (from SSH host) | DB 在 SSH 主机上的端口，如 `3306` |

### 3.4 对比与生成 SQL

1. **Source** 选参考库（期望状态），**Target** 选待同步库
2. 点击 **Compare** → 左侧差异树（绿=目标缺 / 红=目标多 / 橙=变更）
3. 点击 **Generate Migration SQL** → 右侧预览 SQL
4. **Copy** 或 **Export .sql** 导出

### 3.5 调试插件代码

- 在源码中打断点
- `./gradlew runIde` 启动沙箱
- 在沙箱中触发操作，断点会在 **主 IDE** 中命中

---

## 4. 单元测试说明

测试资源目录：

```
src/test/resources/
├── case1/   origin_db.sql + target_db.sql   # 目标缺表
├── case2/   origin_db.sql + target_db.sql   # 列差异
└── case3/   origin_db.sql + target_db.sql   # 目标多表
```

对应测试类：`SchemaDiffCaseTest`（基于 H2 内存库 + Liquibase diff）。

单独跑某个 case：

```bash
./gradlew test --tests "com.diffdb.SchemaDiffCaseTest.case1_missingTable"
```

---

## 5. 查看 Compare / 生成 SQL 日志

DiffDB 会把 Compare 过程写入 IntelliJ 日志（logger: `com.diffdb.ui.DiffDbPanel`）。

### 沙箱 IDE 内

**Help → Show Log in Finder**（Mac）或 **Show Log in Explorer**（Windows），打开 `idea.log`。

### 项目目录（终端）

```bash
# 实时跟踪沙箱日志
tail -f build/idea-sandbox/IC-2024.1/log/idea.log

# 只看 DiffDB 相关
grep -i diffdb build/idea-sandbox/IC-2024.1/log/idea.log | tail -50
```

Compare 失败时日志里通常有 `Compare failed` 或 Liquibase 堆栈；修复后 UI 也会弹出错误对话框。

---

## 6. 常见问题

| 现象 | 处理 |
|------|------|
| Gradle 下载 TLS 握手失败 | 重试；或检查代理/防火墙 |
| Test Connection 报 Driver not found / PluginClassLoader | 重新 `./gradlew clean runIde` 加载内置驱动；或手动指定 Driver jar |
| Compare 点击无反应 / 无结果 | 看 **§5 日志**；常见为 Liquibase 未初始化（已修复）或 `catch` 漏掉 Error；重新 `./gradlew runIde` |
| Compare 很慢 | 远程库检查网络/SSH；大 schema 属正常 |
| Tools → DiffDB 点了没反应 | ① 确认已 **Open Project**（Welcome 页无效）；② 重新 `./gradlew clean runIde` 加载最新插件；③ 若弹出错误框说明 Tool Window 未注册，检查 Settings → Plugins → DiffDB 已启用 |
| 插件 Gradle 版本过旧提示 | 可在 `gradle.properties` 将 `intellijPluginVersion` 升至 2.16.0 |

---

## 7. 本地 MySQL / PostgreSQL 快速造数（可选）

若需真实库联调，可本地起两个 schema/database，分别导入不同 DDL，再在 DiffDB 里配置两个连接对比。

MySQL 示例：

```bash
mysql -u root -p -e "CREATE DATABASE diffdb_origin; CREATE DATABASE diffdb_target;"
mysql -u root -p diffdb_origin < scripts/demo/origin.sql
mysql -u root -p diffdb_target < scripts/demo/target.sql
```

（`scripts/demo/` 可按需自行添加，非测试必需。）
