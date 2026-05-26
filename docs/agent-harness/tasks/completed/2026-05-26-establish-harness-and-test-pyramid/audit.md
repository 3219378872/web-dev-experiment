# Audit

按 quality-rules.md 与 README.md 中声明的 harness 能力逐项审计。

| Requirement | Status | Evidence |
| --- | --- | --- |
| harness CLI 支持 new / check / summary / complete / abandon | done | `scripts/_agent_harness/cli.py` 五个 subparser |
| 任务记录含 task.yaml + 四个叙述文件 | done | `scripts/_agent_harness/lifecycle.py::new_task` |
| harness check 输出稳定 rule ID（H001–H030） | done | `scripts/_agent_harness/checks.py` 与 quality-rules.md 对齐 |
| knowledge_base CLI 支持 check / sync-report | done | `scripts/_knowledge_base/cli.py` |
| KB check 输出稳定 rule ID（K001–K006） | done | `scripts/_knowledge_base/checks.py` |
| 全部 11 个 maven 模块 + 2 个前端目录均有 modules 页面 | done | `docs/knowledge-base/modules/*.md` 13 个 |
| 至少一个跨服务 flow 页面 | done | order-checkout-flow + auth-and-gateway-flow |
| INDEX.md 引用全部 KB 页面 | done | K006 通过 |
| engineering-lint 校验 md 链接 + CLAUDE/AGENTS 同步 + harness + KB | done | `scripts/engineering-lint.py` 调用全部 |
| pre-commit 调用 engineering-lint | done | `.pre-commit-config.yaml` 本地 hook |
| CLAUDE.md ⇄ AGENTS.md 字节镜像 | done | `diff -q CLAUDE.md AGENTS.md` 无差异 |
| 单元/集成/冒烟/E2E/视觉整改任务已建立追踪 | done | TaskList #2–#6 全部 pending，等待本任务完成后启动 |
| 全部测试阶段实际执行 | pending | 等 task #2–#6 推进 |
