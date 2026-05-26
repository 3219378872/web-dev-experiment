# Handoff: Cleanup Tracked Build Artifacts

## Status
PR-open —— PR #13 已创建（https://github.com/3219378872/web-dev-experiment/pull/13），
remote branch `origin/task/2026-05-27-cleanup-tracked-build-artifacts` 已推送，CI/
codex-review 迭代中（已应用 1 轮 codex 反馈：K005 KB 同步 + 本次反馈的 verification 完整化 + 归档 handoff 终态对齐）。

## Files Changed
- 25124 个 deletion：`hmall-web/node_modules/**`、`hmall-admin/node_modules/**`、
  `hmall-web/dist/**`、`hmall-admin/dist/**`（git index 移除，磁盘文件保留）。
- `docs/agent-harness/tasks/active/2026-05-27-archive-merged-tasks-batch/` →
  `docs/agent-harness/tasks/completed/2026-05-27-archive-merged-tasks-batch/`
  （task.yaml 终态字段写入 + `agent_harness.py complete` 移动）。
- `docs/agent-harness/tasks/active/2026-05-27-cleanup-tracked-build-artifacts/`（本任务记录）。

## Commands Run
- `git checkout -b task/2026-05-27-cleanup-tracked-build-artifacts`
- `git rm --cached -r hmall-web/node_modules hmall-admin/node_modules hmall-web/dist hmall-admin/dist`
- `python3 scripts/agent_harness.py new cleanup-tracked-build-artifacts --date 2026-05-27`
- `python3 scripts/agent_harness.py complete 2026-05-27-archive-merged-tasks-batch`
- `python3 scripts/agent_harness.py check`
- `python3 scripts/knowledge_base.py check`
- `python3 scripts/engineering-lint.py`
- `diff -u AGENTS.md CLAUDE.md`

## Known Risks
- PR diff 文件数极大（25K+）—— GitHub Web UI 渲染可能慢；reviewer 应主要看
  `docs/agent-harness/...` 与本 PR 描述。
- `git rm --cached -r` 不会删除工作目录中的 node_modules / dist；下一次 git pull
  后无 conflict（因为已 ignore），但任何当前持有该 branch 的协作者需要重新
  `npm ci` 才能跑 CI 等价的 build。

## Next Action
> **Progress note**: 步骤 1（branch push）和 2（PR #13 创建）已完成；当前处于步骤 3
> CI/codex-review 迭代中。

3. 等 CI 5/5 jobs 全绿 + codex-review `blocking findings: none`（迭代中）。
4. squash merge + `--delete-branch`，本任务由下一次 chore PR 归档（与 PR #12/#13
   同样的递归模式 —— 下一个 PR 顺手把本 task 移到 completed/）。

## Worktree Or Branch
- `task/2026-05-27-cleanup-tracked-build-artifacts`

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-27-cleanup-tracked-build-artifacts`
- Remote branch: `origin/task/2026-05-27-cleanup-tracked-build-artifacts`
- Pull request: https://github.com/3219378872/web-dev-experiment/pull/13（已创建）
- Remote branch cleanup: pending until PR #13 merged (PR-cleanup workflow 会自动删除)

## CI And Review
- CI status: 推送后由 GitHub Actions 触发；本 PR 不动 backend / workflow / 业务前端代码，预期 5/5 jobs green。
- Codex review: 推送后等 codex 评审；本 PR 是纯 chore（untrack + 归档），预期 `blocking findings: none`。
