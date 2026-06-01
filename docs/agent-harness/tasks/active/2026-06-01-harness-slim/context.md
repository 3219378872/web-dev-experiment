# Context: Harness Slim

## Objective
简化 agent harness 系统：将 task.yaml 从状态仪表板精简为知识捕获系统，收窄 Codex CI 审查范围。

## Scope
- In scope: TaskStatus 6→3, task.yaml 14→7 字段, handoff 模板精简, Codex prompt 收窄
- Out of scope: 业务代码变更, docker-compose 变更

## Related Artifacts
- Spec: null - 参照 bidking-controller PR #23 模式，不需要独立 spec
- Plan: null - 改动明确，不需要独立 plan

## Likely Files
- `scripts/_agent_harness/model.py`
- `scripts/_agent_harness/checks.py`
- `scripts/_agent_harness/lifecycle.py`
- `scripts/_agent_harness/gc.py`
- `scripts/_agent_harness/templates.py`
- `docs/agent-harness/templates/handoff.md`
- `docs/agent-harness/templates/task-context.md`
- `docs/agent-harness/quality-rules.md`
- `.github/workflows/ci.yml`
- `CLAUDE.md`
- `AGENTS.md`

## Runtime Evidence To Inspect First
- `python3 scripts/agent_harness.py check`
- `python3 scripts/agent_harness.py check --include-completed`

## Safety Constraints
- 向后兼容：旧 task.yaml 静默接受，旧 status 值自动映射
- 不破坏 `agent_harness.py new/check/complete/abandon` CLI
- CI codex-review 仍阻塞合并
