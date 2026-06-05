# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -q -pl hm-api -am compile` | passed | hm-api 编译通过，feign-slf4j 依赖被正确解析。 |
| `mvn -B -ntp -q -pl user-service -am compile` | passed | user-service 编译通过，能继承 hm-api 的 feign-slf4j。 |
| `mvn -B -ntp -q -pl user-service -am test -Dtest=UserServiceFeignContextLoadTest` | passed | 新测试验证不 mock Feign 时上下文可加载，feignLoggerFactory → Slf4jLogger 正常实例化。 |
| `mvn -B -ntp -q test` (全部模块) | passed | 全部后端单元测试通过，含 user-service/item-service/trade-service/pay-service/cart-service/notify-service/file-service/hm-gateway/hm-common。 |

## 跟进验证（整栈启动阻塞修复，提交 89b7dd4）

| Command | Result | Evidence |
| --- | --- | --- |
| seata 最小复现/验证（docker run） | reproduced→pass | 错误 `server-addr` 复现 `all servers([localhost:8848])` 崩溃；改 camelCase `serverAddr` 后 `Server started, service listen port: 8091`。 |
| `docker compose up -d --build --wait --wait-timeout 420` | passed | `exit=0`；16 容器全部 healthy（8 java + seata + nacos/mysql/redis/rabbitmq/minio + 2 前端）。 |
| 各服务 `/actuator/health`（统一 8080） | passed | 8 个 java 服务均 `{"status":"UP"}`，redis 组件由 `DOWN(localhost:6379)` 修复为 `UP`。 |
| `scripts/smoke/smoke.sh`（compose 网络内打网关） | passed | `Results: 14/14 passed`。 |
| CI（PR #87 重跑） | passed | lint/test/integration/smoke 四个主 job 全部通过；smoke job 4m33s pass（修复前该 job 失败）。 |
| harness / KB(含 K005) / engineering-lint | passed | 三项交付前检查均通过。 |
