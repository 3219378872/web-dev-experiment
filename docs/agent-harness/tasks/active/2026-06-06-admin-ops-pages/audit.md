# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| #135 uses backend pagination contract | done | `UserList.vue` maps `pageSize` to backend `size` via `userQueryParams`; focused Vitest covers the mapping. |
| #135 status/email filters are real | done | `AdminUserController` accepts optional `status`; `UserServiceImpl.queryUsersPage` filters status and matches keyword against username/phone/email; service tests cover email and status filters. |
| #135 fake user actions removed | done | Unsupported export and new-user buttons were removed from `UserList.vue`. |
| #139 review tab filters are backend-backed | done | `ReviewList.vue` sends page/size plus rating range params; `ReviewController` and `ItemReviewServiceImpl` support exact rating and min/max ranges; service tests cover range filtering. |
| #139 fake review actions removed | done | Static approved status and unsupported reply action were removed from `ReviewList.vue`. |
| #143 avatar upload is wired | done | `Profile.vue` loads persisted admin profile via `GET /admin/profile`, uploads avatar with existing `POST /upload/image` helper, then persists avatar through `PUT /admin/profile`; helper tests cover payload shape and server-profile application. |
| #143 password update validates current password | done | `Profile.vue` sends `currentPassword`; `AdminPasswordUpdateDTO` carries it; `UserServiceImpl.updateProfileWithPassword` rejects missing/wrong current password before storing a new BCrypt hash; service tests cover both failures and success. |
| #143 login records are not faked | done | Static recent-login card was removed from `Profile.vue`; no new login-record backend was invented. |
| Harness remains active by request | done | User requested no archival; task stays under `docs/agent-harness/tasks/active/` with `status: active`. |
