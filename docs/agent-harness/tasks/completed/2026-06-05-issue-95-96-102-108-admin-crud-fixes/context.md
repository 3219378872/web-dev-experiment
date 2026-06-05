# Context: Issue 95 96 102 108 Admin CRUD Fixes

## Objective
修复管理后台商品、分类、轮播图管理相关的 CRUD 问题。

## Scope
- In scope:
  - #95: 管理端商品列表查询参数补全、批量操作事件、标签徽章基于后端统计
  - #96: 管理端分类管理移除假数据，使用真实接口数据
  - #102: 管理端轮播图管理接入后端 Banner CRUD 接口
  - #108: 商品新增/编辑页分类动态获取、图片真实上传、规格动态管理、发布设置绑定真实字段
- Out of scope:
  - 前端样式大改
  - 新增后端实体/表结构

## Related Artifacts
- Spec: none - 修复现有功能，无需新 spec
- Plan: none - 修复现有功能，无需新 plan

## Likely Files
- `hmall-admin/src/views/ItemList.vue`
- `hmall-admin/src/views/CategoryList.vue`
- `hmall-admin/src/views/BannerList.vue`
- `hmall-admin/src/views/ItemEdit.vue`
- `hmall-admin/src/api/item.js`
- `hmall-admin/src/api/upload.js` (新增)
- `item-service/src/main/java/com/hmall/item/controller/ItemController.java`

## Runtime Evidence To Inspect First
- 管理端商品列表页查询、批量操作、徽章统计
- 管理端分类页数据真实性
- 管理端轮播图页 CRUD
- 商品编辑页分类/图片/规格/发布设置

## Safety Constraints
- 不破坏既有 API 响应形态
- 新增 endpoint 遵循 R<Void>(写) + *VO/PageDTO<T>(读) 规范
- 不在源码中硬编码密钥

## Worktree Or Branch
- `task/2026-06-05-issue-95-96-102-108-admin-crud-fixes`

## Open Questions
- none
