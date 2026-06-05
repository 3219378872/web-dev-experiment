# Handoff

## Summary
修复了 Issue #92（购物车不显示商品信息）和 Issue #100（支付 500 错误）。

## Changes

### Issue #92 - 购物车商品数据
- `CartServiceImpl.addItem2Cart()`: 保存购物车前调用 `itemClient.queryItemByIds()` 获取并填充 name/image/price
- `CartServiceImpl.handleCartItems()`: 回填 name/image（当快照为 null 时）

### Issue #100 - 支付 500 错误
- `PayOrderServiceImpl.tryPayOrderByBalance()`: 增加 `po == null` 校验
- `PayOrderServiceImpl.tryPayOrderByBalance()`: MQ 发布包裹在 try-catch 中降级
- `OrderClientFallbackFactory` (new): 查询返回 null，更新抛出 BizIllegalException
- `UserClientFallbackFactory` (new): 扣减抛出 BizIllegalException，统计返回 0
- `hm-api/pom.xml`: 增加 `spring-cloud-starter-circuitbreaker-resilience4j`
- `pay-service/src/main/resources/application.yaml`: 增加 `feign.circuitbreaker.enabled: true`

## Verification
- [x] `mvn test -pl cart-service -am` - BUILD SUCCESS
- [x] `mvn test -pl pay-service -am` - BUILD SUCCESS

## PR
- PR title: `fix(backend): resolve cart item metadata (#92) and pay order 500 error (#100)`
- Branch: `task/2026-06-05-fix-backend-92-100`
- Base: `main`
