# Verification

| # | Command | Result | Evidence |
| --- | --- | --- | --- |
| 1 | `cmp CLAUDE.md AGENTS.md` | passed | byte equal |
| 2 | `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| 3 | `python3 scripts/knowledge_base.py check --base origin/main` | passed | knowledge base check passed (K005) |
| 4 | `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |
| 5 | `mvn -B -ntp test` | passed | BUILD SUCCESS |
| 6 | `cd hmall-web && npm ci && npm test --if-present && npm run build` | passed | placeholder test exit 0, build 4.76s |
| 7 | `cd hmall-admin && npm ci && npm test --if-present && npm run build` | passed | placeholder test exit 0, build 9.21s |
| 8 | `docker compose config -q` | passed | exit 0 |
| 9 | `docker compose down -v && docker compose up -d` | passed | 清理数据卷后重新启动，16 容器全部启动 |
| 10 | `docker compose ps` | passed | nacos-init exited(0)、smoke-test exited(0)，其余 14 服务 Up |
| 11 | `docker compose logs smoke-test` | passed | 10/10 passed（smoke-test 服务自动运行，含多端点就绪检查） |
| 12 | `docker compose exec hm-gateway curl -sf --max-time 5 http://localhost:8080/items/page?page=1&size=10` | passed | 200 + valid JSON |
| 13 | `docker compose exec hm-gateway curl -sf --max-time 5 http://localhost:8080/categories` | passed | 200 |
| 14 | `docker compose exec hm-gateway curl -sf --max-time 5 -X POST -H 'Content-Type: application/json' -d '{"username":"testuser","password":"admin123"}' http://localhost:8080/users/login` | passed | 返回 token |
| 15 | `mvn -B -ntp -q test` | passed | BUILD SUCCESS（含 hm-gateway 变更） |
| 16 | `curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:8080/categories -H 'Content-Type: application/json' -d '{"name":"test"}'` | 401 | 匿名 POST /categories 被拒（需认证） |
| 17 | `curl -s -o /dev/null -w '%{http_code}' -X PUT http://localhost:8080/categories/1 -H 'Content-Type: application/json' -d '{"name":"test"}'` | 401 | 匿名 PUT /categories/1 被拒（需认证） |
| 18 | `curl -s -o /dev/null -w '%{http_code}' -X DELETE http://localhost:8080/categories/1` | 401 | 匿名 DELETE /categories/1 被拒（需认证） |
| 19 | `curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/categories` | 200 | 匿名 GET /categories 放行（读白名单） |

## Notes

- Nacos 路由初始化已通过 `nacos-init` 服务自动化，不再需要手动运行 `init-nacos-routes.sh`。
- `nacos-init` 使用 `curlimages/curl` 镜像 + `apk add python3`，挂载 `./scripts` 目录。
- 所有 Java 服务依赖 `nacos-init` 的 `service_completed_successfully` 条件。
- `mvn -B -ntp verify -Pintegration` 本地跳过：WSL2 宿主机无法访问 Docker MySQL（端口 3306 不可达）。CI integration job 已通过（run 26505386582，conclusion: success）。
- `npx --prefix e2e playwright test --list` 跳过：本 PR 不改 e2e spec。
