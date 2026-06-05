# Context: Fix Issue #73 – Order Actions (Repurchase / Delete / Pay)

## Objective

修复 Issue #73 中报告的三个订单操作 Bug：

- **Bug #8**：`OrderList.vue` 中"再次购买"与"删除订单"按钮无 `@click` 绑定，后端也无对应 API。
- **Bug #9**：`OrderDetail.vue` 中 `handlePay()` 只做 `router.push(…?pay=1)`，无实际支付流程；`OrderList` 的立即付款也只是导航。

## Scope

- In scope：
  - 后端 `trade-service` 新增 `DELETE /orders/{id}` 端点（校验归属与状态，返回 `R<Void>`），补 JUnit5 单测。
  - 前端 `hmall-web/src/api/order.js` 新增 `deleteOrder`、`createPayOrder`、`payOrderByBalance`。
  - `OrderList.vue`：再次购买 → `handleRepurchase`（遍历 details 加购后跳转 `/cart`）；删除订单 → `handleDeleteOrder`（调 API 刷新列表）。
  - `OrderDetail.vue`：立即付款弹窗，先 `POST /pay-orders` 创建支付单，再 `POST /pay-orders/{id}` 执行余额支付，成功刷新订单；`?pay=1` 参数自动触发弹窗。
  - 前端 Vitest 测试（再次购买 / 删除 / 支付函数逻辑）。
- Out of scope：Seata 事务改动、多种支付渠道（目前只余额）、Gateway 路由变更（`/pay-orders` 已有路由）。

## Related Artifacts

- Issue: #73
- Changed files:
  - `trade-service/src/main/java/com/hmall/service/IOrderService.java`
  - `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
  - `trade-service/src/main/java/com/hmall/controller/OrderController.java`
  - `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`
  - `hmall-web/src/api/order.js`
  - `hmall-web/src/views/OrderList.vue`
  - `hmall-web/src/views/OrderDetail.vue`
  - `hmall-web/src/__tests__/order-actions.spec.ts`

## Runtime Evidence Inspected

- `PayController` 端点确认：`POST /pay-orders`（接收 `PayApplyDTO`，返回 `String` 支付单ID）；`POST /pay-orders/{id}`（接收 `PayOrderFormDTO{id, pw}`，返回 `void`）。
- `Order` 实体无 `deleted` 字段（无软删除列），`deleteOrder` 实现为 `removeById`，状态守卫限制只允许 status=4/5 的订单被删除。
- `cartStore.addItem(form)` 接收 `{itemId, num}`，与后端 `/carts POST` 对齐。
- `OrderDetailVO.itemId` 字段存在，可供再次购买使用。

## Safety Constraints

- 不破坏既有 endpoint 形态（新增 DELETE `/orders/{id}`，不修改已有接口）。
- `deleteOrder` 严格检查 userId 归属，防止越权删除。
- 支付弹窗密码字段用 `type="password"`，不明文展示。
