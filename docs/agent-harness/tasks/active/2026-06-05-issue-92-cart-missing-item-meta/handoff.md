# Handoff: Issue #92

## 状态
已完成修复，待提交 PR。

## 改动摘要
- `CartServiceImpl.addItem2Cart()`: 保存前通过 `itemClient` 获取商品信息，填充 `name`/`image`/`price`/`spec`
- `CartServiceImpl.handleCartItems()`: 回填 `name`/`spec`/`image` 到 `CartVO`

## 关联 Issue
Closes #92
