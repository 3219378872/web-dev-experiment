# Handoff: Backend Integration Tests

## Status
Implementing —— 本地 compile + unit tests 通过；待 push 与开 PR；集成测试需 MySQL/Redis。

## Files Changed

DDL/Entity 修复（2 个）：
- `docs/sql/init-all-tables.sql`（pay_order 完整 DDL）
- `trade-service/src/main/java/com/hmall/domain/po/OrderLogistics.java`（@TableField 映射）

Parent POM（1 个）：
- `pom.xml`（integration profile + failsafe + jacoco）

共享基类（1 个）：
- `hm-common/src/test/java/com/hmall/common/it/AbstractIT.java`（保留未使用）

集成测试新增（~25 个文件）：
- item-service: application-test.yml, sql/schema.sql, sql/data-item.sql, sql/data-review.sql, ItemServiceImplIT.java, ItemReviewServiceImplIT.java
- notify-service: application-test.yml, sql/schema.sql, sql/data-notification.sql, NotificationServiceImplIT.java
- user-service: application-test.yml, sql/schema.sql, sql/data-user.sql, sql/data-address.sql, UserServiceImplIT.java, AddressServiceImplIT.java, FavoriteServiceImplIT.java, hmall.jks (copy)
- cart-service: application-test.yml, sql/schema.sql, CartServiceImplIT.java
- trade-service: application-test.yml, sql/schema.sql, sql/data-order.sql, OrderServiceImplIT.java, CouponServiceImplIT.java
- pay-service: application-test.yml, sql/schema.sql, sql/data-payorder.sql, PayOrderServiceImplIT.java

删除（1 个）：
- hm-service src/test/.../ItemServiceImplTest.java（由 item-service IT 覆盖，已删除）

Harness 任务记录：
- `docs/agent-harness/tasks/active/2026-05-26-backend-integration-tests/`

## Commands Run
- `mvn -B -ntp -DskipTests compile`（全模块）
- `mvn -B -ntp -q test`（76 tests pass）

## Known Risks
- 集成测试需要运行中的 MySQL + Redis（本地或 CI 容器）
- `AbstractIT` 类在 hm-common test scope，其他模块无法引用，各 IT 类已内联 UserContext 管理
- PayApplyDTO 和 PayOrderFormDTO 使用 @Builder 模式，setter 不可用
- notify-service 有 spring-cloud-starter-bootstrap 依赖，需 `spring.cloud.bootstrap.enabled=false`
- jacoco coverage 检查设为 BUNDLE/INSTRUCTION/0.50（保守值），CI 首次运行可能触发调整

## Next Action
1. push `task/2026-05-26-backend-integration-tests`
2. `gh pr create`，记录 PR URL
3. 等 CI integration job 通过（需要 MySQL + Redis service containers）
4. merge 后 task → done 归档；启动 task #4（smoke tests）

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-backend-integration-tests`
- Pull request: none - create after local acceptance and push
