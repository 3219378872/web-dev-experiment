---
title: hmall-web
tracks:
  - hmall-web/
last_synced_commit: c7c40c5f5c10302cc9ae71ef1c41bc099cd68690
last_synced_date: 2026-06-04
sync_note: "Fix #60: request.js 拦截器 401 仅在本地有 token 时跳登录；ItemDetail.vue 游客免调 checkFavorite；Home.vue/AppHeader.vue 分类改为动态从 /categories 获取，消除硬编码与口径漂移。"
---

# hmall-web

## 职责

客户端前端。Vue 3 + Element Plus + Pinia + Vue Router + Vite，
覆盖商品浏览、详情、加购、结算、订单、用户中心、优惠券、评价共 16 页面。

## 公开接口与契约

- 所有 API 请求经 [hm-gateway](hm-gateway.md)。后端响应封装当前不统一：
  异常一律是 `R<Void>`（`CommonExceptionAdvice`），成功响应可能是 `R<T>`/`R<Void>`、
  `*VO`/`*DTO`/`PageDTO<T>` 或裸 `void`/`Long`/`String`。axios 拦截器
  `r => r.data` 对两种形态都兼容，调用点按对应字段消费（如 `data?.list`）。
- 鉴权走 JWT；登录后 token 存 localStorage，axios 拦截器自动注入 Authorization header。
- Pinia store 持有用户态、购物车摘要、当前订单草稿。

## 上游

浏览器用户。

## 下游

[hm-gateway](hm-gateway.md)。

## 关键文件

- `package.json` —— 依赖与脚本。
- `vite.config.ts` —— 构建/代理配置。
- `.eslintrc.cjs`、`.prettierrc` —— 代码质量与格式化配置。
- `src/api/` —— axios 客户端、按服务划分的 API 调用。
- `src/router/index.ts` —— 路由。
- `src/stores/` —— Pinia store。
- `src/views/` —— 13 个页面。
- `src/components/` —— 共享组件。

## 注意事项与陷阱

- 价格展示一律分→元转换在前端做，避免后端返回 double。
- 鉴权失败（401）必须跳登录并清 token，不要静默重试。
- 商品图懒加载；首屏 LCP 关注度高。
- 路由 meta 标记是否需登录，由全局守卫统一处理。
- 提交前运行 `npm run lint` 和 `npm run format`；pre-commit hook 自动检查。
