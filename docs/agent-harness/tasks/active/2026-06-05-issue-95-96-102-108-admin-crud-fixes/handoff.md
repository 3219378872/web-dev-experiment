# Handoff: Issue 95 96 102 108 Admin CRUD Fixes

## Status
已完成，待提交 PR。

## Files Changed
- `hmall-admin/src/views/ItemList.vue` - 查询参数、批量操作、徽章统计
- `hmall-admin/src/views/CategoryList.vue` - 移除假数据
- `hmall-admin/src/views/BannerList.vue` - 接入 Banner API
- `hmall-admin/src/views/ItemEdit.vue` - 分类动态、图片上传、规格管理、发布设置
- `hmall-admin/src/api/item.js` - 新增批量操作、统计、Banner API
- `hmall-admin/src/api/upload.js` - 新增图片上传 API
- `item-service/src/main/java/com/hmall/item/controller/ItemController.java` - 新增 stats 接口、minPrice/maxPrice 支持

## Commands Run
- `python3 scripts/agent_harness.py check` - passed
- `python3 scripts/knowledge_base.py check` - passed
- `python3 scripts/engineering-lint.py` - passed
- `mvn -q -pl item-service -am test` - passed
- `cd hmall-admin && npm run build` - passed

## Known Risks
- 轮播图前端使用 `gradient`/`subtitle`/`timeRange`/`enabled` 字段为前端增强字段，后端 Banner 实体无这些字段，前端做了默认值回退处理。
- 规格数据以 JSON 字符串存储在 `Item.spec` 字段，前后端需保持一致。

## Next Action
PR #117 已创建，CI 运行中，等待通过后合并。

## PR
- https://github.com/3219378872/web-dev-experiment/pull/117

## Worktree Or Branch
- `task/2026-06-05-issue-95-96-102-108-admin-crud-fixes`
