# Handoff: Fix Issue 86 Feign Slf4J

## Status
Completed。初版补 feign-slf4j + actuator + 硬化 smoke 后，硬化的 smoke job（去 `|| true` +
`--wait`）暴露出整栈无法健康起来；遂跟进逐一定位并修复 7 个整栈启动阻塞问题。
本地完整复刻 CI smoke job 验证：16 容器全部 healthy、`smoke.sh` 14/14 通过、`mvn test` 通过；
CI 上 lint/test/integration/smoke 四个主 job 均通过。

## Files Changed
- `hm-api/pom.xml` — add `feign-slf4j` dependency (root fix)
- `hm-gateway/pom.xml` — add `spring-boot-starter-actuator`
- `user-service/pom.xml` — add `spring-boot-starter-actuator`
- `item-service/pom.xml` — add `spring-boot-starter-actuator`
- `cart-service/pom.xml` — add `spring-boot-starter-actuator`
- `trade-service/pom.xml` — add `spring-boot-starter-actuator`
- `pay-service/pom.xml` — add `spring-boot-starter-actuator`
- `notify-service/pom.xml` — add `spring-boot-starter-actuator`
- `file-service/pom.xml` — add `spring-boot-starter-actuator`
- `docker-compose.yml` — add `healthcheck` to `x-java-service` anchor
- `.github/workflows/ci.yml` — smoke job: `mvn package` before compose up; remove `|| true` from up/smoke steps
- `pom.xml` — `maven-surefire-plugin` system property `knife4j.enable=false` to avoid Springfox/Actuator NPE in tests
- `user-service/src/test/java/com/hmall/UserServiceFeignContextLoadTest.java` — new context-load test without `@MockBean ItemClient`

### 跟进提交（89b7dd4）修复整栈启动阻塞，7 个根因：
- `docker-compose.yml` —— seata `registry.nacos.server-addr`→`serverAddr`(camelCase，根因1)；
  seata healthcheck `/health`→`/`(避 console 401，根因2)；x-java-env + cart 独立块新增
  `SERVER_PORT=8080`(统一端口，根因4)、`SPRING_MVC_PATHMATCH_MATCHING_STRATEGY=ant_path_matcher`
  (配合补丁，根因5)、`SPRING_REDIS_HOST/PORT`(根因6)
- `hm-api/.../client/{Order,Coupon,User,Favorite,Review,Item}Client.java` —— 三对同名
  `@FeignClient` 各加唯一 `contextId`(根因3)
- `hm-common/.../config/SpringfoxCompatibilityConfig.java`(新增) + `META-INF/spring.factories`
  —— springfox 2.10.5 × actuator NPE 兼容补丁，`@ConditionalOnWebApplication(SERVLET)`(根因5)
- `cart-service/.../application.yaml` —— 补 `hm.cart.max-items: 10`，修复 POST /carts 拆箱 NPE(根因7)
- KB：hm-api/hm-common/cart-service modules + auth-and-gateway/order-checkout flows 同步

## Commands Run
- `mvn -B -ntp -q test`(all modules) — all green（初版与跟进各跑一次）
- `mvn -DskipTests clean package` + `docker compose up -d --build --wait`：16 容器全 healthy
- `bash scripts/smoke/smoke.sh`(compose 网络内打网关) — 14/14 passed
- `agent_harness.py check` / `knowledge_base.py check --base origin/main` / `engineering-lint.py` — 全过

## Known Risks
- `trade-service/pom.xml` 中原有的单独 `feign-slf4j` 声明现为冗余（无害），可后续清理。
- **修正初版的错误假设**：actuator × Springfox 2.10.5 的 NPE 并非"仅测试环境"——运行时
  actuator 端点的 PathPattern 映射同样触发 `documentationPluginsBootstrapper` NPE，使 5 个
  knife4j 服务在容器内崩溃循环。已由 `SpringfoxCompatibilityConfig`（BeanPostProcessor 剔除
  actuator 的 PathPattern 映射）+ `matching-strategy=ant_path_matcher` 在运行时根治，
  surefire 的 `knife4j.enable=false` 仍保留用于测试期。
- redis/端口经 docker compose env 覆盖修复（docker 层）；各服务 application.yaml 的 redis
  缩进 bug 与 per-service 端口未改（本地开发按 localhost/各自端口，不受影响）。

## Next Action
跟进提交已推送，CI 主 job 全绿；本提交解决 codex-review 关于 active/completed 任务重复的 blocking finding。

## Worktree Or Branch
- `task/2026-06-05-fix-issue-86-feign-slf4j`
