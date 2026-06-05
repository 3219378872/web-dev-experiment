# Handoff: Fix Issue #71 - 分类筛选失效 + 品类数量显示为0

## Status

Implementation complete. Tests passing. Build passing.

## Files Changed

- `hmall-web/src/views/Category.vue` — 修复筛选逻辑（按 category 名称匹配）与品类数量（客户端计算 categoriesWithCount）
- `hmall-web/src/__tests__/Category.spec.js` — 新增 18 个单元测试覆盖筛选、计数、排序逻辑
- `docs/knowledge-base/modules/hmall-web.md` — bump last_synced_commit（K005 豁免）

## Commands Run

- `npm ci --silent` — 依赖安装
- `npm test` — 20 tests passed
- `npm run build` — build succeeded

## Known Risks

- none；纯前端修复，不影响后端 API 契约

## Next Action

Push branch, open PR, wait for CI.

## Worktree Or Branch

- `task/2026-06-05-fix-issue-71-category-filter`
