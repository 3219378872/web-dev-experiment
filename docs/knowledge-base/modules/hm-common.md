---
title: hm-common
tracks:
  - hm-common/
last_synced_commit: bc09d0e
last_synced_date: 2026-05-26
sync_note: ""
---

# hm-common

## 职责

跨所有微服务的共享基础库：统一响应封装、分页查询契约、通用异常、Mybatis-Plus
默认配置、JWT 解析、用户上下文持有、Web/Feign 拦截器。
不包含业务逻辑。

## 公开接口与契约

- `com.hmall.common.domain.R<T>` / `PageDTO<T>` —— 全部 controller 返回值必须用它们包装。
- `com.hmall.common.domain.PageQuery` —— 分页/排序参数基类。
- `CommonException` / `BadRequestException` / `UnauthorizedException` /
  `ForbiddenException` / `BizIllegalException` / `DbException` —— 全栈统一抛出。
- `UserContext` —— ThreadLocal 当前登录用户 ID；由 Web/Feign 拦截器写入。
- `MvcConfig` / `MybatisConfig` —— 自动装配，子服务通常无需自配。

## 上游

无 —— 它是基础库。

## 下游

`hm-api`、`hm-gateway`、所有业务微服务（`*-service`）。

## 关键文件

- `domain/R.java` —— 统一响应封装。
- `domain/PageDTO.java` / `domain/PageQuery.java` —— 分页契约。
- `exception/` —— 异常体系。
- `utils/UserContext.java` —— 用户上下文。
- `config/MvcConfig.java` —— Web MVC 默认配置。
- `interceptor/UserInfoInterceptor.java` —— 从 header 读取并写入 `UserContext`。

## 注意事项与陷阱

- 任何 controller 都必须返回 `R<T>` 或 `PageDTO<T>`；裸返 entity 视为 breaking change。
- `UserContext` 是 ThreadLocal，异步线程必须显式传递。
- 修改 `R` 字段顺序或名称会破坏前端解析。
