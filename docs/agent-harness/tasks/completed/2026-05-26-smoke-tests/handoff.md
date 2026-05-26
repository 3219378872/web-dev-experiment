# Handoff: Smoke Tests

## Status
Done — merged via PR #5 at 2026-05-26T09:08:06Z; remote branch deleted by pr-cleanup.yml; task archived to completed/ by PR #12.

## Files Changed
- `scripts/smoke/smoke.sh`（新增）
- `.github/workflows/ci.yml`（更新 smoke job）
- `docs/agent-harness/tasks/active/2026-05-26-smoke-tests/`（新增）

## Commands Run
- `bash -n scripts/smoke/smoke.sh`（语法检查）

## Known Risks
- CI docker compose up 需约 60s 启动（Nacos 最长 40s）
- 种子数据 testuser/admin123 必须在数据库中存在
- gateway-routes.json 在 Nacos 中注册，本地启动时需确保 Nacos 有路由配置

## Next Action
1. 更新 CI smoke job
2. 全量 lint 验证
3. push + PR
