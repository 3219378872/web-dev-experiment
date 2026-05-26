# Knowledge Base

为 agent 与人类提供 hmall 项目当前状态的策展式描述。
每个模块在 `modules/` 下一页，跨模块数据流与主运行时流程在 `flows/` 下，
完整索引在 [INDEX.md](INDEX.md)。

本知识库不替代 `docs/superpowers/specs/`（时点设计）或
`docs/agent-harness/`（任务执行记录）。它是面向"项目当前如何工作"的上下文层。

## Page Frontmatter

每页都带 YAML frontmatter：

    ---
    title: trade-service
    tracks:
      - trade-service/
    last_synced_commit: <sha>
    last_synced_date: YYYY-MM-DD
    sync_note: ""
    ---

- `title` —— 页面标题；模块页对齐模块名。
- `tracks` —— 本页负责覆盖的仓库路径列表（目录加 `/`）。
- `last_synced_commit` / `last_synced_date` —— 最近一次对齐 commit 与日期。
- `sync_note` —— 可选一行说明；作为轻量豁免使用。

## Module Pages

每个模块页包含固定章节：职责 / 公开接口与契约 / 上游 / 下游 / 关键文件 / 注意事项与陷阱。

## 保持新鲜

`python3 scripts/knowledge_base.py check` 校验结构与链接，是阻塞性 lint。
规则 K005 是"共变"检查：PR 改动了某页 tracks 路径，就必须同时改这页。
如果不需要内容变化，把 `last_synced_commit` 推到当前 HEAD、在 `sync_note`
写明原因 —— 这是轻量豁免。

新增后端 Maven 模块或前端目录必须在同一 PR 里加 `modules/` 页面（规则 K002）。

`python3 scripts/knowledge_base.py sync-report --output <path>` 生成漂移报告，
便于定期巡检。
