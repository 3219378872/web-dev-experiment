# Audit

| # | Requirement | Status | Evidence |
| --- | --- | --- | --- |
| 1 | TaskStatus 6→3 (active/done/abandoned) | done | model.py |
| 2 | task.yaml 移除 7 瞬态字段 | done | model.py |
| 3 | 旧状态值自动映射 (created→active 等) | done | model.py _legacy_active |
| 4 | 旧字段静默接受 | done | model.py _LEGACY_FIELDS |
| 5 | 检查规则移除 H006/H008/H010/H011/H012 | done | checks.py |
| 6 | handoff 模板精简 | done | templates/handoff.md |
| 7 | task-context 模板精简 | done | templates/task-context.md |
| 8 | quality-rules.md 更新 | done | quality-rules.md |
| 9 | Codex prompt 收窄 | done | ci.yml |
| 10 | CLAUDE.md/AGENTS.md 同步 | done | byte equal |
| 11 | 向后兼容 check --include-completed | done | passed |
| 12 | CLI new/check/summary 正常 | done | all passed |
