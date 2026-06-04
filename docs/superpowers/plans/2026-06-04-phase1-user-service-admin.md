# Phase 1: user-service Admin Endpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 user-service 补齐管理端用户管理与个人中心功能，实现 B1 `/admin/users` 列表/状态/详情 + B5 profile/permissions 全部端点。

**Architecture:** 在 user-service 中新增 AdminUserController（或扩展现有 UserController），提供管理端用户管理功能。新增 UserVO 脱敏类（不含 password），遵循 R<Void>（写）+ *VO/PageDTO（读）约定。复用现有 user.status 字段（1=正常/2=冻结），Gateway `/admin/**` 路径已有角色校验。

**Tech Stack:** Spring Boot 2.7.12, Spring Cloud Alibaba 2021.0.4.0, MyBatis-Plus, MySQL 8, JUnit 5, Mockito, Testcontainers (MySQL + Redis)。

---

## 关键现状与对 spec 的偏差（实现前必读）

1. **status 字段已存在**：`User` PO 已有 `status` 字段（`UserStatus` 枚举：FROZEN=0, NORMAL=1）。spec §3 指出需复用现有字段，值用 1/2（而非 0/1）。**需调整枚举值**：FROZEN=2, NORMAL=1。
2. **GET /users/{id} 返回含 password 的 PO**：存在安全隐患。spec §3 要求新增脱敏 `UserVO`。**新增 UserVO**，管理端详情使用 UserVO。
3. **PUT /users/profile 部分实现**：现有实现仅支持修改 nickname/avatar/email，不支持改密码，不支持管理员操作。**需扩展**支持密码修改和管理员操作。
4. **Gateway 鉴权已就绪**：`AuthGlobalFilter` 第 49-53 行已实现 `/admin/**` 路径的角色校验（检查 token 中 role 是否为 "admin"）。后端端点只需实现业务逻辑。
5. **分页查询基础**：MyBatis-Plus 已集成，可使用 `Page<T>` 进行分页查询。

## File Structure

- Create `user-service/src/main/java/com/hmall/domain/vo/UserVO.java` — 脱敏用户 VO（不含 password）
- Modify `user-service/src/main/java/com/hmall/domain/po/User.java` — 调整 UserStatus 枚举值（FROZEN=2, NORMAL=1）
- Modify `user-service/src/main/java/com/hmall/enums/UserStatus.java` — 调整枚举值
- Create `user-service/src/main/java/com/hmall/controller/AdminUserController.java` — 管理端用户管理 Controller
- Modify `user-service/src/main/java/com/hmall/service/IUserService.java` — 新增 admin 相关方法
- Modify `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java` — 实现 admin 相关方法
- Create `user-service/src/test/java/com/hmall/controller/AdminUserControllerTest.java` — Controller 单元测试
- Create `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java` — Service 单元测试（扩展现有测试）
- Modify `docs/knowledge-base/modules/user-service.md` — 更新 KB 页面

---

## Tasks

### Task 0: 建分支与 harness 任务记录（Safety 硬约束）

**Files:**
- Create: `docs/agent-harness/tasks/active/2026-06-04-phase1-user-service-admin/{task.yaml,context.md,verification.md,audit.md,handoff.md}`

- [x] **Step 1:** 从 `main` 切分支。

```bash
git switch -c task/2026-06-04-phase1-user-service-admin origin/main
```

- [x] **Step 2:** 填 `task.yaml`：`status: active`、`spec`/`plan` 指向本文件、`task_branch: task/2026-06-04-phase1-user-service-admin`。填 context/verification/audit/handoff 叙述。

- [ ] **Step 3:** `python3 scripts/agent_harness.py check` → Exit 0。

- [ ] **Step 4: Commit** `chore(harness): phase1 user-service admin task record`。

### Task 1: UserVO 脱敏类 + UserStatus 枚举调整

**Files:**
- Create: `user-service/src/main/java/com/hmall/domain/vo/UserVO.java`
- Modify: `user-service/src/main/java/com/hmall/enums/UserStatus.java`

- [ ] **Step 1:** 创建 `UserVO` 类，包含 User PO 中除 password 外的所有字段：

```java
package com.hmall.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "用户脱敏 VO")
public class UserVO {
    @ApiModelProperty("用户id")
    private Long id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码（脱敏，不返回）")
    private String password;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("详细信息")
    private String info;

    @ApiModelProperty("用户状态：1正常，2冻结")
    private Integer status;

    @ApiModelProperty("用户余额")
    private Integer balance;

    @ApiModelProperty("用户角色")
    private String role;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2:** 调整 `UserStatus` 枚举值，将 FROZEN=0 改为 FROZEN=2，NORMAL=1 保持不变：

```java
package com.hmall.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结");

    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;
}
```

- [ ] **Step 3:** 验证现有代码中 UserStatus 的使用，确保调整枚举值不会破坏现有功能。

- [ ] **Step 4: Commit** `feat(user-service): add UserVO and adjust UserStatus enum`。

### Task 2: UserService 接口扩展

**Files:**
- Modify: `user-service/src/main/java/com/hmall/service/IUserService.java`

- [ ] **Step 1:** 在 `IUserService` 接口中新增以下方法：

```java
/**
 * 分页查询用户列表（管理端）
 * @param page 页码
 * @param size 每页大小
 * @param keyword 搜索关键词（模糊匹配 username/phone）
 * @return 用户分页数据
 */
PageDTO<UserVO> queryUsersPage(Integer page, Integer size, String keyword);

/**
 * 修改用户状态（管理端）
 * @param userId 用户ID
 * @param status 状态值（1=正常，2=冻结）
 */
void updateUserStatus(Long userId, Integer status);

/**
 * 获取用户详情（管理端，脱敏）
 * @param userId 用户ID
 * @return 用户脱敏信息
 */
UserVO getUserDetail(Long userId);

/**
 * 更新用户个人资料（支持密码修改）
 * @param profile 用户资料
 */
void updateProfileWithPassword(User profile);

/**
 * 获取管理员权限码列表
 * @return 权限码列表
 */
List<String> getAdminPermissions();
```

- [ ] **Step 2:** 确保导入必要的类（`PageDTO`、`UserVO`、`List`）。

- [ ] **Step 3: Commit** `feat(user-service): extend IUserService with admin methods`。

### Task 3: UserService 实现

**Files:**
- Modify: `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`

- [ ] **Step 1:** 实现 `queryUsersPage` 方法：

```java
@Override
public PageDTO<UserVO> queryUsersPage(Integer page, Integer size, String keyword) {
    // 构建查询条件
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.isNotBlank(keyword)) {
        wrapper.like(User::getUsername, keyword)
               .or()
               .like(User::getPhone, keyword);
    }
    
    // 执行分页查询
    Page<User> pageParam = new Page<>(page, size);
    Page<User> result = userMapper.selectPage(pageParam, wrapper);
    
    // 转换为 UserVO（脱敏）
    List<UserVO> voList = result.getRecords().stream()
            .map(this::convertToUserVO)
            .collect(Collectors.toList());
    
    return new PageDTO<>(result.getTotal(), voList);
}
```

- [ ] **Step 2:** 实现 `updateUserStatus` 方法：

```java
@Override
public void updateUserStatus(Long userId, Integer status) {
    // 验证状态值
    if (status != UserStatus.NORMAL.getValue() && status != UserStatus.FROZEN.getValue()) {
        throw new BadRequestException("无效的用户状态");
    }
    
    // 查询用户
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BadRequestException("用户不存在");
    }
    
    // 更新状态
    user.setStatus(status);
    userMapper.updateById(user);
}
```

- [ ] **Step 3:** 实现 `getUserDetail` 方法：

```java
@Override
public UserVO getUserDetail(Long userId) {
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BadRequestException("用户不存在");
    }
    return convertToUserVO(user);
}
```

- [ ] **Step 4:** 实现 `updateProfileWithPassword` 方法：

```java
@Override
public void updateProfileWithPassword(User profile) {
    // 获取当前登录用户
    Long userId = UserContext.getUser();
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BadRequestException("用户不存在");
    }
    
    // 更新基本信息
    if (profile.getNickname() != null) {
        user.setNickname(profile.getNickname());
    }
    if (profile.getAvatar() != null) {
        user.setAvatar(profile.getAvatar());
    }
    if (profile.getEmail() != null) {
        user.setEmail(profile.getEmail());
    }
    
    // 更新密码（如果提供）
    if (StringUtils.isNotBlank(profile.getPassword())) {
        // 加密密码
        String encodedPassword = BCrypt.hashpw(profile.getPassword(), BCrypt.gensalt());
        user.setPassword(encodedPassword);
    }
    
    userMapper.updateById(user);
}
```

- [ ] **Step 5:** 实现 `getAdminPermissions` 方法：

```java
@Override
public List<String> getAdminPermissions() {
    // 获取当前登录用户
    Long userId = UserContext.getUser();
    User user = userMapper.selectById(userId);
    if (user == null) {
        throw new BadRequestException("用户不存在");
    }
    
    // 基于角色返回权限码
    List<String> permissions = new ArrayList<>();
    if ("admin".equals(user.getRole())) {
        // 管理员拥有所有权限
        permissions.add("user:manage");
        permissions.add("item:manage");
        permissions.add("order:manage");
        permissions.add("coupon:manage");
        permissions.add("notification:manage");
        permissions.add("feedback:manage");
        permissions.add("banner:manage");
        permissions.add("dashboard:view");
    } else {
        // 普通用户无管理权限
        // 可以根据需求扩展更细粒度的权限
    }
    
    return permissions;
}
```

- [ ] **Step 6:** 添加私有辅助方法 `convertToUserVO`：

```java
private UserVO convertToUserVO(User user) {
    UserVO vo = new UserVO();
    vo.setId(user.getId());
    vo.setUsername(user.getUsername());
    // 不设置 password，实现脱敏
    vo.setPhone(user.getPhone());
    vo.setInfo(user.getInfo());
    vo.setStatus(user.getStatus());
    vo.setBalance(user.getBalance());
    vo.setRole(user.getRole());
    vo.setCreateTime(user.getCreateTime());
    vo.setUpdateTime(user.getUpdateTime());
    return vo;
}
```

- [ ] **Step 7:** 确保导入必要的类（`BCrypt`、`StringUtils`、`UserContext`、`BadRequestException` 等）。

- [ ] **Step 8: Commit** `feat(user-service): implement admin methods in UserServiceImpl`。

### Task 4: AdminUserController 实现

**Files:**
- Create: `user-service/src/main/java/com/hmall/controller/AdminUserController.java`

- [ ] **Step 1:** 创建 `AdminUserController` 类：

```java
package com.hmall.controller;

import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.domain.vo.UserVO;
import com.hmall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端用户管理")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IUserService userService;

    @ApiOperation("分页查询用户列表")
    @GetMapping
    public PageDTO<UserVO> queryUsersPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword) {
        return userService.queryUsersPage(page, size, keyword);
    }

    @ApiOperation("修改用户状态")
    @PutMapping("/{id}/status")
    public R<Void> updateUserStatus(
            @ApiParam("用户ID") @PathVariable Long id,
            @ApiParam("状态值") @RequestBody Integer status) {
        userService.updateUserStatus(id, status);
        return R.ok();
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    public UserVO getUserDetail(
            @ApiParam("用户ID") @PathVariable Long id) {
        return userService.getUserDetail(id);
    }
}
```

- [ ] **Step 2:** 创建 `AdminProfileController` 类（或扩展现有 UserController）：

```java
package com.hmall.controller;

import com.hmall.common.domain.R;
import com.hmall.domain.po.User;
import com.hmall.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端个人中心")
@RestController
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final IUserService userService;

    @ApiOperation("修改管理员信息/密码")
    @PutMapping
    public R<Void> updateProfile(@RequestBody User profile) {
        userService.updateProfileWithPassword(profile);
        return R.ok();
    }

    @ApiOperation("获取管理员权限码")
    @GetMapping("/permissions")
    public List<String> getPermissions() {
        return userService.getAdminPermissions();
    }
}
```

- [ ] **Step 3:** 考虑是否需要修改现有 `UserController` 中的 `PUT /users/profile` 端点。根据 spec，管理员改信息/密码应该复用 `PUT /users/profile`（需确认 adminToken 是否被 user-service 接受）。如果 adminToken 不通，则新增 `/admin/profile`。

- [ ] **Step 4: Commit** `feat(user-service): add AdminUserController and AdminProfileController`。

### Task 5: 单元测试

**Files:**
- Create: `user-service/src/test/java/com/hmall/controller/AdminUserControllerTest.java`
- Create: `user-service/src/test/java/com/hmall/controller/AdminProfileControllerTest.java`
- Modify: `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`（扩展现有测试）

- [ ] **Step 1:** 创建 `AdminUserControllerTest` 测试类：

```java
package com.hmall.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmall.common.domain.PageDTO;
import com.hmall.domain.vo.UserVO;
import com.hmall.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void queryUsersPage_shouldReturnPageDTO() throws Exception {
        // Given
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        PageDTO<UserVO> pageDTO = new PageDTO<>(1L, Arrays.asList(userVO));
        when(userService.queryUsersPage(1, 10, "test")).thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].username").value("testuser"));

        verify(userService).queryUsersPage(1, 10, "test");
    }

    @Test
    void updateUserStatus_shouldReturnOk() throws Exception {
        // Given
        doNothing().when(userService).updateUserStatus(1L, 2);

        // When & Then
        mockMvc.perform(put("/admin/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("2"))
                .andExpect(status().isOk());

        verify(userService).updateUserStatus(1L, 2);
    }

    @Test
    void getUserDetail_shouldReturnUserVO() throws Exception {
        // Given
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setUsername("testuser");
        when(userService.getUserDetail(1L)).thenReturn(userVO);

        // When & Then
        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserDetail(1L);
    }
}
```

- [ ] **Step 2:** 创建 `AdminProfileControllerTest` 测试类。

- [ ] **Step 3:** 扩展 `UserServiceImplTest` 测试类，覆盖新增的 admin 方法。

- [ ] **Step 4:** 运行测试验证覆盖率。

- [ ] **Step 5: Commit** `test(user-service): add unit tests for admin endpoints`。

### Task 6: 集成测试（可选）

**Files:**
- Create: `user-service/src/test/java/com/hmall/it/AdminUserIT.java`

- [ ] **Step 1:** 创建集成测试类，使用 Testcontainers（MySQL + Redis）验证端点行为。

- [ ] **Step 2:** 测试端点契约：
  - `GET /admin/users` 返回 `PageDTO<UserVO>`，支持 keyword 模糊匹配
  - `PUT /admin/users/{id}/status` 修改用户状态
  - `GET /admin/users/{id}` 返回脱敏的 `UserVO`
  - `PUT /admin/profile` 支持管理员修改信息/密码
  - `GET /admin/profile/permissions` 返回权限码列表

- [ ] **Step 3:** 验证 Gateway `/admin/**` 鉴权集成。

- [ ] **Step 4: Commit** `test(user-service): add integration tests for admin endpoints`。

### Task 7: Knowledge Base 更新

**Files:**
- Modify: `docs/knowledge-base/modules/user-service.md`

- [ ] **Step 1:** 更新 `user-service.md` 页面，添加新增端点的契约说明。

- [ ] **Step 2:** 更新 `last_synced_commit` 到当前 HEAD。

- [ ] **Step 3:** 运行 `python3 scripts/knowledge_base.py check` → Exit 0。

- [ ] **Step 4: Commit** `docs(kb): update user-service module page with admin endpoints`。

### Task 8: 工程化检查与收尾

**Files:**
- Modify: `docs/agent-harness/tasks/active/2026-06-04-phase1-user-service-admin/audit.md`

- [ ] **Step 1:** 运行 `python3 scripts/agent_harness.py check` → Exit 0。

- [ ] **Step 2:** 运行 `python3 scripts/knowledge_base.py check` → Exit 0。

- [ ] **Step 3:** 运行 `python3 scripts/engineering-lint.py` → Exit 0。

- [ ] **Step 4:** 更新 `audit.md`，标记所有审计项为 ✅。

- [ ] **Step 5:** 更新 `handoff.md`，标记状态为"完成"。

- [ ] **Step 6: Commit** `chore(harness): complete phase1 user-service admin task`。

---

## Testing Strategy

### Unit Tests
- **UserController**: 测试所有新增端点的方法调用和返回值
- **UserService**: 测试 admin 相关业务逻辑（分页查询、状态修改、权限派生）
- **UserVO**: 验证脱敏逻辑（不含 password 字段）

### Integration Tests
- **端点契约验证**: 使用 Spring Boot Test + Testcontainers 验证端点行为
- **数据库操作**: 验证 status 字段修改、分页查询正确性
- **权限验证**: 验证 `/admin/*` 端点的 Gateway 鉴权集成

## Acceptance Criteria

- [ ] 所有 5 个端点实现并可访问
- [ ] 所有端点遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- [ ] UserVO 不包含 password 字段
- [ ] 单元测试通过，覆盖率 ≥ 80%
- [ ] 集成测试通过（如果启用）
- [ ] 不破坏现有用户端点（`/users/*`）
- [ ] Gateway `/admin/**` 鉴权正常工作
- [ ] Knowledge base 同步完成
- [ ] Harness 任务文档完整