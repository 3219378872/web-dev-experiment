# Context: Issue 91 Category Pagination

## Objective
为分类页面添加分页控件，使其不再只展示前 100 项商品，而是支持按页浏览全部商品。

## Scope
- In scope:
  - `hmall-web/src/views/Category.vue` 添加分页 UI 与分页状态管理
  - 修改 `getItems` 调用为按当前页码/页大小请求
  - 分类筛选与排序需作用于当前页数据
  - 更新/补充单元测试
- Out of scope:
  - 后端接口改动（`/items/page` 已支持分页参数）
  - 其他页面改动

## Related Artifacts
- Spec: none - 改动范围小，无需单独 spec。
- Plan: none - 单文件修改，无需多步计划。

## Likely Files
- `hmall-web/src/views/Category.vue`
- `hmall-web/src/__tests__/Category.spec.js`

## Runtime Evidence To Inspect First
- Category.vue 当前在 `onMounted` 中一次性请求 `size: 100`，无分页控件。

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- 不要无前端协调地改既有 endpoint 形态。

## Worktree Or Branch
- `task/2026-06-05-issue-91-category-pagination`

## Open Questions
- none
