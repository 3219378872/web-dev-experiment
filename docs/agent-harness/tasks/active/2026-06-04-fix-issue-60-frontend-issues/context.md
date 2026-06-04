# Context: Fix Issue 60 Frontend Issues

## Objective
修复 hmall-web 前端三处问题：游客访问商品详情被强制跳登录、首页左侧分类菜单无跳转、硬编码分类与数据库不匹配。

## Scope
- In scope:
  - request.js 401 拦截器逻辑调整（游客静默失败）
  - ItemDetail.vue 游客免调 checkFavorite
  - Home.vue catmenu 添加 router-link 跳转
  - Home.vue / AppHeader.vue 分类改为动态从 /categories 获取
- Out of scope:
  - 后端接口改动
  - 其他页面逻辑

## Related Artifacts
- Spec: null —— 前端 bug 修复，无需独立 spec。
- Plan: null —— 变更范围明确，无需独立 plan。

## Likely Files
- `hmall-web/src/api/request.js`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/views/Home.vue`
- `hmall-web/src/components/AppHeader.vue`

## Runtime Evidence To Inspect First
- 未登录访问 `/item/1` 观察是否跳转登录页
- 首页左侧分类菜单点击观察是否有跳转

## Safety Constraints
- 不破坏已登录用户的正常 401 处理流程。
- 保持 axios 拦截器对其他错误状态的处理不变。

## Worktree Or Branch
- `task/2026-06-04-fix-issue-60-frontend-issues`

## Open Questions
- none
