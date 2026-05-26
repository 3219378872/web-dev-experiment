# Context: Cleanup Tracked Build Artifacts

## Objective
把历史污染进 git 的 25124 个 build 产物（hmall-web/hmall-admin 的 node_modules + dist
两端）批量从 git tracking 中移除（保留磁盘文件不删），让仓库 .gitignore 真正生效，
同时归档 PR #12 留下的 active task 自身，完成全流程闭环。

## Scope
- In scope:
  - `git rm --cached -r hmall-web/node_modules hmall-admin/node_modules hmall-web/dist hmall-admin/dist`
    （只移除 git index，工作目录文件保留 —— 重新 npm ci/build 后产物原样）。
  - 归档 `docs/agent-harness/tasks/active/2026-05-27-archive-merged-tasks-batch` 到
    `completed/`（task.yaml 终态字段 + agent_harness.py complete）。
  - 新建本 task 自身的 harness 记录。
- Out of scope:
  - 改动 `.gitignore`（已含 `node_modules/`、两端 `dist/`、各 `node_modules/`、
    `e2e/node_modules/` 等条目；问题不在 ignore 表达，而在历史 tracked）。
  - 任何业务代码 / Java / Vue / CI workflow 改动。

## Related Artifacts
- Spec: none —— 纯污染清理，无设计决策。
- Plan: none —— 单条 git 命令 + 一次归档。

## Likely Files
- ~25124 个 deletion：`hmall-web/node_modules/**`、`hmall-admin/node_modules/**`、
  `hmall-web/dist/**`、`hmall-admin/dist/**`。
- `docs/agent-harness/tasks/active/2026-05-27-archive-merged-tasks-batch/` → 整体迁
  `completed/`。
- `docs/agent-harness/tasks/active/2026-05-27-cleanup-tracked-build-artifacts/`（本任务记录）。

## Runtime Evidence To Inspect First
- `git ls-files | grep -E '(node_modules|dist)' | wc -l` —— 数 25076（初始） / 0（清理后）。
- `cat .gitignore | head -30` —— 确认 ignore 条目已存在。

## Safety Constraints
- 不在 PR 同时改 `.gitignore`（已经在），避免范围扩散。
- 不动磁盘文件，只移 git index：`git rm --cached -r ...`。
- 不影响 CI smoke job 的两端 build（npm ci 重新生成 node_modules 即可）。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-27-cleanup-tracked-build-artifacts`
- Remote branch: `origin/task/2026-05-27-cleanup-tracked-build-artifacts`
- Pull request: 推送后开 PR，URL 回写。

## Open Questions
- none
