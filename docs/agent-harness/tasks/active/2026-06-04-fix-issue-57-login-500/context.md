# Context: Fix Issue 57 Login 500

## Objective
修复 user-service 登录接口：用户名不存在时返回 500 的问题，改为返回 400 BadRequestException，与密码错误分支保持一致。

## Scope
- In scope:
  - UserServiceImpl.login 异常类型调整
  - 对应单测断言更新
  - 知识库 K005 同步
- Out of scope:
  - 其他接口响应码调整
  - 前端改动

## Related Artifacts
- Spec: null —— 单文件 bug 修复，无需独立 spec。
- Plan: null —— 变更范围单一，无需独立 plan。

## Likely Files
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`

## Runtime Evidence To Inspect First
- 复现命令：`curl -X POST -H 'Content-Type: application/json' -d '{"username":"ghost","password":"x"}' http://localhost:8080/users/login`

## Safety Constraints
- 不暴露用户枚举信息（统一文案为“用户名或密码错误”）。
- 不破坏现有成功路径的响应结构。

## Worktree Or Branch
- `task/2026-06-04-fix-issue-57-login-500`

## Open Questions
- none
