# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 系统架构图 + 部署拓扑 | done | `01-system-architecture.md` 两张图 |
| 模块/Feign 调用依赖图 | done | `02-module-dependencies.md` 两张图 |
| 核心业务时序图（鉴权/下单/支付） | done | `03-sequence-diagrams.md` 三张 sequenceDiagram |
| 数据模型 E-R 概览 | done | `04-data-model.md` 一张 erDiagram，18 实体即全部 18 表（含独立的 notifications/uploads/banners） |
| 按真实代码绘图，标注未接入中间件 | done | README "现状 vs CLAUDE.md 差异"专节 |
| codex-review 第一轮 3 findings 已修正 | done | ①compose 实际容器化全部 Java 服务（改 README + 01）②Feign 9 个客户端、删除捏造的 user-service 边（改 02）③E-R 补齐 18 实体（改 04） |
| codex-review 第二轮 1 finding 已修正 | done | OrderClient 目标 `order-service` 全仓无服务注册、路径 `/users`≠trade 的 `/orders`，pay→trade 回写实为失效调用；02 改为红色虚线+失效说明，纠正此前"指向 trade-service"的错误断言 |
| codex-review 第三轮 1 finding 已修正 | done | 支付失败路径画错：`tryPayOrderByBalance` 为 `@Transactional` 无 try/catch、OrderClient 无 fallback，步骤5 抛异常会回滚整个本地事务（pay_order 撤销）、已扣余额不补偿、用户收到错误。03 支付时序重画为真实失败流程并给出结论；移除"pay_order 成功/用户支付成功"的错误描述 |
| 不改代码、不改 CLAUDE/AGENTS | done | 变更仅 docs/structure 与本任务记录 |
| 无需独立 spec | not applicable | 纯文档任务，见 task.yaml spec_waiver |
