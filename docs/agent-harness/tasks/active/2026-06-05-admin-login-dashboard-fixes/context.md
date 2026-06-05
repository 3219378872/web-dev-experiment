# Context: Admin Login Dashboard Fixes

## Objective
Fix 6 admin-side issues: login page static captcha/role picker/fake stats, dashboard mock data, order management checkbox/tab counts/pagination, review stats, profile backend connection, notification/feedback field and pagination mismatches.

## Scope
- In scope:
  - #112: Login.vue - dynamic canvas captcha, remove role picker, remove fake stats
  - #101: Dashboard.vue - connect to /admin/dashboard API (summary, trend, category-share, top-items, 待办, latest-orders)
  - #97: OrderList.vue - fix checkbox (el-checkbox), global tab counts, pagination range limit
  - #98: ReviewList.vue - real stats (avg rating, good rate, bad count), fix username/images mapping, server-side pagination
  - #104: Profile.vue - connect to /admin/profile APIs, load from localStorage
  - #109: NotificationList.vue/FeedbackList.vue - pass page/size params, remove non-existent fields
  - New API files: dashboard.js, profile.js
  - Update item.js getReviews to accept params
  - Update KB module hmall-admin.md
- Out of scope:
  - Backend API changes (all endpoints already exist)
  - E2E tests
  - AdminLayout changes

## Related Artifacts
- Spec: none - fixes are straightforward bug fixes against existing APIs
- Plan: none - single PR covering 6 related frontend issues

## Likely Files
- `hmall-admin/src/views/Login.vue`
- `hmall-admin/src/views/Dashboard.vue`
- `hmall-admin/src/views/OrderList.vue`
- `hmall-admin/src/views/ReviewList.vue`
- `hmall-admin/src/views/Profile.vue`
- `hmall-admin/src/views/NotificationList.vue`
- `hmall-admin/src/views/FeedbackList.vue`
- `hmall-admin/src/api/dashboard.js` (new)
- `hmall-admin/src/api/profile.js` (new)
- `hmall-admin/src/api/item.js`
- `docs/knowledge-base/modules/hmall-admin.md`

## Runtime Evidence To Inspect First
- 直接查看各 Vue 文件中的硬编码文本和本地数组

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.

## Worktree Or Branch
- `task/2026-06-05-admin-login-dashboard-fixes`

## Open Questions
- 无
