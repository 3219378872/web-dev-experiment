# Context: Fix Issue #71 - 分类筛选失效 + 品类数量显示为0

## Objective

修复 /category 页面两个 Bug：点击分类后商品列表为空（筛选失效），以及左侧分类数量全部显示为 0。

## Scope

- In scope:
  - `hmall-web/src/views/Category.vue` 筛选逻辑与计数逻辑修复
  - `hmall-web/src/__tests__/Category.spec.js` 新增单元测试
  - `docs/knowledge-base/modules/hmall-web.md` frontmatter bump (K005 豁免)
- Out of scope:
  - 后端 endpoint 形态变更（避免破坏现网调用）
  - 其他前端页面

## Bug 根因

**Bug #5 - 筛选失效**：`filteredItems` computed 用 `i.categoryId === activeCatId || i.cid === activeCatId` 匹配，但后端 item 对象只有 `category` 字段（值为分类名称字符串，如"电子产品"），没有 `categoryId`/`cid` 字段，导致过滤条件永远为 false，商品列表始终为空。

**Bug #6 - 数量显示为 0**：`CategoryController.list()` 返回的 Category PO 没有 `count` 字段，前端 `cat.count || 0` 永远取 0。

## Related Artifacts

- Spec: none - 根因明确，纯前端逻辑修复
- Plan: none - 单文件改动，修复策略清晰

## Likely Files

- `hmall-web/src/views/Category.vue`
- `hmall-web/src/__tests__/Category.spec.js`
- `docs/knowledge-base/modules/hmall-web.md`

## Runtime Evidence To Inspect First

- `hmall-web/src/views/Category.vue` filteredItems computed 中的 categoryId/cid 字段匹配

## Safety Constraints

- 纯前端修复，不改动任何后端 endpoint 形态，不破坏 API 契约
- 不在源码中硬编码密钥/口令/Token

## Worktree Or Branch

- `task/2026-06-05-fix-issue-71-category-filter`

## Open Questions

- none
