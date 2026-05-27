# Handoff: docker compose 全栈验证修复

## Status

本地验收完成。Smoke test 10/10 通过（从 Docker 网络内运行）。
待 push 远程、开 PR、过 CI 与 review。

## Files Changed

- `hm-gateway/src/main/resources/application.yaml`（加 `/categories/**` 到鉴权白名单）
- `docs/sql/init-all-tables.sql`（修复 user.status 类型 + 密码 hash）
- `scripts/init-nacos-routes.sh`（新增：Nacos gateway 路由初始化脚本）
- `.gitignore`（加 `docker/mysql/`）

## Commands Run

- `cmp CLAUDE.md AGENTS.md` → byte equal
- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/knowledge_base.py check --base origin/main` → passed
- `python3 scripts/engineering-lint.py` → passed
- `mvn -B -ntp test` → BUILD SUCCESS
- `docker compose up -d` → 12 容器 Up
- smoke test（Docker 网络内）→ 10/10 passed
- `bash scripts/init-nacos-routes.sh` → Nacos API 返回 true

## Known Risks

- hm-service 未部署（docker-compose.yml 中无此容器），`/hi` 端点不可用。
  本次不处理，后续可选部署。
- WSL2 宿主机→Docker 网络不通（iptables DROP 规则），smoke test 需从
  容器内部运行。这是环境问题，非代码缺陷。
- Nacos 路由配置需在 `docker compose up` 后手动运行 `init-nacos-routes.sh`。
  可考虑加入 docker-compose.yml 的 init 容器或 entrypoint。

## Next Action

Push 远程、开 PR、过 CI 与 codex review。

## Worktree Or Branch

- `task/2026-05-27-docker-compose-fixes`

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-05-27-docker-compose-fixes`
- Remote branch: 待 push
- Pull request: 待创建

## CI And Review

- CI status: not run yet
- Codex review: not run yet
