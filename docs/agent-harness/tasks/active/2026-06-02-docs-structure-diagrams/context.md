# Context: Docs Structure Diagrams

## Objective
在 `docs/structure/` 下用 Mermaid 嵌入 Markdown，补齐系统架构、部署拓扑、模块/Feign 调用依赖、核心业务时序、数据模型 E-R 五类可视化文档。

## Scope
- In scope: 新建 `docs/structure/` 5 个 Markdown 文档（纯文档，Mermaid 图）。
- Out of scope: 任何代码改动；修改 `CLAUDE.md`/`AGENTS.md`；改动 `docs/knowledge-base/`。

## Related Artifacts
- Spec: none —— 纯文档任务，无 API/数据/行为变更，无需独立 spec。
- Plan: `/home/dev/.claude/plans/docs-structure-compiled-scott.md`（已批准）。

## Likely Files
- `docs/structure/README.md`
- `docs/structure/01-system-architecture.md`
- `docs/structure/02-module-dependencies.md`
- `docs/structure/03-sequence-diagrams.md`
- `docs/structure/04-data-model.md`

## Runtime Evidence To Inspect First
- `scripts/init-nacos-routes.sh`（gateway 路由前缀）
- `docker-compose.yml`（真实编排的中间件）
- `hm-api/.../client/`（Feign 客户端）
- `trade-service` OrderServiceImpl / `pay-service` PayOrderServiceImpl（下单、支付链）
- `hm-gateway` AuthGlobalFilter / `hm-common` UserInfoInterceptor（鉴权透传）
- `docs/sql/init-all-tables.sql`（18 表 E-R）

## Safety Constraints
- 不提交任何密钥。
- 不破坏 `hm-common` 的 `R<T>`/`PageDTO` 响应封装（本任务不碰代码）。
- 图须反映真实代码：Seata/RabbitMQ/MinIO/ES 在仓库中声明但未接入，须明确标注，勿照搬 CLAUDE.md 的叙述。

## Worktree Or Branch
- `task/2026-06-02-docs-structure-diagrams`

## Open Questions
- none（格式 Mermaid、范围四类图全做、中文，均已与用户确认）
