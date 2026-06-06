# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| #155 uploaded `/files/**` URLs render in dev | done | Both frontend Vite configs proxy `/files` to `http://localhost:8080` with `changeOrigin` and no rewrite. |
| #155 upload endpoint remains reachable in dev | done | Both frontend Vite configs proxy `/upload` to Gateway with no `/api` rewrite. |
| #155 uploaded images render in production containers | done | Both frontend nginx configs include `location /files/` proxying to `http://hm-gateway:8080/files/`. |
| #155 upload endpoint remains reachable in production containers | done | Both frontend nginx configs include `location /upload/` proxying to `http://hm-gateway:8080/upload/`. |
| #156 admin category list routes through Gateway seed | done | `scripts/init-nacos-routes.sh` item-service Path args include exact `/admin/categories` and wildcard `/admin/categories/**`. |
| Existing backend contracts unchanged | done | No changes to `FileController`, `CategoryController`, DTOs, or response envelopes. |
| Regression coverage exists | done | Added frontend proxy config tests and `scripts/tests/test_init_nacos_routes.py`; all focused tests pass after the fix. |
