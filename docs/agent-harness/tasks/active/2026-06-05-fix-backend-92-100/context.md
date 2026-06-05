# Fix Issue #92 and #100: 购物车商品数据与支付500错误

## Objective
修复两个后端 Bug：
1. **Issue #92**: 购物车页面不显示商品名称、图片和单价
2. **Issue #100**: 支付接口返回 500 错误

## Scope
### In scope
- cart-service: `CartServiceImpl.addItem2Cart()` 新增购物车时填充商品名称/图片/价格
- cart-service: `CartServiceImpl.handleCartItems()` 回填商品名称/图片
- pay-service: `PayOrderServiceImpl.tryPayOrderByBalance()` 增加空值检查
- pay-service: MQ 发布降级处理
- hm-api: OrderClient 和 UserClient 添加 FallbackFactory

### Out of scope
- 前端页面修改
- 其他服务的 Feign 客户端

## Related Artifacts
- Spec: none
- Plan: none

## Likely Files
- `cart-service/src/main/java/com/hmall/cart/service/impl/CartServiceImpl.java`
- `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- `hm-api/src/main/java/com/hmall/api/client/OrderClient.java`
- `hm-api/src/main/java/com/hmall/api/client/UserClient.java`
- `hm-api/src/main/java/com/hmall/api/client/fallback/OrderClientFallbackFactory.java` (new)
- `hm-api/src/main/java/com/hmall/api/client/fallback/UserClientFallbackFactory.java` (new)

## Safety Constraints
- 不要破坏既有 API 响应结构
- Feign 降级返回 null 时，调用方已有 null 检查

## Worktree Or Branch
- `task/2026-06-05-fix-backend-92-100`

## Open Questions
- none
