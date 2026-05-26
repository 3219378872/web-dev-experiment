# Agent Harness

仓库本地的 agent-first 工程记录系统。它不替代 `docs/superpowers/specs/` 或
`docs/superpowers/plans/`，而是围绕这些设计/计划记录执行生命周期，让后续 agent 或
人类无需翻阅聊天记录就能理解当前在做什么。

## 何时使用

为以下"重要"工作创建/更新一条 harness 任务记录：

- 行为变更（业务逻辑、API 契约、订单/支付流程）
- 跨多模块/微服务的重构
- 前端 hmall-web / hmall-admin 的功能新增或视觉整改
- 运行时调试（容器、网关、Nacos、Seata）
- 安全敏感改动（鉴权、密钥、SQL、跨服务调用）
- 引入 spec 或 implementation plan 的工作

非常小的纯文档编辑可省略，但要在 commit message 里写明原因。

## 任务记录

活动任务位于：

```text
docs/agent-harness/tasks/active/
```

已完成任务移到：

```text
docs/agent-harness/tasks/completed/
```

每条记录包含四个叙述文件 + 一个结构化文件：

- `task.yaml`：机器可读元数据（见下文）。
- `context.md`：目标、范围、相关 spec/plan、相关文件、需优先查看的运行证据、安全约束、待定问题。
- `verification.md`：运行过的命令、结果、证据、跳过的命令、环境阻塞。
- `audit.md`：逐条需求 vs spec 的复核或显式无 spec 的理由。
- `handoff.md`：当前状态、改动文件、已执行命令、风险、下一步、worktree/分支状态。

## task.yaml 字段

- `slug`：完整 slug（`YYYY-MM-DD-short-slug`）。
- `status`：`created` / `implementing` / `pr-open` / `merged` / `done` / `abandoned`。
- `base_branch`、`task_branch`、`remote_branch`。
- `pull_request`：PR URL，或 `null` 配合 `pr_waiver` 原因。
- `ci_status`、`codex_review`：`not-run` / `pending` / `passed` / `failed`。
- `remote_cleanup`：`pending` / `done` / `not-applicable`。
- `spec`、`plan`：`docs/superpowers/` 下路径，或 `null` 配合 `spec_waiver`/`plan_waiver`。

四个 `.md` 文件保持纯叙述；checker 只从 `task.yaml` 读结构化规则。

## 交付流程

每个重要任务必须按"分支→PR"循环走：

1. 从 `main` 切 `task/YYYY-MM-DD-short-slug` 任务分支。
2. 创建/更新 active 任务记录。
3. 在任务分支实现。
4. 本地跑验收，把命令与结果记在 `verification.md`。
5. 推送 `origin/task/YYYY-MM-DD-short-slug`。
6. 开 PR，把 URL 记在 `handoff.md`。
7. CI 通过、review 通过。
8. 合并后删除远程分支，记录清理证据，再把任务挪到 `completed/`。

## CLI

```bash
python3 scripts/agent_harness.py new <slug>
python3 scripts/agent_harness.py check
python3 scripts/agent_harness.py check --include-completed
python3 scripts/agent_harness.py summary
python3 scripts/agent_harness.py complete <full-slug>
python3 scripts/agent_harness.py abandon <full-slug> --reason "<reason>"
```

`check` 是只读的；engineering-lint 与 pre-commit 会调用它。

## 与 superpowers 文档的关系

`docs/superpowers/specs/` 放设计、`docs/superpowers/plans/` 放多步实施计划。
harness 任务在 `context.md` 链接它们，并用 `audit.md` 证明实现满足设计。

## 完成规则

未把验证证据与 spec 审计都更新到位前，不要把任务从 `active/` 挪到 `completed/`。
某个 focused 测试绿了不能替代逐条需求审计（当存在 spec 时）。
