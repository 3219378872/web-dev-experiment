# 交接记录

## 变更摘要
- `hmall-web/src/views/ItemDetail.vue`：规格选择器从硬编码改为从 `item.spec` 动态解析

## PR 描述
Fix #93: 商品详情页规格选择器写死在前端

- 解析后端 `item.spec` 字段动态渲染规格选项
- 支持颜色规格自动显示色块
- 购物车/立即购买使用用户选择的规格

## CI 状态
- lint: 待验证
- test: 通过（26 tests）
- build: 通过

## 已知问题
- 无
