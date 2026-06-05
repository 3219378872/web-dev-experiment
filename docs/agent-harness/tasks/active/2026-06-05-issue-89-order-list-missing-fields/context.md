# Issue #89: 订单列表不显示商品名称、图片与规格

## Objective
修复用户端「我的订单」列表页面中商品信息（名称、图片、规格、单价）不显示的问题。

## Scope
- In scope: `OrderController.myOrders()` 方法，确保返回的 `OrderVO` 包含 `details` 列表
- Out of scope: 前端页面修改（前端已正确读取 `details` 字段，问题仅在后端）

## Related Artifacts
- Spec: none - 这是一个简单的数据填充 bugfix，不需要单独 spec
- Plan: none - 修改范围小，单文件单方法

## Likely Files
- `trade-service/src/main/java/com/hmall/controller/OrderController.java`

## Runtime Evidence To Inspect First
- `OrderController.myOrders()` 第 76-86 行：使用 `PageDTO.of(result, OrderVO.class)` 只做了简单 Bean 拷贝
- `OrderController.queryOrderById()` 第 44-60 行：正确查询了 OrderDetail 并填充，可作为参考

## Safety Constraints
- 不要破坏既有 API 响应结构
- 保持 `PageDTO<OrderVO>` 返回格式不变

## Worktree Or Branch
- `task/2026-06-05-issue-89-order-list-missing-fields`

## Open Questions
- none
