# Context: Backend Integration Tests

## Objective
为后端 Maven 业务模块建立 Spring Boot Test + SQL 初始化 + Feign Mock 的集成测试体系，
修复阻塞 DDL/Entity 不匹配，接入 jacoco 覆盖率门禁。

## Scope
- In scope:
  - 修复 `pay_order` DDL（6→19 列）与 `OrderLogistics` 字段映射（`@TableField`）
  - Parent POM 新增 `<profile id="integration">`：failsafe + jacoco
  - `hm-common` 新增 `AbstractIT` 基类（`UserContext` 管理）
  - Tier 1：item-service（ItemServiceImpl + ItemReviewServiceImpl），notify-service（NotificationServiceImpl）
  - Tier 2：user-service（UserServiceImpl + AddressServiceImpl + FavoriteServiceImpl），cart-service（CartServiceImpl + @MockBean ItemClient）
  - Tier 3：trade-service（OrderServiceImpl + CouponServiceImpl, @MockBean），pay-service（PayOrderServiceImpl, @MockBean）
  - 删除 hm-service 旧 `ItemServiceImplTest`
- Out of scope:
  - file-service（依赖 MinIO 外部文件系统，缺少 MinIO container)
  - hm-service（聚合演示服务，已由微服务 IT 覆盖）
  - hm-gateway（无数据库依赖，无需集成测试）
  - 前端集成测试

## Related Artifacts
- Spec: docs/superpowers/specs/2026-05-18-hmall-refactor-design.md
- Plan: docs/superpowers/plans/2026-05-18-hmall-refactor-plan.md
- Task #2: docs/agent-harness/tasks/completed/2026-05-26-backend-unit-tests/

## Implementation Approach
- CI 已有 MySQL 8 + Redis 7 service containers，通过 env vars 注入
- 每个服务创建 `application-test.yml`，禁用 Nacos discovery/config
- `@SpringBootTest(properties = {"spring.cloud.bootstrap.enabled=false"})`
- `@Sql` 初始化表结构 + 测试数据，`@Transactional` 保证隔离
- `@MockBean` 替代 Feign 客户端（ItemClient, CartClient, UserClient, OrderClient）

## Safety Constraints
- 不改变任何业务行为
- 所有 81 个现有单元测试继续通过
- DDL 修复仅添加缺失列，不改已有列
