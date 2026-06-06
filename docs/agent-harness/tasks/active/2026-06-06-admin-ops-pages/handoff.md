# Handoff: Admin Ops Pages

## Status
Ready for PR; local focused tests, module tests, frontend build/lint, harness, KB, and engineering lint have passed. Keep this task active after PR merge per user request and do not archive it in this branch.

## Files Changed
- `hmall-admin/src/views/UserList.vue`
- `hmall-admin/src/views/ReviewList.vue`
- `hmall-admin/src/views/Profile.vue`
- `hmall-admin/src/utils/adminOpsActions.js`
- `hmall-admin/src/__tests__/adminOpsActions.spec.js`
- `user-service/src/main/java/com/hmall/controller/AdminUserController.java`
- `user-service/src/main/java/com/hmall/controller/AdminProfileController.java`
- `user-service/src/main/java/com/hmall/domain/dto/AdminPasswordUpdateDTO.java`
- `user-service/src/main/java/com/hmall/service/IUserService.java`
- `user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java`
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java`
- `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`
- `item-service/src/main/java/com/hmall/item/service/IItemReviewService.java`
- `item-service/src/main/java/com/hmall/item/service/impl/ItemReviewServiceImpl.java`
- `item-service/src/test/java/com/hmall/item/service/impl/ItemReviewServiceImplTest.java`
- `docs/knowledge-base/modules/hmall-admin.md`
- `docs/knowledge-base/modules/user-service.md`
- `docs/knowledge-base/modules/item-service.md`
- `docs/knowledge-base/flows/auth-and-gateway-flow.md`
- `docs/knowledge-base/flows/order-checkout-flow.md`

## Commands Run
- `cd hmall-admin && npm ci`
- `cd hmall-admin && npm test -- --run src/__tests__/adminOpsActions.spec.js`
- `mvn -q -pl user-service -am -Dtest=UserServiceImplTest -DfailIfNoTests=false test`
- `mvn -q -pl item-service -am -Dtest=ItemReviewServiceImplTest -DfailIfNoTests=false test`
- `cd hmall-admin && npm test`
- `cd hmall-admin && npm run build`
- `cd hmall-admin && npm run lint`
- `mvn -q -pl item-service -am test`
- `mvn -q -pl user-service -am test`
- `python3 scripts/agent_harness.py check`
- `python3 scripts/knowledge_base.py check --base origin/main`
- `python3 scripts/engineering-lint.py`

## Known Risks
- `AdminPasswordUpdateDTO` extends existing `User` payload shape to preserve the current service update path while adding `currentPassword`.
- Avatar upload now persists and previews `profileForm.avatar`; any broken external image URL will fall back only through browser broken-image behavior.
- Login records remain unsupported and hidden; implementing them requires a separate backend contract.
- Nacos timeout logs can appear during user-service Spring context tests but did not fail the Maven run.

## Next Action
Run repo harness/KB/lint gates, commit, push, open PR, wait for CI and codex-review, merge, delete the remote branch, and leave the task active for later unified archival.

## Worktree Or Branch
- `task/2026-06-06-admin-ops-pages`
