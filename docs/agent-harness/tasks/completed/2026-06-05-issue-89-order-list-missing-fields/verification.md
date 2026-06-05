# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -pl trade-service -am test` | PASS | Tests run: 58, Failures: 0, Errors: 0, Skipped: 0 |
| `mvn -pl trade-service -am compile` | PASS | 编译通过，无错误 |

## 功能验证
- `myOrders()` 现在会批量查询订单详情并填充到每个 OrderVO 的 details 字段
- 前端 `OrderList.vue` 的 `orderItems()` 函数会从 `order.details` 读取 name/image/spec/price/num
- 订单列表页现在应该能正确显示商品名称、图片、规格和单价
