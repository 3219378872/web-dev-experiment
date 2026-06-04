# Verification

## Test Strategy

### Unit Tests
- **UserController**: 测试所有新增端点的方法调用和返回值
- **UserService**: 测试 admin 相关业务逻辑（分页查询、状态修改、权限派生）
- **UserVO**: 验证脱敏逻辑（不含 password 字段）

### Integration Tests
- **端点契约验证**: 使用 Spring Boot Test + Testcontainers 验证端点行为
- **数据库操作**: 验证 status 字段修改、分页查询正确性
- **权限验证**: 验证 `/admin/*` 端点的 Gateway 鉴权集成

## Verification Commands

```bash
# 1. 单元测试
mvn -B -ntp -pl user-service test

# 2. 集成测试（需要 Docker）
mvn -B -ntp -pl user-service verify -Pintegration

# 3. 全模块构建
mvn -B -ntp verify -DskipIntegrationTests

# 4. 端点功能验证（需要运行服务）
# GET /admin/users?page=1&size=10&keyword=test
# PUT /admin/users/{id}/status (body: {"status": 2})
# GET /admin/users/{id}
# PUT /users/profile (body: {"nickname": "new", "password": "new"})
# GET /admin/profile/permissions
```

## Expected Results

### 端点行为
1. `GET /admin/users` 返回 `PageDTO<UserVO>`，支持 keyword 模糊匹配 username/phone
2. `PUT /admin/users/{id}/status` 修改用户状态（1=正常/2=冻结），返回 `R<Void>`
3. `GET /admin/users/{id}` 返回脱敏的 `UserVO`（不含 password）
4. `PUT /users/profile` 支持管理员修改信息/密码，返回 `R<Void>`
5. `GET /admin/profile/permissions` 返回基于角色的权限码列表

### 测试覆盖率
- 新增代码行覆盖率 ≥ 80%
- 新增分支覆盖率 ≥ 80%

## Acceptance Criteria

- [ ] 所有 5 个端点实现并可访问
- [ ] 所有端点遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- [ ] UserVO 不包含 password 字段
- [ ] 单元测试通过，覆盖率达标
- [ ] 集成测试通过（如果启用）
- [ ] 不破坏现有用户端点（`/users/*`）
- [ ] Gateway `/admin/**` 鉴权正常工作

## Evidence Collection

- 测试输出日志
- JaCoCo 覆盖率报告
- 端点调用示例（curl/httpie）
- 代码变更 diff