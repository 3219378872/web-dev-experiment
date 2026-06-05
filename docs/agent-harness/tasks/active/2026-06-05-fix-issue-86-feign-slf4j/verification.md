# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| seata 最小复现（错误 key） | reproduced | `docker run ... -e registry.nacos.server-addr=...` → `NacosException ... all servers([localhost:8848])` 崩溃，与 CI smoke 日志一致。 |
| seata 最小验证（camelCase key） | pass | `-e registry.nacos.serverAddr=nacos-test:8848` → `Server started, service listen port: 8091`，`start error` 计数 0，进程持续运行。 |
| seata healthcheck 端点探测 | pass | 容器内 `/health`→401、`/actuator/health`→401、`/`→200（白名单），healthcheck 改探 `/`。 |
| `mvn -DskipTests clean package` | pass | exit 0；校验 user-service fat jar 内 hm-api `OrderClient.class` 含 `contextId`/`orderClient`、hm-common jar 含 `SpringfoxCompatibilityConfig` 且 spring.factories 已注册。 |
| `docker compose up -d --build --wait --wait-timeout 420` | pass | `compose_up_exit=0`；16 个容器全部 healthy（8 个 java 服务 + seata + nacos/mysql/redis/rabbitmq/minio + 2 前端）。 |
| 各服务 `/actuator/health`（统一 8080） | pass | gateway/user/item/cart/trade/pay/notify/file 均 `{"status":"UP"}`，redis 组件 `UP`（修复前 `DOWN: Unable to connect to localhost:6379`）。 |
| `scripts/smoke/smoke.sh`（网络内打网关） | pass | `Results: 14/14 passed`，`All smoke tests PASSED`（含商品/搜索/分类/通知/登录/地址鉴权/商品详情/购物车增查删）。 |
| `mvn -B -ntp -q test` | （见 handoff/最终结果） | CI test job 等价的后端单元测试。 |
| `python3 scripts/agent_harness.py check` | pass | `agent harness check passed`。 |
| `python3 scripts/knowledge_base.py check --base origin/main` | pass | `knowledge base check passed`（含 K005，已同步 hm-api.md/hm-common.md）。 |
| `python3 scripts/engineering-lint.py` | pass | `engineering-lint: all checks passed`。 |

注：宿主机（WSL2）直接 `curl localhost:8080` 返回 000 属环境端口映射限制；网关容器内
`/items/page`→200 证明路由正常，故 smoke.sh 在 compose 网络内打 `http://hm-gateway:8080`
执行。CI（标准 Docker 主机）按原样打 `localhost:8080` 即可。
