# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | pass | `agent harness check passed` |
| `python3 scripts/knowledge_base.py check` | pass | `knowledge base check passed`（docs/structure 不在 tracks，K005 不触发） |
| `python3 scripts/engineering-lint.py` | pass | harness + KB 均 passed |
| `mmdc` 渲染全部 9 张 Mermaid 图 | pass | 5 个文件逐一渲染无 parse/syntax error（01:2、02:2、03:3、04:1、README:1） |
| 事实抽样核对 | pass | `AuthGlobalFilter` 注入 `user-info`/`role-info` 头（L57-60）；`PayOrderServiceImpl` `order.setStatus(2)`+`setPayTime`+`orderClient.updateById`（L64-68） |
