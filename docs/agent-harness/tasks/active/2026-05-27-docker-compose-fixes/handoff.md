# Handoff: docker compose 全栈验证修复

## Status

PR #21 已创建。Smoke test 10/10 通过（从 Docker 网络内运行）。
Nacos 路由初始化已通过 `nacos-init` 服务自动化。
待 CI 与 codex review 通过后合并。

## Files Changed

- `hm-gateway/src/main/resources/application.yaml`（加 `/categories/**` 到鉴权白名单）
- `docs/sql/init-all-tables.sql`（修复 user.status 类型 + 密码 hash）
- `scripts/init-nacos-routes.sh`（新增：Nacos gateway 路由初始化脚本）
- `docker-compose.yml`（新增 `nacos-init`、`hm-service`、`smoke-test`；MySQL 挂载 SQL init；所有 Java 服务依赖 nacos-init）
- `.gitignore`（加 `docker/mysql/`）

## Commands Run

- `cmp CLAUDE.md AGENTS.md` → byte equal
- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/knowledge_base.py check --base origin/main` → passed
- `python3 scripts/engineering-lint.py` → passed
- `mvn -B -ntp test` → BUILD SUCCESS
- `docker compose up -d` → 16 容器启动
- `docker compose ps` → nacos-init exited(0)、smoke-test exited(0)，其余 14 服务 Up
- `docker compose logs smoke-test` → 10/10 passed

## Known Risks

- hm-service 已加入 docker-compose.yml，`/hi` 端点可用。
- `nacos-init` 使用 `apk add python3` 安装依赖，首次运行可能稍慢。
- SQL init 通过 MySQL 的 `/docker-entrypoint-initdb.d/` 机制自动导入，
  仅在数据目录为空时生效（首次启动）；MySQL 数据使用 host bind mount
  `./docker/mysql`，完整清理需 `docker compose down -v && rm -rf ./docker/mysql`。
  重新启动后可通过 `docker compose logs mysql` 确认 SQL 导入。
- smoke-test 等待 items/categories/notifications 三个端点均返回 200 后才执行，
  超时 120s，避免服务就绪竞态。
- WSL2 宿主机→Docker 网络不通（iptables DROP 规则），smoke test 需从
  容器内部运行。这是环境问题，非代码缺陷。

## Next Action

Round 19 blocking findings (2 个):
1. smoke-test curl -sf 探针在 400/401 时触发 fallback → 移除 -f，改用 "!= 000" 判断
2. docker compose down -v 不清理 bind mount ./docker/mysql → 更新证据描述
修复后重新推送，等待 CI codex-review 通过后合并。

## Worktree Or Branch

- `task/2026-05-27-docker-compose-fixes`

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-05-27-docker-compose-fixes`
- Remote branch: `origin/task/2026-05-27-docker-compose-fixes`
- Pull request: #21（https://github.com/3219378872/web-dev-experiment/pull/21）

## CI And Review

- CI status: round 17-19 — lint/test/integration/smoke passed; codex-review failed
- Codex review round 16: smoke-test 未等待 user-service 就绪（已修）
- Codex review round 17: 增加 user-service 就绪检查（POST /users/login 返回 400/401）
- Codex review round 18: 误包含 archive-pr18-task 目录（已移除）；handoff 文本 stale（已更新）
- Codex review round 19: curl -sf 探针 bug + MySQL bind mount 证据描述不准确（本次修复）
- Next: push round 20 fix，等待 CI + codex-review 全部通过
