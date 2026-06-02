# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 系统架构图 + 部署拓扑 | done | `01-system-architecture.md` 两张图 |
| 模块/Feign 调用依赖图 | done | `02-module-dependencies.md` 两张图 |
| 核心业务时序图（鉴权/下单/支付） | done | `03-sequence-diagrams.md` 三张 sequenceDiagram |
| 数据模型 E-R 概览 | done | `04-data-model.md` 一张 erDiagram，18 实体即全部 18 表（含独立的 notifications/uploads/banners） |
| 按真实代码绘图，标注未接入中间件 | done | README "现状 vs CLAUDE.md 差异"专节 |
| codex-review 3 条 blocking findings 已修正 | done | ①compose 实际容器化全部 Java 服务（改 README + 01 部署拓扑）②Feign 9 个客户端、真实调用图仅 cart/trade/pay 三方、删除捏造的 user-service 边（改 02）③E-R 补齐 18 实体（改 04） |
| 不改代码、不改 CLAUDE/AGENTS | done | 变更仅 docs/structure 与本任务记录 |
| 无需独立 spec | not applicable | 纯文档任务，见 task.yaml spec_waiver |
