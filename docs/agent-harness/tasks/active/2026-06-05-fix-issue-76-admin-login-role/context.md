# Context: Fix Issue #76 — Admin Login Role Missing in VO

## Objective

Add the `role` field to `UserLoginVO` so that `hmall-admin` Login.vue can correctly
distinguish admin accounts from ordinary users on login.

## Scope

- In scope: `UserLoginVO.java` (add `role` field), `UserServiceImpl.login()` (set role
  on VO), `UserServiceImplTest` (assert role in login tests).
- Out of scope: JWT claim changes, gateway routing, login endpoint response envelope
  shape, any other service.

## Related Artifacts

- Spec: none — root cause is a single missing field, no separate spec required.
- Plan: none — two-line change in production code, no multi-step plan required.
- Issue: #76

## Likely Files

- `user-service/src/main/java/com/hmall/domain/vo/UserLoginVO.java`
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`

## Runtime Evidence To Inspect First

- `docs/sql/init-all-tables.sql` — confirms admin user has `role = 'admin'`, matching
  the frontend's `data.role !== 'admin'` check exactly (no case mismatch).

## Safety Constraints

- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Do not change the login endpoint URL, HTTP method, or other VO fields.
- Adding a new field to an existing JSON response is backward-compatible.

## Worktree Or Branch

- `task/2026-06-05-fix-issue-76-admin-login-role`

## Open Questions

- none
