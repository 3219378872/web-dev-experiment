# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| `UserLoginVO` has `role` field | done | Field added as `private String role;` |
| `login()` sets `vo.setRole(role)` | done | `role` variable already computed from `user.getRole()` with fallback to `"user"` |
| DB admin user `role` value matches frontend check | confirmed | `init-all-tables.sql` inserts `role='admin'`; Login.vue checks `data.role !== 'admin'` — exact match |
| Unit tests assert role in login VO | done | `login_success` asserts `role == "user"`, new `login_adminUser_roleIsAdmin` asserts `role == "admin"`, `login_nullRole_defaultsToUser` asserts `role == "user"` |
| No other endpoint shapes changed | confirmed | Only VO and impl touched; login URL/method/envelope unchanged |
| No secrets committed | confirmed | No credentials in changed files |
