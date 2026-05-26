# Context: Playwright E2E Tests

## Objective
为 hmall 两个前端（hmall-web 客户端 + hmall-admin 管理后台）建立 Playwright E2E 测试套件，
覆盖核心用户旅程：浏览商品、注册登录、购物车、管理端 CRUD。

## Scope
- In scope:
  - `e2e/` 目录 + `@playwright/test` + TypeScript
  - hmall-web 测试：首页、搜索、登录、购物车
  - hmall-admin 测试：登录、仪表盘、商品/用户/订单列表
  - auth.setup.ts：API 登录 + localStorage token 注入
- Out of scope:
  - 完整下单流程（需 Mock 支付）
  - 管理端 CRUD 表单提交
  - 视觉回归截图对比

## Test Scenarios (11 个)
- hmall-web: 浏览首页、搜索、分类、登录/错误、购物车
- hmall-admin: 登录、仪表盘、商品列表、用户列表、订单列表

## Implementation Approach
- Playwright webServer 可选（依赖外部 docker compose）
- CI 在 smoke job 中集成 docker compose + npx playwright test
