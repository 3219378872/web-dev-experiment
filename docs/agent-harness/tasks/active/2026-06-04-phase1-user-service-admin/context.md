# Context: Phase 1 - user-service Admin Endpoints

## Objective

实现 `docs/superpowers/specs/2026-06-04-backend-api-fulfillment-design.md` 中 Phase 1 的全部端点，为 user-service 补齐管理端用户管理与个人中心功能。

## Scope

- **In scope**: 
  - B1 管理端用户管理：`GET /admin/users`（列表/搜索/分页）、`PUT /admin/users/{id}/status`（启用/禁用）、`GET /admin/users/{id}`（详情）
  - B5 管理端个人中心：`PUT /users/profile`（管理员改信息/密码）、`GET /admin/profile/permissions`（权限码）
  - 新增 `UserVO` 脱敏类（不含 password）
- **Out of scope**: 
  - 其他 Phase（item-service、trade-service、notify-service）
  - 现有用户端点（`/users/*`）的修改（避免破坏现网调用）

## Current State

经代码核验，user-service 当前状态：
- **未实现**: `GET /admin/users`、`PUT /admin/users/{id}/status`、`GET /admin/users/{id}`、`GET /admin/profile/permissions`
- **部分实现**: `PUT /users/profile`（仅支持修改 nickname/avatar/email，不支持改密码，不支持管理员操作）
- **缺失**: `UserVO` 脱敏类（现有 `GET /users/{id}` 返回含 password 的 `User` PO，存在安全隐患）
- **已有基础**: `UserStatus` 枚举（FROZEN=0, NORMAL=1）、`User.status` 字段、Gateway `/admin/**` 角色校验

## Requirements

1. **端点契约**（来自 spec §5）：
   - `GET /admin/users?page&size&keyword` → `PageDTO<UserVO>`（keyword 模糊匹配 username/phone）
   - `PUT /admin/users/{id}/status`（body `{status}`）→ `R<Void>`（值 1/2）
   - `GET /admin/users/{id}` → `UserVO`（脱敏详情）
   - `PUT /users/profile` → `R<Void>`（管理员改信息/密码）
   - `GET /admin/profile/permissions` → `List<String>`（基于角色派生权限码）

2. **数据模型**：
   - 复用现有 `user.status` 字段（值 1=正常/2=冻结，而非 0/1）
   - 新增 `UserVO`（不含 password，其余字段与 User PO 一致）

3. **技术约束**：
   - 写操作返 `R<Void>`，读操作返 `*VO`/`PageDTO<T>`
   - `/admin/*` 端点依赖 Gateway 注入身份做鉴权
   - 不修改既有 endpoint 形态（避免破坏现网前端调用）

## Likely Files

- `user-service/src/main/java/com/hmall/controller/UserController.java`（现有 Controller，需扩展）
- `user-service/src/main/java/com/hmall/domain/vo/UserVO.java`（新增）
- `user-service/src/main/java/com/hmall/service/IUserService.java`（新增 admin 方法）
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`（实现 admin 方法）
- `user-service/src/main/java/com/hmall/domain/po/User.java`（可能需调整 status 枚举值）

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token
- 不破坏现有公共 API 响应封装
- 新增端点必须遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- 不修改既有 endpoint 形态（避免破坏现网前端调用）

## Worktree Or Branch

- `task/2026-06-04-phase1-user-service-admin`（从 main 切出）