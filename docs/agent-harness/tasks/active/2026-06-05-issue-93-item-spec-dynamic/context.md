# Issue #93: 商品详情页规格选择器写死在前端

## 问题描述
ItemDetail.vue 中的颜色、版本规格选择器是硬编码的（曜石黑/云母白/雾霾蓝、标准版/降噪增强版/游戏专业版），不适用于所有商品。

## 根因分析
前端未使用后端 `item.spec` 字段，而是写死了几组规格选项。

## 修复范围
- `hmall-web/src/views/ItemDetail.vue`：
  - 新增 `parsedSpecs` computed，解析 `item.spec`（格式：`key:val1/val2,key2:val3/val4`）
  - 新增 `selectedSpecs` ref 存储用户选择
  - 新增 `isColorKey`、`swatchStyle`、`selectSpec`、`formatSelectedSpec` 辅助函数
  - 模板中用 `v-for` 动态渲染规格行
  - 购物车/立即购买时调用 `formatSelectedSpec()` 生成规格字符串

## 影响面
仅商品详情页规格选择区域，不影响其他页面。
