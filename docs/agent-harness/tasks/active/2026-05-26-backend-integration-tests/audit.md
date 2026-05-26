# Audit

按 spec `2026-05-18-hmall-refactor-design.md` 的"测试金字塔"章节与
`docs/agent-harness/quality-rules.md` Backend Verification 要求逐项审计。

| Requirement | Status | Evidence |
| --- | --- | --- |
| 修复 pay_order DDL（6→19 列）| done | `init-all-tables.sql` 更新 |
| 修复 OrderLogistics @TableField 映射 | done | `@TableField("phone"/"district"/"detail")` 三处 |
| Parent POM 新增 integration profile | done | failsafe + jacoco 配置 |
| item-service 集成测试 | done | ItemServiceImplIT(4) + ItemReviewServiceImplIT(2) |
| notify-service 集成测试 | done | NotificationServiceImplIT(2) |
| user-service 集成测试 | done | UserServiceImplIT(6) + AddressServiceImplIT(2) + FavoriteServiceImplIT(4) |
| cart-service 集成测试 | done | CartServiceImplIT(4, @MockBean ItemClient) |
| trade-service 集成测试 | done | OrderServiceImplIT(7, @MockBean) + CouponServiceImplIT(6) |
| pay-service 集成测试 | done | PayOrderServiceImplIT(4, @MockBean) |
| 删除旧 ItemServiceImplTest | done | item-service IT 已覆盖 |
| 现有 76 个单测继续通过 | done | `mvn -q test` 全绿 |
| 集成测试不依赖外部基础设施 | done | 通过 SRPING_DATASOURCE_URL/USERNAME/PASSWORD + SPRING_REDIS_HOST/PORT env vars |
| 未修改公共 API 契约 | done | 仅 test 资源 + DDL 修复 |
| jacoco 覆盖率门禁 | done | 50% instruction coverage (BUNDLE 级别) |
| file-service 集成测试 | deferred | 依赖 MinIO 外部文件系统，缺少 MinIO container |
| hm-service 集成测试 | n/a | 聚合演示服务，已由微服务 IT 覆盖 |
