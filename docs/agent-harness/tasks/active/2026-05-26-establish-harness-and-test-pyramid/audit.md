# Audit

按 quality-rules.md 与 README.md 中声明的 harness 能力逐项审计。

| Requirement | Status | Evidence |
| --- | --- | --- |
| harness CLI 支持 new / check / summary / complete / abandon | done | `scripts/_agent_harness/cli.py` 五个 subparser |
| 任务记录含 task.yaml + 四个叙述文件 | done | `scripts/_agent_harness/lifecycle.py::new_task` |
| check 输出稳定 rule ID（H001–H030） | done | `scripts/_agent_harness/checks.py` 与 quality-rules.md 对齐 |
| engineering-lint 校验 md 链接、CLAUDE/AGENTS 同步、harness | done | `scripts/engineering-lint.py` |
| pre-commit 调用 engineering-lint | done | `.pre-commit-config.yaml` 本地 hook |
| CLAUDE.md ⇄ AGENTS.md 字节镜像 | done | `diff -q CLAUDE.md AGENTS.md` 无差异 |
| 单元/集成/冒烟/E2E/视觉整改任务已建立追踪 | done | TaskList #2–#6 全部 pending，等待本任务完成后启动 |
| 全部测试阶段实际执行 | pending | 等 task #2–#6 推进 |
