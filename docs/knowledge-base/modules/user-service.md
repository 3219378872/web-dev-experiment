---
title: user-service
tracks:
  - user-service/
last_synced_commit: 4a14a2b
last_synced_date: 2026-06-06
sync_note: "fix(#135,#143): 管理端用户列表新增 status 过滤且 keyword 覆盖 username/phone/email；管理员改密必须提交 currentPassword 并通过 BCrypt 校验。"
---

# user-service

## 职责

用户身份、地址簿、商品收藏的归属服务。负责注册、登录（颁发 JWT）、
找回密码、地址 CRUD、收藏增删查。**新增**：管理端用户管理与个人中心功能。

## 公开接口与契约

### 用户端接口
- HTTP API：`/users/login`、`/users/register`、`/users/send-code`、
  `/users/reset-password`、`/users/profile`（GET 取当前用户脱敏资料 / PUT 更新）、
  `/addresses/**`、`/favorites/**`。
- **登录标识（Issue #133）**：`/users/login` 的 `username` 字段接受用户名 / 邮箱 /
  手机号任一，service 层 `where username=? or email=? or phone=?` 命中即可，返回形态
  仍为 `UserLoginVO`（未破坏既有契约）。找回密码当前仅支持邮箱验证码。
- **个人资料持久化（Issue #130）**：`GET /users/profile` 返回当前登录用户 `UserVO`
  （含 gender/birthday）；`PUT /users/profile` 落库 nickname/email/avatar/gender/
  birthday（空白/null 字段跳过）。头像走 [file-service](file-service.md) `/upload/image`
  得 url 后再 PUT 持久化。
- 对外 Feign：通过 [hm-api](hm-api.md) `UserClient` 暴露用户摘要/地址查询。
- 持久层：`hm-userinfo` 数据库（MySQL），表 `user`、`address`、`user_favorite`。

### 管理端接口（Phase 1 新增）
- `GET /admin/users?page&size&keyword&status` → `PageDTO<UserVO>`：分页查询用户列表，
  keyword 模糊匹配 username/phone/email，status 可选过滤用户状态（1=正常/2=冻结）。
- `PUT /admin/users/{id}/status`（body `{status}`）→ `R<Void>`：修改用户状态（1=正常/2=冻结）
- `GET /admin/users/{id}` → `UserVO`：获取用户详情（脱敏，不含 password）
- `PUT /admin/profile`（body `AdminPasswordUpdateDTO` 兼容 User 字段）→ `R<Void>`：
  管理员修改信息/密码；改密时必须提供 `currentPassword` 并通过 BCrypt 校验。
- `GET /admin/profile/permissions` → `List<String>`：获取管理员权限码

### 数据模型
- `UserLoginVO`：登录响应 VO，包含 token、userId、username、balance、**role**（Issue #76 新增）。`role` 值与数据库 `user.role` 字段一致（`"admin"` / `"user"`），`role` 为 null 时默认返回 `"user"`。`hmall-admin` Login.vue 用此字段做管理员身份校验（`data.role !== 'admin'`）。
- `UserVO`：用户脱敏 VO，包含 id、username、phone、status、balance、role、email、avatar、nickname、gender、birthday、createTime、updateTime（不含 password）。gender/birthday 为 Issue #130 新增（迁移 `docs/sql/user-service-profile-fields.sql`，列 `gender VARCHAR(1)`/`birthday DATE`）
- `UserStatusDTO`：用户状态修改 DTO，包含 status 字段（1=正常/2=冻结）
- `AdminPasswordUpdateDTO`：管理员个人中心改密 DTO，继承当前资料更新字段并新增
  `currentPassword`，仅在提交新密码时强制校验。
- `UserStatus` 枚举：NORMAL(1, "正常")、FROZEN(2, "冻结")

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [trade-service](trade-service.md) 通过 Feign 查询地址、用户摘要。

## 下游

- MySQL（`hm-userinfo`）、Redis（JWT 刷新/黑名单可选）。

## 关键文件

- `UserApplication.java` —— Spring Boot 启动类。
- `controller/UserController.java` / `AddressController.java` / `FavoriteController.java`。
- `controller/AdminUserController.java` / `AdminProfileController.java`（Phase 1 新增）。
- `service/IUserService.java` 与其 impl。
- `domain/vo/UserVO.java`（Phase 1 新增）。
- `domain/vo/FavoriteVO.java`（收藏商品VO，包含商品信息）。
- `enums/UserStatus.java`（枚举值调整：NORMAL=1, FROZEN=2）。
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
- **UserVO 脱敏**：管理端接口返回 UserVO 不含 password 字段，避免密码泄露。
- **状态值约定**：UserStatus 枚举值为 NORMAL=1, FROZEN=2（非 0/1），与数据库字段对齐。
- 管理员个人中心普通资料更新可只提交 nickname/email/avatar；只要提交 password，
  service 层就要求 `currentPassword` 非空且匹配当前登录管理员密码。
