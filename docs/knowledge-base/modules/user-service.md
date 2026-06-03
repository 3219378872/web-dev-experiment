---
title: user-service
tracks:
  - user-service/
last_synced_commit: 7b4de82
last_synced_date: 2026-06-03
sync_note: "新增单元测试 + JaCoCo skip 属性修复"
---

# user-service

## 职责

用户身份、地址簿、商品收藏的归属服务。负责注册、登录（颁发 JWT）、
找回密码、地址 CRUD、收藏增删查。

## 公开接口与契约

- HTTP API：`/users/login`、`/users/register`、`/users/me`、`/addresses/**`、
  `/favorites/**`。
- 对外 Feign：通过 [hm-api](hm-api.md) `UserClient` 暴露用户摘要/地址查询。
- 持久层：`hm-userinfo` 数据库（MySQL），表 `user`、`address`、`user_favorite`。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [trade-service](trade-service.md) 通过 Feign 查询地址、用户摘要。

## 下游

- MySQL（`hm-userinfo`）、Redis（JWT 刷新/黑名单可选）。

## 关键文件

- `UserApplication.java` —— Spring Boot 启动类。
- `controller/UserController.java` / `AddressController.java` / `FavoriteController.java`。
- `service/IUserService.java` 与其 impl。
- `utils/JwtTool.java` —— JWT 颁发。
- `mapper/UserMapper.java` / `AddressMapper.java` / `UserFavoriteMapper.java`。

## 注意事项与陷阱

- `deductMoney` 加 `@Transactional`，作为余额支付 Seata AT 全局事务的 RM 分支：当
  [pay-service](pay-service.md) 的 `tryPayOrderByBalance` 全局回滚时，余额扣减经
  `undo_log` 自动恢复。需 `seata-spring-boot-starter`（默认 `seata.enabled=false`）。
- 密码必须 BCrypt 加盐存储，不允许明文。
- 登录失败次数限流，避免暴力破解。
- 修改 JWT claim 名称/过期策略会破坏 [hm-gateway](hm-gateway.md) 解析。
- 用户软删除（`status` 字段），不要物理删行。
