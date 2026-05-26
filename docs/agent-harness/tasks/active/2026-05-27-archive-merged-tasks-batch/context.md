# Context: Archive Merged Tasks Batch

## Objective
一次性把 7 个 PR 已 merge 但 active task records 未归档的任务挪到 completed/，
让 active/ 只反映真正在跑或在 PR 阶段的工作，并补齐每个任务 task.yaml 的终态字段
（status/ci_status/codex_review/remote_cleanup）与最小可接受 verification 证据。

## Scope
- In scope:
  - 把下列已 merge 的 active 任务 task.yaml 写到终态并 `agent_harness.py complete`
    迁到 completed/：
    - `2026-05-26-finalize-harness-bootstrap-and-add-ci`（PR #2）
    - `2026-05-26-backend-unit-tests`（PR #3）
    - `2026-05-26-backend-integration-tests`（PR #4）
    - `2026-05-26-smoke-tests`（PR #5）
    - `2026-05-26-playwright-e2e-tests`（PR #6）
    - `2026-05-26-visual-regression-fixes`（PR #8）
    - `2026-05-26-align-codex-naming`（PR #11）
  - 给 `playwright-e2e-tests` 与 `visual-regression-fixes` 补本地可执行的最小
    passing 行（前者 `npm ci` + `playwright test --list`，后者 `npm ci`+`npm run build`），
    满足 `agent_harness.py complete` 要求的「verification.md 必须含至少一行 passed」。
  - 本任务自身的 harness 记录（本 commit 同包含）。
- Out of scope:
  - 任何代码改动（无 .java / .vue / .ts / 业务配置变更）。
  - 重跑已 merged PR 中的测试，或重新验证其内容正确性（已由当时的 PR CI 覆盖）。
  - 重写 `node_modules` 之类历史遗留的版本控制污染（独立任务）。

## Related Artifacts
- Spec: none —— 纯归档清理，无设计决策。
- Plan: none —— 7 个 task 的归档动作机械且独立。

## Likely Files
- `docs/agent-harness/tasks/active/*/task.yaml`（7 份 → 终态字段更新）
- `docs/agent-harness/tasks/completed/2026-05-26-playwright-e2e-tests/verification.md`（补 passing 行）
- `docs/agent-harness/tasks/completed/2026-05-26-visual-regression-fixes/verification.md`（补 passing 行）
- 7 个 `docs/agent-harness/tasks/active/<slug>/` 目录 → 整体 rename 到
  `docs/agent-harness/tasks/completed/<slug>/`（由 `agent_harness.py complete`
  自动执行）。
- `docs/agent-harness/tasks/active/2026-05-27-archive-merged-tasks-batch/`（本任务记录）

## Runtime Evidence To Inspect First
- `gh pr list --state merged --json number,mergedAt,title` —— 确认 7 个 PR 已实际 merged。
- `grep -E "^(status|pull_request):" docs/agent-harness/tasks/active/*/task.yaml`
  —— 看哪些任务的 task.yaml 还停在 `implementing` / `pr-open` 状态。

## Safety Constraints
- 不改动业务代码、配置、secrets、workflow yml。
- 不删除任何 PR/branch；归档只对 task records 起作用。
- `complete` 命令要求 task.yaml status == `merged`、verification.md ≥1 行 passed、
  audit.md 有数据；若任一不满足须先补齐再 complete，禁绕过。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-27-archive-merged-tasks-batch`
- Remote branch: `origin/task/2026-05-27-archive-merged-tasks-batch`
- Pull request: 推送后创建，URL 写回本文件与 task.yaml。

## Open Questions
- none
