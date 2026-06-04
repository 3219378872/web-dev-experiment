# Context: Phase 4 — notify-service FAQ + Customer Message Placeholder

## Objective

实现 Backend API Fulfillment spec Phase 4（notify-service）：新增 `GET /faqs` 端点返回
FAQ 列表，确认客服消息收发端点已存在（占位标志保留）。

## Scope

- In scope:
  - `faq` 表 DDL（`docs/sql/notify-service-faq.sql`）
  - `Faq` PO + `FaqVO` + `FaqMapper` + `IFaqService` / `FaqServiceImpl` + `FaqController`
  - 单元测试（`FaqServiceImplTest`），目标 80% 分支覆盖
  - 测试 schema 更新（H2 `faq` 表）
  - 知识库页面更新（`notify-service.md`）
  - harness 任务记录
- Out of scope:
  - 客服 WebSocket 实时系统（占位，前端轮询消费现有端点）
  - Gateway 路由变更（`/faqs` 走现有转发规则）
  - 其他 Phase 的端点

## Related Artifacts

- Spec: committed at d7b580c on `feat/backend-api-fulfillment` branch (Phase 4 section: GET /faqs + customer message placeholder)
- Plan: `docs/superpowers/plans/2026-06-04-phase4-notify-service.md`

## Likely Files

- `notify-service/src/main/java/com/hmall/notify/domain/po/Faq.java`
- `notify-service/src/main/java/com/hmall/notify/domain/vo/FaqVO.java`
- `notify-service/src/main/java/com/hmall/notify/mapper/FaqMapper.java`
- `notify-service/src/main/java/com/hmall/notify/service/IFaqService.java`
- `notify-service/src/main/java/com/hmall/notify/service/impl/FaqServiceImpl.java`
- `notify-service/src/main/java/com/hmall/notify/controller/FaqController.java`
- `notify-service/src/test/java/com/hmall/notify/service/impl/FaqServiceImplTest.java`
- `notify-service/src/test/resources/schema.sql`
- `notify-service/src/test/resources/sql/schema.sql`
- `docs/sql/notify-service-faq.sql`
- `docs/knowledge-base/modules/notify-service.md`

## Safety Constraints

- 不破坏现有端点形态（`/messages`、`/admin/messages` 不变）
- 新表用 SQL 迁移脚本，不改写既有 DDL
- 写操作 `R<Void>`、读操作 `List<FaqVO>` 遵循仓库约定
- 不硬编码密钥
