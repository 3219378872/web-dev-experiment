# Audit

按 `quality-rules.md` 的 CI Verification 小节与 task #1 收尾要求逐条审计。

| Requirement | Status | Evidence |
| --- | --- | --- |
| task #1 任务记录移到 `completed/` | done | `docs/agent-harness/tasks/completed/2026-05-26-establish-harness-and-test-pyramid/` |
| task #1 task.yaml 标 `merged`、含 PR URL、`remote_cleanup: done` | done | `task.yaml` 字段 |
| 5 个 CI job（lint/test/integration/smoke/codex-review）全部配置 | done | `.github/workflows/ci.yml` |
| codex-review 需要 secrets 且未配置时硬失败 | done | `ci.yml::codex-review.Verify Codex secrets` 步骤 |
| `lint` job 包含 harness/KB/AGENTS↔CLAUDE/engineering-lint | done | `.github/workflows/ci.yml::lint` |
| `integration` job 提供 MySQL+Redis service containers | done | `.github/workflows/ci.yml::integration.services` |
| `smoke` job 覆盖两个前端 + docker compose 配置渲染 | done | `.github/workflows/ci.yml::smoke` |
| `knowledge-base-sync.yml` 每周一自动开漂移 PR | done | cron `0 6 * * 1` |
| `pr-cleanup.yml` PR 合并后删合并分支 | done | `pull_request.closed + merged==true` |
| `CLAUDE.md` ⇄ `AGENTS.md` 同步含 CI 章节 | done | `diff -q CLAUDE.md AGENTS.md` 无差异 |
| `quality-rules.md` CI Verification 小节列出全部 5 job | done | `docs/agent-harness/quality-rules.md` |
| PR 在 main 上 merge 并删除远程分支 | done | PR #2 merged at 2026-05-26T08:01:11Z, remote branch auto-deleted by pr-cleanup.yml |
