# Handoff: Route Proxy Issues

## Status
Implemented, pushed, and reviewed in PR 157. Local checks and PR CI passed on head `5f47b0f`; task record completed after codex-review passed.

## Files Changed
- `hmall-web/vite.config.js` and `hmall-admin/vite.config.js` add root-relative `/files` and `/upload` dev proxies to Gateway.
- `hmall-web/nginx.conf` and `hmall-admin/nginx.conf` add production `location /files/` and `location /upload/` reverse proxies to `hm-gateway`.
- `scripts/init-nacos-routes.sh` adds exact `/admin/categories` and wildcard `/admin/categories/**` to the item-service route seed.
- `hmall-web/src/__tests__/proxyConfig.spec.js` and `hmall-admin/src/__tests__/proxyConfig.spec.js` cover frontend dev/prod forwarding.
- `scripts/tests/test_init_nacos_routes.py` covers the Nacos route seed.
- KB pages for `hmall-web`, `hmall-admin`, and `hm-gateway` document the forwarding contracts.

## Commands Run
- `npm ci` in `hmall-web`
- `npm ci` in `hmall-admin`
- `npm test -- proxyConfig.spec.js` in `hmall-web` before and after fix
- `npm test -- proxyConfig.spec.js` in `hmall-admin` before and after fix
- `python3 -m pytest scripts/tests/test_init_nacos_routes.py -q` before and after fix
- `python3 scripts/knowledge_base.py check --base origin/main`
- `python3 scripts/agent_harness.py check`
- `python3 scripts/engineering-lint.py`
- `npm test` in `hmall-web`
- `npm test` in `hmall-admin`
- `npm run build` in `hmall-web`
- `npm run build` in `hmall-admin`
- `docker compose config -q`
- PR 157 CI: lint, test, integration, smoke, and codex-review all passed.

## Known Risks
- Existing npm audit warnings remain in both frontend dependency trees; this task did not change package dependencies.
- Updating a running local stack still requires publishing the revised `gateway-routes.json` seed to Nacos, or restarting the stack path that runs `scripts/init-nacos-routes.sh`.

## Next Action
Commit and push this completed-task lifecycle update, then merge PR 157 and verify issues 155 and 156 close.

## Worktree Or Branch
- `task/2026-06-06-route-proxy-issues`
