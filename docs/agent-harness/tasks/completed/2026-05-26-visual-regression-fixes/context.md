# Context: Visual Regression Fixes

## Objective
修复前端代码审查中发现的视觉和可访问性缺陷，并建立 Playwright 截图基线。

## Scope
- img alt 属性：ProductCard, Cart, ItemDetail, OrderConfirm, ItemList(admin), BannerList(admin)
- 图片回退：OrderConfirm, ItemList(admin), BannerList(admin) 加 `|| '/placeholder.png'`
- AppHeader router-link-active 高亮
- Router scrollBehavior（导航回顶部）
- AdminLayout default-openeds 子菜单
- e2e/visual/screenshots.spec.ts 截图脚本

## Out of Scope
- 响应式设计、表单验证、CSS 变量主题化、rem 迁移
