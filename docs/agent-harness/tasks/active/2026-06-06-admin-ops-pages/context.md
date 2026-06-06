# Context: Admin Ops Pages

## Objective
Align admin user management, review management, and profile pages with real backend contracts and remove unsupported fake actions.

## Scope
- In scope:
  - Issue #135: user list pagination/filter parameters and unsupported export/new-user actions.
  - Issue #139: review list pagination/tab filters and unsupported reply/static approval state.
  - Issue #143: admin profile avatar upload and password update current-password validation.
- Out of scope:
  - Login-record persistence/display; no backend contract exists in this repo.
  - Review reply moderation workflow; no backend contract exists in this repo.
  - User creation/export workflow; no backend contract exists in this repo.

## Related Artifacts
- Spec: none - GitHub issues #135/#139/#143 are bounded bugfix acceptance criteria.
- Plan: none - grouped issue fix is small enough to track in this harness record.

## Likely Files
- `hmall-admin/src/views/UserList.vue`
- `hmall-admin/src/views/ReviewList.vue`
- `hmall-admin/src/views/Profile.vue`
- `hmall-admin/src/utils/adminOpsActions.js`
- `user-service/src/main/java/com/hmall/controller/AdminUserController.java`
- `user-service/src/main/java/com/hmall/controller/AdminProfileController.java`
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`
- `item-service/src/main/java/com/hmall/item/service/impl/ItemReviewServiceImpl.java`

## Runtime Evidence To Inspect First
- GitHub issues #135, #139, and #143.
- Existing admin API calls in `hmall-admin/src/api/`.
- Current controller parameter names in `user-service` and `item-service`.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Preserve existing endpoint compatibility: add optional filters/validation without changing successful response shapes.
- Keep unsupported controls hidden rather than wiring demo-only behavior.

## Worktree Or Branch
- `task/2026-06-06-admin-ops-pages`

## Open Questions
- none
