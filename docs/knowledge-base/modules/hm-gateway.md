---
title: hm-gateway
tracks:
  - hm-gateway/
last_synced_commit: 0c7fdd3
last_synced_date: 2026-05-27
sync_note: "2026-05-27: 引入 excludeReadPaths（仅 GET/HEAD/OPTIONS 放行），/categories/** 移至读白名单"
---

# hm-gateway

## 职责

Spring Cloud Gateway 反向代理：路由分发、JWT 鉴权、CORS、限流、
将解析出的用户 ID 通过 header 传递给下游业务服务。
所有外部请求必须经 Gateway 入口，业务服务不直接对外暴露。

## 公开接口与契约

- 路由规则在 Nacos 共享配置（`shared-jwt.yaml` / 网关 `application.yaml`）中。
- 通过 header `user-info` 把解析后的 userId 传给下游；下游用
  `hm-common.interceptor.UserInfoInterceptor` 写入 `UserContext`。
- 白名单（`auth.excludePaths`）走未鉴权放行；读白名单（`auth.excludeReadPaths`）仅 GET/HEAD/OPTIONS 放行。
- `DynamicRouteLoader` 监听 Nacos `gateway-routes.json` 实现动态路由。

## 上游

外部浏览器 / 移动端 / [hmall-web](hmall-web.md) / [hmall-admin](hmall-admin.md)。

## 下游

所有业务微服务；通过 Nacos 服务发现按路由 path 转发。

## 关键文件

- `GatewayApplication.java` —— 启动类。
- `filters/AuthGlobalFilter.java` —— 全局鉴权过滤器。
- `utils/JwtTool.java` —— JWT 解析。
- `routes/DynamicRouteLoader.java` —— 动态路由。
- `config/AuthProperties.java` / `JwtProperties.java` / `SecurityConfig.java` —— 配置。

## 注意事项与陷阱

- 白名单路径必须显式加 `/api/...` 前缀，与下游 contextPath 对齐。
- JWT 失效或被吊销时返回 401，不要返回 500 泄漏堆栈。
- 修改路由配置后必须验证 Nacos 已下发；否则路由不生效但无报错。
- 不要把鉴权逻辑下沉到业务服务，让 Gateway 作为唯一信任边界。

详见 [auth-and-gateway-flow](../flows/auth-and-gateway-flow.md)。
