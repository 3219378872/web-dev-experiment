# Handoff: Backend Integration Tests

## Status
Done — merged via PR #4 at 2026-05-26T08:57:28Z; remote branch deleted by pr-cleanup.yml; task archived to completed/ by PR #12.

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
> **Final note (post-merge)**: 所有 1–5 步骤已实际执行：PR #4 已 merge (2026-05-26T08:57:28Z); remote branch 已由 pr-cleanup.yml 自动删除; 本任务已由 PR #12 归档到 completed/。

（任务已 done；原 Next Action 数字步骤已全部执行完毕，详见 Status 行与 Branch And PR 节。）

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-backend-integration-tests`
- Remote branch: `origin/task/2026-05-26-backend-integration-tests` (deleted post-merge by pr-cleanup.yml)
- Pull request: https://github.com/3219378872/web-dev-experiment/pull/4 (merged at 2026-05-26T08:57:28Z)
- Remote branch cleanup: done (auto-deleted by pr-cleanup.yml after PR #4 merged).

## CI And Review
- CI status: passed —— PR #4 merge commit 上的 main push CI 5/5 jobs 全绿。
- Codex review: passed (blocking findings: none) on PR #4.
