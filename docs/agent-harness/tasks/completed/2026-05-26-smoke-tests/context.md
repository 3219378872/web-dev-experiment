# Context: Smoke Tests

## Objective
为 hmall 微服务栈建立 HTTP 冒烟测试脚本（`scripts/smoke/smoke.sh`），
使用 curl + jq 验证 docker-compose 运行中栈的核心 API 路径。

## Scope
- In scope:
  - `scripts/smoke/smoke.sh` 冒烟脚本（bash + curl + jq）
  - 10 个测试场景：健康检查、公开 API、登录认证、认证拒绝、错误处理
  - CI smoke job 新增 docker compose up + HTTP smoke 步骤
- Out of scope:
  - 完整的 E2E 用户旅程测试（Playwright 覆盖）
  - 管理后台 API (hmall-admin)
  - 性能/负载测试

## Test Scenarios
1. `GET /hi` → 200（网关可达性）
2. `GET /items/page` → 200 + JSON（商品分页）
3. `GET /search/list` → 200 + JSON（搜索）
4. `GET /categories` → 200 + JSON（分类）
5. `GET /notifications/active` → 200 + JSON（公告）
6. `POST /users/login` → 200 + token（登录）
7. `GET /addresses` with token → 200（认证请求）
8. `GET /addresses` without token → 401（认证拒绝）
9. `POST /users/login` wrong password → 400/401（错误密码）
10. `GET /addresses` with invalid token → 401（无效 token）

## Implementation Approach
- 纯 bash + curl + jq，无额外依赖
- 退出码：0 = 全通过，1 = 有失败
- CI 中通过 docker compose up --wait 启动全栈后运行
