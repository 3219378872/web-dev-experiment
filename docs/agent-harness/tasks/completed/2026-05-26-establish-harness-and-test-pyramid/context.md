# Context: Establish Harness And Test Pyramid

## Objective
仿照 ../bidking-controller 在 hmall 仓库建立 agent-harness 工程脚手架，并据此推进
单元测试 → 集成测试 → 冒烟测试 → Playwright E2E → 视觉整改的分层质量体系。

## Scope
- In scope:
  - `scripts/_agent_harness/*.py` + `scripts/agent_harness.py` Python CLI
  - `docs/agent-harness/` 任务簿、模板、quality-rules、garbage-collection
  - `scripts/engineering-lint.py` + `.pre-commit-config.yaml`
  - `CLAUDE.md` / `AGENTS.md`（互镜）
  - 各阶段测试基础设施（pom 配置、Testcontainers、Vitest、Playwright 工程）
  - 修复测试或视觉整改中发现的真实 bug
- Out of scope:
  - 改写既有业务逻辑（仅在 bug 修复必要时改）
  - 业务功能扩展

## Related Artifacts
- Spec: docs/superpowers/specs/2026-05-18-hmall-refactor-design.md
- Plan: docs/superpowers/plans/2026-05-18-hmall-refactor-plan.md

## Likely Files
- `scripts/_agent_harness/cli.py`
- `scripts/_agent_harness/checks.py`
- `scripts/_agent_harness/lifecycle.py`
- `scripts/_agent_harness/model.py`
- `scripts/_agent_harness/gc.py`
- `scripts/_agent_harness/templates.py`
- `scripts/agent_harness.py`
- `scripts/engineering-lint.py`
- `.pre-commit-config.yaml`
- `CLAUDE.md`
- `AGENTS.md`
- `pom.xml`（test 依赖）
- `trade-service/pom.xml` 等各模块（Testcontainers）
- `hmall-web/package.json`、`hmall-admin/package.json`（Vitest）
- `e2e/`（Playwright 工程）
- `scripts/smoke/`（冒烟脚本）

## Runtime Evidence To Inspect First
- `docker compose ps` 与 `docker compose logs` —— 确认当前部署确实跑得起来。
- `mvn -q test` 当前输出 —— 看现状各模块测试是否能跑。
- `hmall-web` / `hmall-admin` 浏览器打开 —— 标记现存视觉/交互问题。

## Safety Constraints
- 不修改 `docker-compose.yml` 凭据。
- 不在源码中留密钥。
- 不破坏 `hm-common.Result<T>` / `PageDTO<T>` 响应封装。
- 不绕过 `hm-gateway` 直连业务服务。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-establish-harness-and-test-pyramid`
- Remote branch: `origin/task/2026-05-26-establish-harness-and-test-pyramid`
- Pull request: none - create after local acceptance and push.

## Open Questions
- Testcontainers 在 WSL2 Docker 环境下能否稳定启动 MySQL（待执行时验证）。
- Playwright 是否走 hm-gateway 还是直连前端 dev server（待具体场景决定）。
