# Agent Harness Garbage Collection

垃圾回收让 agent 记录系统保持有用，而非变成又一棵陈旧的文档树。

每个任务结束后、长时间中断后、较大合并前手动跑一次。

## 检查项

- 活动任务超出预期工期且无近期证据。
- 已完成任务仍在 `docs/agent-harness/tasks/active/` 下。
- 有 spec 但无 plan 且作用域暗示需要实施。
- plan 无任何活动或已完成任务引用。
- audit 没有验证证据。
- verification 没有具体命令输出或显式跳过原因。
- `AGENTS.md` 与 `CLAUDE.md` 漂移。
- 任务文档里残留模板占位符。

## 命令

```bash
python3 scripts/agent_harness.py summary
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check
python3 scripts/knowledge_base.py sync-report --output /tmp/kb-sync.md
diff -u AGENTS.md CLAUDE.md
```

`summary` 仅建议性、退出码总为 0。`check` 严格、不合规则非零退出。

## 清理策略

宁可修复任务记录也不要直接删除。验证与审计现状对齐之前不要把任务挪到 `completed/`。
若任务被放弃，在 `handoff.md` 写明原因后再挪到 `completed/`，避免后续 agent 误把它当活动任务。
