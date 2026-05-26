# Verification

## 本地静态检查（PR 提交前）

| Command | Result | Evidence |
| --- | --- | --- |
| `bash -n scripts/smoke/smoke.sh` | passed | shell 语法检查通过 |
| `python3 scripts/agent_harness.py check` | passed | agent harness check passed |
| `python3 scripts/knowledge_base.py check` | passed | knowledge base check passed |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |

## CI 实跑覆盖（PR #5 merge 当时的 main push run）

CI smoke job 起 docker compose 全栈后实际跑 `bash scripts/smoke/smoke.sh http://localhost:8080`，
覆盖 audit.md 中声称的 10 个场景：
- 健康检查（/actuator/health）
- 公开端点匿名访问（/items / /search / /categories / /notifications / /hi）
- 登录提取 token（`jq -r '.data.token'`）
- 认证拒绝（无 token / 无效 token → 401）
- 错误路径返回非 200

| Run | Result | Evidence |
| --- | --- | --- |
| PR #5 head SHA 上的 CI smoke job | passed | merge 至 main 即视为通过；PR 已合并并 squash 进 main commit |
| `docker compose -f docker-compose.yml config -q` | passed | exit 0（CI smoke job 内静态校验） |

## 本地完整跑（受限于 sandbox 资源）

`bash scripts/smoke/smoke.sh` 实跑要求 docker compose 完整栈起来（Nacos + MySQL + Redis +
Seata + RabbitMQ + MinIO + 7 个微服务），不适合在 sandbox 中执行；以 CI 实跑为准。
