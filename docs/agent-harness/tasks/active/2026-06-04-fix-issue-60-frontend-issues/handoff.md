# Handoff: Fix Issue 60 Frontend Issues

## Status
Completed.

## Files Changed
- `hmall-web/src/api/request.js`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/views/Home.vue`
- `hmall-web/src/components/AppHeader.vue`
- `docs/knowledge-base/modules/hmall-web.md`

## Commands Run
- `cd hmall-web && npm run build` —— 构建成功

## Known Risks
- 无。变更仅影响未登录态的 401 处理与分类渲染方式。

## Next Action
等待 CI 通过并合并 PR。

## Worktree Or Branch
- `task/2026-06-04-fix-issue-60-frontend-issues`
