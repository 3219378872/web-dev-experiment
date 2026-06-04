---
title: auth-and-gateway-flow
tracks:
  - hm-gateway/
  - user-service/
  - hm-common/
last_synced_commit: c7c40c5f5c10302cc9ae71ef1c41bc099cd68690
last_synced_date: 2026-06-04
sync_note: "Fix #57: login 异常类型调整（Assert.notNull → BadRequestException），无需文档更新"
---

# auth-and-gateway-flow

登录、JWT 颁发与 [hm-gateway](../modules/hm-gateway.md) 鉴权链。所有外部请求
（C 端 / 管理端）都经此路径。

## 流程

1. **登录** —— [hmall-web](../modules/hmall-web.md) 或
   [hmall-admin](../modules/hmall-admin.md) → [hm-gateway](../modules/hm-gateway.md)
   `POST /users/login`（白名单路径放行）。
2. **颁发 JWT** —— Gateway 转发到 [user-service](../modules/user-service.md)。
   user-service 校验用户名/密码（BCrypt），颁发 JWT（含 userId、role、过期）。
3. **前端持久化** —— 返回 token，前端存 localStorage，后续 axios 请求自动注入
   `Authorization: Bearer <token>`。
4. **鉴权拦截** —— 后续请求经 Gateway，`AuthGlobalFilter` 校验 JWT：
   - 白名单路径（`excludePaths`）放行。
   - 读白名单路径（`excludeReadPaths`）仅 GET/HEAD/OPTIONS 放行；写操作需认证。
   - 解析 token 失败 / 过期 → 401。
   - 解析成功 → 将 userId 写入 header `user-info`，转发下游。
5. **下游用户上下文** —— 业务服务通过 [hm-common](../modules/hm-common.md)
   `UserInfoInterceptor` 把 header 写入 `UserContext`（ThreadLocal），controller/service
   通过 `UserContext.getUser()` 拿当前用户 ID。
   异步消费者（如 RabbitMQ listener）不经过 Gateway/header 拦截器；若业务逻辑需要用户上下文，
   必须从事件载荷显式设置 `UserContext` 并在 `finally` 中清理。
6. **管理端额外校验** —— 管理端路由（`/admin/**`）在 Gateway 或下游服务侧再判
   role 是否管理员；非管理员返回 403。

## 关键不变量

- JWT 是唯一信任来源；下游业务服务不再做密码校验。
- 业务服务不能信任外部 header `user-info`；它必须由 Gateway 写入（其它入口被拒）。
- token 撤销若需要，走 Redis 黑名单 + 短过期 + refresh token，不要长生命期。
- `UserContext` 是 ThreadLocal，跨线程必须显式传递（异步任务、`@Async`、线程池）。

## 失败与恢复

- 401 → 前端清 token 跳登录；不静默重试。
- 403 → 显示"无权限"，不暴露资源是否存在。
- Gateway 故障 → 前端展示"网关不可用"，不直连业务服务。
- JWT 密钥泄露 → 全员强制下线（清 Redis 黑名单 + 轮换密钥）。

## 测试策略

- 单元：JwtTool / UserInfoInterceptor 单测。
- 集成：起 Gateway + user-service + Testcontainers MySQL，验证白名单/401/403 路径。
- E2E：Playwright 登录后跳首页、刷新页面保持登录、退出后 401 跳回登录页。
