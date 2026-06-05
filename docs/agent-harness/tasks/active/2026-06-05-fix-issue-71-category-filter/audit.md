# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Bug #5 筛选逻辑按分类名称匹配 | done | Category.vue filteredItems 改为 `i.category === activeCatName` 匹配 |
| Bug #6 品类数量客户端计算 | done | 新增 categoriesWithCount computed，基于 items 按名称 filter 计算 count |
| 不改变后端 endpoint 形态 | done | 纯前端修复，未接触任何后端代码 |
| 补 Vitest 单元测试 | done | hmall-web/src/__tests__/Category.spec.js 18 个测试（筛选+计数+排序逻辑） |
| 测试全通过 | done | 20 tests passed (3 test files) |
| build 通过 | done | `npm run build` 成功 |
| 不可变模式（sortItems 返回新数组） | done | sortItems 函数使用 `[...list]` 复制再排序 |
