# Context: Archive Cleanup Task

## Objective
归档 PR #13 留下的 active task `2026-05-27-cleanup-tracked-build-artifacts` 到
completed/，闭合本轮"按流程提交 PR"目标。

## Scope
- In scope:
  - 把 `docs/agent-harness/tasks/active/2026-05-27-cleanup-tracked-build-artifacts/`
    的 task.yaml 终态字段刷为 merged/passed/passed/done，再用
    `agent_harness.py complete` 移到 completed/。
  - 新建本 task 自身的 harness 记录。
- Out of scope:
  - 任何代码 / 配置 / CI workflow / KB 内容改动。
  - 处理本 task 自身归档（递归尾巴 —— active/ 仍会留下本 task 一条作为合理停止点）。

## Related Artifacts
- Spec: none —— 纯归档 chore，无设计决策。
- Plan: none —— 单一 git + python 命令，无多步实施。

## Likely Files
- `docs/agent-harness/tasks/active/2026-05-27-cleanup-tracked-build-artifacts/` →
  `docs/agent-harness/tasks/completed/2026-05-27-cleanup-tracked-build-artifacts/`
- `docs/agent-harness/tasks/active/2026-05-27-archive-cleanup-task/`（本任务记录）

## Runtime Evidence To Inspect First
- `ls docs/agent-harness/tasks/active/` —— 确认只剩 cleanup-tracked-build-artifacts。
- `grep -E "^(status|pull_request):" docs/agent-harness/tasks/active/2026-05-27-cleanup-tracked-build-artifacts/task.yaml`
  —— 确认 status 仍是 pr-open / PR URL 是 #13。

## Safety Constraints
- 只动 harness task records，不改任何代码 / KB / workflow。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-27-archive-cleanup-task`
- Remote branch: `origin/task/2026-05-27-archive-cleanup-task`
- Pull request: 推送后开 PR，URL 回写。

## Open Questions
- none
