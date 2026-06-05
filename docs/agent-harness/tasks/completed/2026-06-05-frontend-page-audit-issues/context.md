# Context: Frontend Page Audit Issues

## Objective
Audit every hmall-web and hmall-admin page against docs/front-spec.md, current frontend wiring,
and backend Controller implementation, then submit page-level improvement issues for gaps.

## Scope
- In scope: frontend page audit, API wiring review, backend endpoint existence check, GitHub issue submission, harness evidence.
- Out of scope: implementing the fixes, changing API contracts, changing seed data.

## Related Artifacts
- Spec: `docs/front-spec.md` is the authoritative existing frontend requirement source; no new spec needed.
- Plan: waived because this is an audit/issue-submission task, not an implementation sequence.

## Likely Files
- `docs/front-spec.md`
- `hmall-web/src/router/index.js`
- `hmall-web/src/views/*.vue`
- `hmall-web/src/api/*.js`
- `hmall-admin/src/router/index.js`
- `hmall-admin/src/views/*.vue`
- `hmall-admin/src/api/*.js`
- Backend `*Controller.java` files under user/item/trade/notify/file/pay services.

## Runtime Evidence To Inspect First
- `gh issue list --state open` for already submitted frontend improvement issues.
- Frontend route/view source files.
- Backend Controller source files for true endpoint availability.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Do not alter business code in this task; submit audit issues only.

## Worktree Or Branch
- `task/2026-06-05-frontend-page-audit-issues`

## Open Questions
- none
