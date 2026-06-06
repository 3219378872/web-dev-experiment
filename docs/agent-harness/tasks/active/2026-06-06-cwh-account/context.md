# Context: C 端账户域三项缺陷 (#129 #130 #133)

## Objective
修复 hmall-web 收藏页加购未接口化(#129)、个人资料未真实持久化(#130)、登录页文案与后端能力不符(#133) 三项缺陷，并补齐必要的 user-service 后端能力。

## Scope
- In scope:
  - #129 FavoriteList.vue「加入购物车」接 `cartStore.addItem`（按收藏 itemId 加购，处理未登录/异常）。
  - #130 Profile.vue 头像上传接 `/upload/image` + `PUT /users/profile` 持久化；新增 `GET /users/profile` 拉真实资料；为 User 增 gender/birthday 列并持久化；订单统计改为分页汇总；手机「更换」改只读说明。
  - #133 user-service 登录支持用户名/邮箱/手机号三标识（不破坏 UserLoginVO 形态）；Login.vue 文案校正（记住账号、邮箱找回）。
- Out of scope:
  - 手机号验证码找回 / 在线换绑手机（后端无短信渠道，文案降级为不支持）。
  - `GET /users/{id}` 返回裸 User（含 password）的既有泄露问题（既有 endpoint，不在本单位协调范围）。

## Related Artifacts
- Spec: none —— 验收点已在 issue 描述内明确，bug 修复无需独立 spec。
- Plan: none —— 单 PR 范围、步骤直观，无需多步计划。

## Likely Files
- `hmall-web/src/views/FavoriteList.vue`、`hmall-web/src/views/Profile.vue`、`hmall-web/src/views/Login.vue`
- `hmall-web/src/api/user.js`，新增 hmall-web/src/utils 下 cartFromFavorite/profileStats/avatarUpload 三个 js
- `user-service/src/main/java/com/hmall/controller/UserController.java`、`user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`、`user-service/src/main/java/com/hmall/domain/po/User.java`、`user-service/src/main/java/com/hmall/domain/vo/UserVO.java`
- `docs/sql/user-service-profile-fields.sql`、`docs/sql/init-all-tables.sql`、`user-service/src/test/resources/sql/schema.sql`

## Runtime Evidence To Inspect First
- `hmall-web/src/api/cart.js`（addToCart 形态）、`ItemDetail.vue`（cartStore.addItem payload 范式）。
- `hm-gateway/src/main/resources/application.yaml` excludePaths（确认 /users/profile 需鉴权）。
- `file-service/src/main/java/com/hmall/file/controller/FileController.java`（/upload/image 返回 id 与 url）。

## Safety Constraints
- 不破坏 `UserLoginVO` 返回形态（登录契约）。
- 新增 endpoint 遵循 `R<Void>`(写) + `*VO`(读)：`GET /users/profile` 返 `UserVO`。
- 不硬编码密钥；头像走 file-service 现有 MinIO 能力。
- 数据库变更用增量 SQL 脚本，不改写既有迁移。

## Worktree Or Branch
- `task/2026-06-06-cwh-account`（worktree: `.worktrees/task-2026-06-06-cwh-account`）

## Open Questions
- none
