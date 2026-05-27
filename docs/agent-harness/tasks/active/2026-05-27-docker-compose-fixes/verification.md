# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `cmp CLAUDE.md AGENTS.md` | passed | byte equal |
| `python3 scripts/agent_harness.py check` | passed | harness 结构校验通过 |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | KB 共变检查通过（K005） |
| `python3 scripts/engineering-lint.py` | passed | 工程 lint 通过 |
| `mvn -B -ntp test` | passed | 全模块单测 BUILD SUCCESS |
| `mvn -B -ntp verify -Pintegration` | skipped | WSL2 宿主机无法访问 Docker MySQL（端口 3306 不可达）；集成测试需 Docker 网络内运行 |
| `cd hmall-web && npm ci && npm test && npm run build` | passed | placeholder test exit 0, build 4.76s |
| `cd hmall-admin && npm ci && npm test && npm run build` | passed | placeholder test exit 0, build 9.21s |
| `docker compose config -q` | passed | exit 0 |
| `docker compose up -d` | passed | 12 容器全部 Up |
| smoke test（Docker 网络内） | passed | 10/10 通过 |
| `bash scripts/init-nacos-routes.sh`（容器内） | passed | Nacos API 返回 true |
| Playwright `npx playwright test --list` | skipped | 本 PR 不改 e2e spec；e2e 需全栈 Docker 栈运行 |
