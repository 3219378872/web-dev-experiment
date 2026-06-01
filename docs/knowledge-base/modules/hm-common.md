---
title: hm-common
tracks:
  - hm-common/
last_synced_commit: 8c38db8
last_synced_date: 2026-05-27
sync_note: "对齐文档与实际混合响应封装模式（不改 hm-common 代码）"
---

# hm-common

## 职责

跨所有微服务的共享基础库：统一响应封装、分页查询契约、通用异常、Mybatis-Plus
默认配置、JWT 解析、用户上下文持有、Web/Feign 拦截器。
不包含业务逻辑。

## 公开接口与契约

- `com.hmall.common.domain.R<T>` / `PageDTO<T>` —— 响应封装类型；在仓库内
  使用不统一（详见 *注意事项与陷阱*）。`CommonExceptionAdvice` 用 `R<Void>`
  统一包装异常路径。
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

- 响应封装在仓库内**不统一**：异常路径由 `CommonExceptionAdvice` 一致返
  `R<Void>`；成功路径混合 `R<T>`/`R<Void>`、`*VO`/`*DTO`/`PageDTO<T>`、
  裸 `void`/`Long`/`String`，甚至裸返数据库 entity（如
  `user-service/AddressController.list()` → `List<Address>`）。两个前端
  axios `r => r.data` 拦截器对两种形态都兼容。
- 新增 endpoint 优先 `R<Void>`(写) + `*VO`/`PageDTO<T>`(读)；**不要无前端
  协调地改既有 endpoint 形态**，否则破坏现网调用。
- `UserContext` 是 ThreadLocal，异步线程必须显式传递。
- 修改 `R` 字段顺序或名称会破坏前端解析。
