# Context: Enhance Unit Tests

## Objective

为 user-service 和 pay-service 增强单元测试，使其满足 JaCoCo 70% 分支覆盖率门控。

## Scope

- In scope: user-service 的 3 个 service.impl 类、pay-service 的 1 个 service.impl 类。
- Out of scope: notify-service（已被根 POM 排除在 JaCoCo 检查外）、hm-common/hm-api/hm-gateway（无 service.impl）。

## Related Artifacts

- Spec: none — 测试增强任务，无需独立 spec。
- Plan: none — 测试增强任务，无需独立 plan。

## Likely Files

- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`
- `user-service/src/test/java/com/hmall/service/impl/AddressServiceImplTest.java`
- `user-service/src/test/java/com/hmall/service/impl/FavoriteServiceImplTest.java`
- `user-service/src/test/java/com/hmall/UserServiceTestBase.java`
- `user-service/src/test/resources/application.yml`
- `pay-service/src/test/java/com/hmall/service/impl/PayOrderServiceImplTest.java`
- `pay-service/src/test/java/com/hmall/PayServiceTestBase.java`
- `pay-service/src/test/resources/application.yml`
- `user-service/pom.xml`（JaCoCo 门控 0.00 → 0.70）
- `pay-service/pom.xml`（JaCoCo 门控 0.00 → 0.70）

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token。
- 不破坏现有公共 API 响应封装。
- 测试使用 H2 内存数据库，不依赖外部服务。

## Worktree Or Branch

- `feat/enhance-tests`
