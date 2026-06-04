# Handoff: Enhance Unit Tests

## Status

完成。

## Files Changed

### 门控提升（70% → 80%）
- `pom.xml` — 根 JaCoCo 门控 0.70 → 0.80
- `user-service/pom.xml` — JaCoCo 门控 0.70 → 0.80
- `pay-service/pom.xml` — JaCoCo 门控 0.70 → 0.80

### trade-service 测试增强
- `trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java` — 新增 1 个测试（claimCoupon_disabled_throwsBadRequest）
- `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java` — 新增 5 个测试（null 订单检查 + 已发货退款）

### Harness Task 更新
- `docs/agent-harness/tasks/active/2026-06-03-enhance-unit-tests/context.md`
- `docs/agent-harness/tasks/active/2026-06-03-enhance-unit-tests/verification.md`
- `docs/agent-harness/tasks/active/2026-06-03-enhance-unit-tests/handoff.md`

## Commands Run

- `mvn -B -ntp -pl trade-service test` — 28 tests, 0 failures, BUILD SUCCESS
- `mvn -B -ntp -pl trade-service verify -DskipIntegrationTests` — BUILD SUCCESS (JaCoCo 90.5%)
- `mvn -B -ntp verify -DskipIntegrationTests` — 全模块 BUILD SUCCESS (80% 门控全部满足)

## Coverage Results

| 模块 | 分支覆盖率 | 状态 |
|------|-----------|------|
| trade-service | 90.5% (38/42) | ✓ |
| item-service | 100.0% (2/2) | ✓ |
| cart-service | 91.7% (11/12) | ✓ |
| user-service | 97.1% (33/34) | ✓ |
| pay-service | 91.7% (11/12) | ✓ |
| file-service | 100.0% (2/2) | ✓ |

## Known Risks

- 无新增风险

## Next Action

提交 PR，等待 CI 门控通过后合并。

## Worktree Or Branch

- `feat/enhance-tests`
