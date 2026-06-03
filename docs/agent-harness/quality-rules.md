# Agent Harness Quality Rules

## Rule IDs

`scripts/agent_harness.py check` 报告失败时使用的稳定规则 ID：

| ID | 规则 |
| --- | --- |
| H001 | `task.yaml` 存在且可解析 |
| H002 | `spec` 是 `docs/superpowers/specs/` 路径，或 null 配合 `spec_waiver` |
| H003 | `plan` 是 `docs/superpowers/plans/` 路径，或 null 配合 `plan_waiver` |
| H004 | 四个叙述文件存在且非空 |
| H005 | `task_branch` 匹配 `task/YYYY-MM-DD-<slug>` |
| H020 | 无未解析占位符（`TODO`、`TBD`、`fill in`） |
| H021 | 无未解析模板指导语（`explain why`、`replace with`） |
| H030 | 已完成任务记录处于终态（`done` 或 `abandoned`） |

H030 仅在 `check --include-completed` 历史检查中触发；其它规则仅对活动任务。

## Spec First

复杂行为变更前需在 `docs/superpowers/specs/` 放 spec。
若不需要 spec，必须在 `context.md` 解释原因。

## Plan First

多步实现需在 `docs/superpowers/plans/` 放 plan。
若不需要 plan，必须在 `context.md` 解释原因。

## Branch First

重要任务不得直接在 `main` 上做。使用 `task/YYYY-MM-DD-short-slug`，推到 origin、本地验收后开 PR。

## Re-Audit Before Completion

实现完成后，按 spec 逐条审计当前文件与命令输出，写入 `audit.md`。
不要拿一个 focused 测试绿来当作所有 spec 语义完成的证据。

## Live Evidence First

运行调试优先用权威证据：

- `docker compose ps`、`docker compose logs <service>`
- MySQL 表中的实际行（`docker exec hmall-mysql mysql -u root -p123 -e "..."`)
- Redis 中实际 key（`docker exec hmall-redis redis-cli ...`)
- Nacos 控制台中实际配置（`localhost:8848/nacos`）
- 浏览器截图（前端可见的视觉问题）

让运行时证据与推测分开。

## Backend Verification

后端默认验证命令：

```bash
mvn -q -DskipTests=false test
```

## Knowledge Base 同步

代码改动涉及 [knowledge-base](../knowledge-base/INDEX.md) 中某页的 `tracks`
路径时，必须在同一 PR 内同步更新该页（K005）。若内容确无需更新，把
`last_synced_commit` 推到当前 HEAD，并在 `sync_note` 写明原因（轻量豁免）。
新增 Maven 模块或前端目录时必须同步新增 `modules/<name>.md`（K002）。

## CI Verification

GitHub Actions（`.github/workflows/ci.yml`）有 5 个 job，全部不得跳过：

| Job | 内容 |
| --- | --- |
| `lint` | harness check + KB check（PR 时含 K005）+ AGENTS↔CLAUDE 镜像 + engineering-lint |
| `test` | `mvn -B -ntp -q test` |
| `integration` | MySQL 8 + Redis 7 service container，`mvn -Pintegration verify` |
| `smoke` | harness summary + `mvn compile` + 两端 `npm ci/test/build` + `docker compose config -q` |
| `codex-review` | PR-only，依赖前 4 job 通过；blocking-findings 非空则失败 |

`codex-review` 需仓库 secrets `OPENAI_API_KEY`、`OPENAI_RESPONSES_API_ENDPOINT`；
未配置时该 job 会硬失败（不静默跳过）。

辅助 workflow：
- `knowledge-base-sync.yml` —— 每周一自动生成 `SYNC-STATUS.md` 并开 PR。
- `pr-cleanup.yml` —— PR 合并即删除远程分支。

本地等价命令（用于 PR 前自检）：

```bash
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check --base origin/main
python3 scripts/engineering-lint.py
mvn -B -ntp -q test
mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true   # 需本地 MySQL/Redis
cd hmall-web   && npm ci && npm test --if-present && npm run build
cd hmall-admin && npm ci && npm test --if-present && npm run build
docker compose -f docker-compose.yml config -q
```

## Frontend Verification

`hmall-web/` 或 `hmall-admin/` 下、TS 配置、package 元数据改动需：

```bash
cd hmall-web && npm test && npm run build
cd hmall-admin && npm test && npm run build
```

跳过的前端检查需在 `verification.md` 写明原因。

## Secrets Safety

- 不在源码中硬编码密钥/口令/Token。
- `docker-compose.yml`、`application.yaml`、`.env` 中的真实凭据走环境变量或本地未提交文件。
- 任何怀疑泄露的密钥立即轮换并在 `handoff.md` 记录。

## API Contract Safety

- 公共 API 响应封装当前在仓库内不统一：
  - **一致部分**：`CommonExceptionAdvice` 把任何抛出的异常统一包成
    `hm-common.domain.R<Void>` 返回 —— 这是唯一全栈一致的封装入口。
  - **成功响应（混合）**：新版接口（`item-service` / `notify-service` /
    `user-service/{Address,Favorite}Controller` + `UserController` 的
    `sendCode`/`register`/`resetPassword`/`updateProfile` / `trade-service` 的
    `cancel`/`confirm`/`refund`/`admin/*` / `CouponController`）写返 `R<Void>`、
    读返 `*VO`/`PageDTO<T>`/`List<*>`；遗留接口（`pay-service`、
    `cart-service`、`trade-service/OrderController` 的
    `createOrder`/`markOrderPaySuccess`/`updateOrder`、
    `user-service/UserController.{login,deductMoney}`、`AddressController.list()`
    等）多数返 `void`/`Long`/`String`/`*VO`，包含裸返数据库 entity（如
    `List<Address>`）。两个前端 axios `r => r.data` 拦截器对两种形态都兼容。
  - **新增 endpoint 指引**：优先 `R<Void>`(写) + `*VO`/`PageDTO<T>`(读)；
    **不要无前端协调地改既有 endpoint 形态**，否则破坏现网调用。
- 改动接口字段、HTTP 状态码或响应外壳形态视为 breaking change，需 spec +
  调用方迁移说明。
