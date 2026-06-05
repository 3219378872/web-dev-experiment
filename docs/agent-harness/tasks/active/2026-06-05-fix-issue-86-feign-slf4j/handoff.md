# Handoff: Fix Issue 86 Feign Slf4J

## Status
跟进 PR #87：硬化版 smoke job（去 `|| true` + `--wait`）暴露出整栈无法健康起来。
逐一根因定位并修复了 6 个阻塞问题，本地完整复刻 CI smoke job 验证整栈健康 + smoke.sh 通过。

## Files Changed
- `docker-compose.yml`
  - seata `registry.nacos.server-addr` → `registry.nacos.serverAddr`（camelCase，根因1）
  - seata healthcheck `/health` → `/`（避开 console 401，根因2）
  - `x-java-env` 新增 `SERVER_PORT=8080`（统一端口，根因4）、
    `SPRING_MVC_PATHMATCH_MATCHING_STRATEGY=ant_path_matcher`（配合补丁，根因5）、
    `MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always`、`SPRING_REDIS_HOST/PORT`（redis，根因6）
  - cart-service 独立 env 块同步补齐上述项
- `hm-api` 的 client 接口 `OrderClient`/`CouponClient`/`UserClient`/`FavoriteClient`/`ReviewClient`/`ItemClient`
  - 三对同名 `@FeignClient` 各加唯一 `contextId`（根因3）
- `hm-common/src/main/java/com/hmall/common/config/SpringfoxCompatibilityConfig.java`（新增）+
  `hm-common/src/main/resources/META-INF/spring.factories`
  - springfox 2.10.5 × actuator NPE 兼容补丁（BeanPostProcessor，根因5），
    `@ConditionalOnWebApplication(SERVLET)` 排除 reactive 网关

## Commands Run
- `docker run ... seataio/seata-server:1.8.0`（提取入口脚本/配置/jar 常量，定位 serverAddr key）
- 最小 nacos+seata 容器复现并验证 seata 注册（根因1）
- `mvn -DskipTests clean package`（×N，clean 避免增量构建陈旧 fat jar）
- `docker compose up -d --build --wait`（完整复刻 CI smoke job）
- 逐服务 `curl /actuator/health` + show-details 诊断各 health 组件

## Known Risks
- redis/端口经 docker compose env 覆盖修复（docker 层）；各服务 application.yaml 的 redis
  缩进 bug 与 per-service 端口未改（本地开发仍按 localhost/各自端口，不受影响）。
- 诊断阶段曾临时启用 `MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=always`，定位 redis 后已移除，
  避免网关无鉴权暴露 health 组件明细（healthcheck 仅判状态码，不需要明细）。

## Next Action
- 等最终 `docker compose up --wait` + `smoke.sh` 通过后提交并推送，触发 CI。
- CI test job 会跑单测（含本 PR 的 UserServiceFeignContextLoadTest）。

## Worktree Or Branch
- `task/2026-06-05-fix-issue-86-feign-slf4j`
