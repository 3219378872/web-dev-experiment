# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp test` | passed | 全模块单测通过 |
| `python3 scripts/agent_harness.py check` | passed | harness 结构校验通过 |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | KB 共变检查通过 |
| `python3 scripts/engineering-lint.py` | passed | 工程 lint 通过 |
| `docker compose up -d` | passed | 12 容器全部 Up |
| `docker compose exec hm-gateway curl -sf http://localhost:8080/items/page` | passed | 200 + valid JSON |
| `docker compose exec hm-gateway curl -sf http://localhost:8080/categories` | passed | 200（修复后） |
| `docker compose exec hm-gateway curl -sf -X POST -H 'Content-Type: application/json' -d '{"username":"testuser","password":"admin123"}' http://localhost:8080/users/login` | passed | 返回 token |
| smoke test（从 Docker 网络内） | passed | 10/10 通过 |
| `bash scripts/init-nacos-routes.sh http://nacos:8848`（从容器内） | passed | Nacos API 返回 true |
| `cmp CLAUDE.md AGENTS.md` | passed | byte equal |
