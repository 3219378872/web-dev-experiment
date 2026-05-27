# Handoff: docker compose 全栈验证修复

## Status

PR #21 已创建。Smoke test 10/10 通过（从 Docker 网络内运行）。
待 CI 与 codex review 通过后合并。

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

修复 codex review blocking findings 后重新提交，等待 CI 通过后合并。

## Worktree Or Branch

- `task/2026-05-27-docker-compose-fixes`

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-05-27-docker-compose-fixes`
- Remote branch: `origin/task/2026-05-27-docker-compose-fixes`
- Pull request: #21（https://github.com/3219378872/web-dev-experiment/pull/21）

## CI And Review

- CI status: round 2 — lint/test/integration/smoke passed，codex-review 2 blocking findings
- Codex review: round 2 failed（task.yaml PR URL 格式、ci/codex 状态未更新、verification 命令不精确）
