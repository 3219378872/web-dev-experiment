# Context: Issue #92 购物车不显示商品名称、图片和单价

## 问题描述
购物车页面商品条目不显示商品名称、图片和单价，仅显示数量和小计。

## 根因分析
1. `addItem2Cart()` 只保存前端传入的 `itemId`/`num`/`spec`，未通过 `itemClient` 获取商品元数据写入 `Cart` PO
2. `handleCartItems()` 虽然批量查询了商品信息，但只设置了 `newPrice`/`status`/`stock`，遗漏了 `name`/`image`/`spec`

## 修复范围
- `cart-service/src/main/java/com/hmall/cart/service/impl/CartServiceImpl.java`
