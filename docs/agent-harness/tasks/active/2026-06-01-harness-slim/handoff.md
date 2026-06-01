# Handoff: Harness Slim

## Status
PR #22 已创建。参照 bidking-controller PR #23 的 harness 瘦身模式。

## Files Changed
- `scripts/_agent_harness/model.py` — TaskStatus 6→3, 移除 7 瞬态字段, 向后兼容
- `scripts/_agent_harness/checks.py` — 移除 H006/H008/H010/H011/H012
- `scripts/_agent_harness/lifecycle.py` — 适配简化模型
- `scripts/_agent_harness/gc.py` — 适配简化状态
- `scripts/_agent_harness/templates.py` — 移除 remote_branch
- `docs/agent-harness/templates/handoff.md` — 移除 Branch/PR/CI 段
- `docs/agent-harness/templates/task-context.md` — 移除 Branch/PR 段
- `docs/agent-harness/quality-rules.md` — 移除 H006/H008/H010/H011/H012
- `.github/workflows/ci.yml` — Codex prompt 收窄
- `CLAUDE.md` / `AGENTS.md` — 同步更新

## Commands Run
- `cmp CLAUDE.md AGENTS.md` → byte equal
- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/agent_harness.py check --include-completed` → passed
- `python3 scripts/knowledge_base.py check` → passed
- `python3 scripts/engineering-lint.py` → passed

## Known Risks
- 旧 task.yaml 向后兼容：legacy 字段静默接受，旧状态值自动映射
- CI codex-review prompt 同步收窄（与 harness 规则改动同一 PR），避免鸡生蛋问题

## Next Action
等待 CI 通过后合并。

## Worktree Or Branch
- `task/2026-06-01-harness-slim`
