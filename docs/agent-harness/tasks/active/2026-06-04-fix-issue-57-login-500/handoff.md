# Handoff: Fix Issue 57 Login 500

## Status
Completed.

## Files Changed
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`
- `docs/knowledge-base/modules/user-service.md`
- `docs/knowledge-base/flows/auth-and-gateway-flow.md`

## Commands Run
- `mvn -q -pl user-service -am test` —— 全部通过

## Known Risks
- 无。变更仅影响异常类型与文案，不改变正常登录流程。

## Next Action
等待 CI 通过并合并 PR。

## Worktree Or Branch
- `task/2026-06-04-fix-issue-57-login-500`
