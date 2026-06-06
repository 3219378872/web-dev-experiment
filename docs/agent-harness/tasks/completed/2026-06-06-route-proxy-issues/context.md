# Context: Route Proxy Issues

## Objective
Fix the currently open route/proxy GitHub issues so uploaded image URLs resolve in both frontends and the admin category list reaches item-service through Gateway.

## Scope
- In scope: GitHub issues #155 and #156; `hmall-web` and `hmall-admin` Vite dev proxy and production nginx config; `scripts/init-nacos-routes.sh` item-service route seed; focused regression tests and KB notes for the touched surfaces.
- Out of scope: changing `FileController` URL shape, changing frontend image render code, changing the admin category controller contract, or editing live Nacos outside the repo.

## Related Artifacts
- Spec: none; the two GitHub issue bodies already define symptoms, root cause, and expected route/proxy contracts.
- Plan: none; this is a narrow bugfix with TDD coverage and no new architecture.

## Likely Files
- `hmall-web/vite.config.js`
- `hmall-web/nginx.conf`
- `hmall-web/src/__tests__/proxyConfig.spec.js`
- `hmall-admin/vite.config.js`
- `hmall-admin/nginx.conf`
- `hmall-admin/src/__tests__/proxyConfig.spec.js`
- `scripts/init-nacos-routes.sh`
- `scripts/tests/test_init_nacos_routes.py`
- `docs/knowledge-base/modules/hmall-web.md`
- `docs/knowledge-base/modules/hmall-admin.md`
- `docs/knowledge-base/modules/hm-gateway.md`

## Runtime Evidence To Inspect First
- Issue #155: `FileController.uploadImage()` returns root-relative `/files/<key>` URLs; both frontend dev/prod configs only forwarded `/api`, so browser image requests to `/files/**` were handled by the frontend origin instead of Gateway.
- Issue #156: `CategoryController` exposes `GET /admin/categories`, but the Nacos route seed for item-service did not include `/admin/categories/**`, so Gateway could not route the admin category list endpoint.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Keep file-service returning `/files/<key>`; fix frontend and Gateway routing around the existing public URL contract.
- Keep admin category API response shape unchanged; only seed the missing Gateway path.

## Worktree Or Branch
- `task/2026-06-06-route-proxy-issues`

## Open Questions
- none
