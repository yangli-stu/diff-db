# DiffDB 发布上线流程与 TODO

## 发布前检查清单

- [ ] 版本号已更新（`gradle.properties` → `pluginVersion`，`plugin.xml` change-notes）
- [ ] `./gradlew test` 全部通过
- [ ] `./gradlew buildPlugin` 成功，zip 体积合理（当前约 5–6 MB）
- [ ] `./gradlew runIde` 沙箱手动走通：建连 → Compare → Generate SQL → Export
- [ ] MySQL、PostgreSQL 各至少测一条真实连接
- [ ] SSH 隧道场景测过（如有条件）
- [ ] README / 插件描述与截图已更新
- [ ] 无硬编码密码、无调试日志泄露敏感信息

---

## 第一次上架（手动上传）

JetBrains 要求**首个版本必须手动上传**，之后可用 Gradle 自动化。

### Step 1：注册与 Vendor

1. 注册 [JetBrains Account](https://account.jetbrains.com/)
2. 打开 [JetBrains Marketplace](https://plugins.jetbrains.com/) → **Upload Plugin**
3. 创建 **Vendor**（个人即可）
4. 声明 **Trader / Non-trader**（个人 hobby、完全免费 → 通常选 **Non-trader**）

### Step 2：准备合规材料

| 材料 | 说明 |
|------|------|
| **EULA** | Marketplace 必填；可用 Apache 2.0 简短说明或自建 EULA 页面 |
| **Privacy Policy** | 若不收集用户数据，写「不收集、不上传任何个人数据」即可 |
| **Plugin 描述** | 英文为主；说明 CE 可用、支持 MySQL/PG、SSH、迁移 SQL |
| **截图** | 至少 1–2 张：差异树 + SQL 预览 |
| **图标** | 已有 `src/main/resources/META-INF/pluginIcon.svg` |
| **开源链接** | 若 Apache 2.0，提供 GitHub 仓库 URL |

### Step 3：插件签名

Marketplace 要求插件签名。在 [JetBrains Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html) 生成证书后：

```bash
export CERTIFICATE_CHAIN="-----BEGIN CERTIFICATE-----..."
export PRIVATE_KEY="-----BEGIN PRIVATE KEY-----..."
export PRIVATE_KEY_PASSWORD="your-password"

./gradlew signPlugin buildPlugin
```

上传 `build/distributions/diff-db-<version>.zip`（已签名）。

### Step 4：填写插件页并提交审核

1. Plugin ID：`com.diffdb.schema-compare`（与 `gradle.properties` 一致）
2. 兼容 IDE：sinceBuild `241`（2024.1+），untilBuild 按需
3. 分类：**Database**
4. 提交 → 等待 **人工审核**（通常数天）

审核指南：[Approval Guidelines](https://plugins.jetbrains.com/docs/marketplace/jetbrains-marketplace-approval-guidelines.html)

---

## 后续版本发布（Gradle 自动化）

首次手动上传成功后：

```bash
# 1.  bump pluginVersion in gradle.properties

# 2.  build & sign
export CERTIFICATE_CHAIN=... PRIVATE_KEY=... PRIVATE_KEY_PASSWORD=...
./gradlew buildPlugin signPlugin

# 3.  publish（需 Marketplace Token）
export PUBLISH_TOKEN=...   # plugins.jetbrains.com → Account → My Tokens
./gradlew publishPlugin
```

可选发布 channel（默认 `default`；beta 用户需配自定义 repository）。

---

## 版本号规范建议

采用语义化 + 构建号，例如：

- `0.1.0` — MVP
- `0.2.0` — 新功能（Flyway 导出、重命名识别）
- `0.1.1` — 仅 bugfix

每次发布必须**递增** `pluginVersion`，Marketplace 不接受同版本重复上传。

---

## 个人开发者 FAQ

| 问题 | 回答 |
|------|------|
| 个人能上架吗？ | **可以**，无需公司主体 |
| 免费插件要 Trader 认证吗？ | 选 Non-trader 即可，流程最简单 |
| 付费插件？ | 需 Trader 认证（身份证、地址、银行等，2025 起更严） |
| 包体积 ~6MB 影响上架吗？ | 不影响，上限 400MB |
| 审核被拒常见原因？ | 描述不清、缺 EULA、功能与描述不符、未签名 |

---

## 产品 TODO（按优先级）

### P0 — 上架必需 / 稳定性

- [ ] 替换 `plugin.xml` 中 vendor 占位邮箱/URL 为真实信息
- [ ] 编写 EULA 与 Privacy Policy 页面（或仓库内 `LEGAL.md`）
- [ ] 准备 2 张 Marketplace 截图（差异树 + SQL 预览）
- [ ] 真实 MySQL / PostgreSQL 端到端回归
- [ ] 生成并配置插件签名证书

### P1 — 体验增强（建议 0.2.0）

- [ ] DROP 语句默认关闭 + 红色高亮（UI 已有 checkbox，可加强提示）
- [ ] 重命名识别：DROP+ADD 合并为 RENAME 建议
- [ ] SSH Host Key 校验（known_hosts 指纹，替代 StrictHostKeyChecking=no）
- [ ] 对象类型过滤（仅表 / 含索引 / 含外键）
- [ ] 导出 Flyway `Vxxx__xxx.sql` / Liquibase changelog

### P2 — 扩展

- [ ] 支持 Oracle / SQL Server
- [ ] 复用 IDEA Settings → SSH Configurations
- [ ] 在目标库执行迁移 SQL（二次确认 + 事务）
- [ ] 数据行级对比（非结构 diff，工作量大）
- [ ] 中英文 i18n

### P3 — 工程 / CI

- [ ] GitHub Actions：`./gradlew test buildPlugin` on push
- [ ] 升级 `intellijPluginVersion` 至 2.16.0+
- [ ] 按需恢复 `verifyPlugin`（固定 IDE 版本列表，避免联网 flaky）
- [ ] 补充 MySQL Testcontainers 集成测试（可选）

---

## 推荐时间线（参考）

| 阶段 | 内容 | 预估 |
|------|------|------|
| Week 1 | P0 真实库测试 + 签名 + 截图 + 合规文案 | 3–5 天 |
| Week 2 | 首次手动上传 + 审核反馈修改 | 3–7 天（含审核等待） |
| Week 3+ | P1 功能迭代，`publishPlugin` 发 0.2.0 | 持续 |

---

## 相关链接

- [Publishing a Plugin](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html)
- [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)
- [Trader / Non-trader](https://plugins.jetbrains.com/docs/marketplace/trader-status.html)
- 本地开发： [LOCAL_RUN.md](./LOCAL_RUN.md)
- 架构设计： [../DESIGN.md](../DESIGN.md)
