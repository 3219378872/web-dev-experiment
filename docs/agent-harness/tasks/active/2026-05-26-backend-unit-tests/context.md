# Context: Backend Unit Tests

## Objective
为后端各 Maven 模块建立 JUnit 5 + Mockito 单元测试基础，覆盖
hm-common 公共契约与若干业务服务的纯逻辑路径，并修复阻塞测试运行的编译 bug。

## Scope
- In scope:
  - 修复 `hm-service/src/main/java/com/hmall/service/impl/CartServiceImpl.java` 的 `Long → int` 编译错误
  - hm-common 8 个测试类：R / PageDTO / PageQuery / UserContext /
    CommonExceptionFamily / UserInfoInterceptor / CollUtils / BeanUtils。
  - hm-gateway：JwtTool 全分支覆盖（往返 / null / garbage / 异签名 / 过期 /
    payload 缺失 / 非数字 user）。
  - user-service：JwtTool（与 gateway 镜像） + UserStatus enum。
  - pay-service：PayStatus / PayType enum（含 `equalsValue(null)` 与
    TRADE_SUCCESS=TRADE_FINISHED=3 的设计约定记录）。
  - 标记 hm-service 旧 `ItemServiceImplTest` 为 `@Disabled` 并写明原因
    （依赖运行中的 MySQL/Nacos/Redis，迁到 task #3）。
- Out of scope:
  - 业务 `*ServiceImpl` 中依赖 `ServiceImpl` 链式 query 的逻辑——属 task #3
    集成测试（Testcontainers）覆盖范围。
  - 前端 Vitest（独立任务或后续阶段）。
  - jacoco 覆盖率门禁（后续在 task #3 一并接入）。

## Related Artifacts
- Spec: docs/superpowers/specs/2026-05-18-hmall-refactor-design.md
- Plan: docs/superpowers/plans/2026-05-18-hmall-refactor-plan.md

## Likely Files
- `hm-service/src/main/java/com/hmall/service/impl/CartServiceImpl.java`（bug 修复）
- `hm-common/src/test/java/com/hmall/common/`（8 个新测试类）
- `hm-gateway/src/test/java/com/hmall/gateway/utils/JwtToolTest.java`
- `user-service/src/test/java/com/hmall/utils/JwtToolTest.java`
- `user-service/src/test/java/com/hmall/enums/UserStatusTest.java`
- `pay-service/src/test/java/com/hmall/enums/PayStatusTest.java`
- `pay-service/src/test/java/com/hmall/enums/PayTypeTest.java`
- hm-service src/test/.../ItemServiceImplTest.java（@Disabled，task #3 已迁移至 item-service IT 并删除）

## Runtime Evidence To Inspect First
- PR #2 CI test job 失败日志（hm-service `CartServiceImpl.java:117/127`
  `incompatible types: java.lang.Long cannot be converted to int`），
  这是 task #2 的前置阻塞 bug。
- `mvn -B -ntp test` 当前基线（task #2 之前：除上述编译错误外，仅
  hm-service 有 1 个真集成测试样例 `ItemServiceImplTest` 会因缺 Spring 配置失败）。

## Safety Constraints
- 不改变任何业务行为；CartServiceImpl 修复仅是把已被 `if (count >= 10)` 与
  `return count > 0` 这种语义比较的 `int` 变量改为 `long`，行为等价。
- 不引入新的运行时依赖；测试只用 spring-boot-starter-test 自带的
  JUnit 5 / Mockito / AssertJ / spring-test。
- 不修改 `hm-common.domain.R` / `PageDTO` 的字段或方法签名（公共契约）。
- 不为通过测试而修改业务源码；BeanUtils 三参 copyBean 在 source=null 时
  仍调 convert(null,null) —— 这是发现的潜在 NPE 风险，仅在测试中记录并
  写注释提示，不改实现。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-backend-unit-tests`
- Remote branch: `origin/task/2026-05-26-backend-unit-tests`
- Pull request: none - create after local acceptance and push.

## Open Questions
- 本 PR 与 PR #2（CI workflows）merge 顺序：若 PR #2 先合并，本 PR 的 CI
  job 将自动启用并验证此处所有测试；若本 PR 先合并，需要 rebase。
- jacoco / 覆盖率门禁是否要在本 PR 接入。当前选择：不接入，让 task #3
  统一加 jacoco-maven-plugin + 门禁。
