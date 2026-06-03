# Handoff: Enhance Unit Tests

## Status

完成。

## Files Changed

- `user-service/pom.xml` — JaCoCo 门控 0.00 → 0.70，移除 coverage-ratchet 注释
- `user-service/src/test/java/com/hmall/UserServiceTestBase.java` — 新增测试基类
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java` — 新增 21 个测试
- `user-service/src/test/java/com/hmall/service/impl/AddressServiceImplTest.java` — 新增 4 个测试
- `user-service/src/test/java/com/hmall/service/impl/FavoriteServiceImplTest.java` — 新增 6 个测试
- `user-service/src/test/resources/application.yml` — H2 测试配置
- `user-service/src/test/resources/sql/schema.sql` — H2 兼容（user 表名加反引号）
- `pay-service/pom.xml` — JaCoCo 门控 0.00 → 0.70，移除 coverage-ratchet 注释，新增 H2 依赖
- `pay-service/src/test/java/com/hmall/PayServiceTestBase.java` — 新增测试基类
- `pay-service/src/test/java/com/hmall/service/impl/PayOrderServiceImplTest.java` — 新增 15 个测试
- `pay-service/src/test/resources/application.yml` — H2 测试配置
- `docs/knowledge-base/modules/user-service.md` — 同步更新
- `docs/knowledge-base/modules/pay-service.md` — 同步更新
- `docs/knowledge-base/flows/auth-and-gateway-flow.md` — 同步更新
- `docs/knowledge-base/flows/order-checkout-flow.md` — 同步更新

## Commands Run

- `mvn -B -ntp -pl user-service verify` — BUILD SUCCESS
- `mvn -B -ntp -pl pay-service verify` — BUILD SUCCESS
- `mvn -B -ntp verify` — 全模块 BUILD SUCCESS

## Known Risks

- notify-service 的测试因 Nacos 连接问题失败（已有问题，非本次变更引入）
- notify-service 已被根 POM 排除在 JaCoCo 检查外

## Next Action

等待 CI 门控通过后合并 PR。

## Worktree Or Branch

- `feat/enhance-tests`
