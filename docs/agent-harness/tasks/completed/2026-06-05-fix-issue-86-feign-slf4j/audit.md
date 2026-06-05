# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Issue #86 根因修复（feign-slf4j） | satisfied | `hm-api/pom.xml` 已添加 `io.github.openfeign:feign-slf4j`，版本由 `spring-cloud-dependencies` BOM 管理。 |
| CI smoke 真正门控 | satisfied | `.github/workflows/ci.yml` smoke job 已去掉 `docker compose up` 与 `smoke.sh` 的 `|| true`，并在 compose up 前执行 `mvn -DskipTests package` 生成 jar。 |
| 业务服务 healthcheck | satisfied | 全部 8 个 Java 服务（gateway + 7 业务）的 `pom.xml` 添加了 `spring-boot-starter-actuator`；`docker-compose.yml` 的 `x-java-service` anchor 增加了 `healthcheck`（`curl -sf http://localhost:8080/actuator/health`）。 |
| 非 mock Feign 上下文加载测试 | satisfied | `user-service/src/test/java/com/hmall/UserServiceFeignContextLoadTest.java` 不 mock `ItemClient`，强制导入 `FeignAutoConfiguration`，验证启动期真实 Feign 装配。 |
| 不破坏现有测试 | satisfied | 全部模块 `mvn test` 通过；为避免 actuator + Springfox 2.x 兼容性问题，根 `pom.xml` 的 `maven-surefire-plugin` 配置了 `<knife4j.enable>false</knife4j.enable>` 系统属性。 |

## 跟进审计（整栈启动阻塞，提交 89b7dd4）

| Requirement | Status | Evidence |
| --- | --- | --- |
| 根因1 seata 注册 nacos | done | 从镜像入口脚本/`NacosRegistryServiceImpl.class` 字节常量确认 seata 1.8.0 用 camelCase `registry.nacos.serverAddr`；compose 的 kebab `server-addr` 被忽略回退 localhost:8848。最小 nacos+seata 复现并验证修复。 |
| 根因2 seata healthcheck 401 | done | 容器内实测 `/health`→401（console 鉴权）、`/`→200（白名单）；改探 `/`。 |
| 根因3 FeignClient bean 重名 | done | 三对 `@FeignClient` 共用服务名无 `contextId`，引用方启动 `FeignClientSpecification` 重名崩溃；各加唯一 `contextId`，`value` 不变。 |
| 根因4 healthcheck 探错端口 | done | 共享 healthcheck 硬编码 :8080，业务服务监听 8081-8087；`SERVER_PORT=8080` 统一（网关 `lb://`、业务服务不发布宿主端口，安全）。 |
| 根因5 actuator×springfox NPE（修正初版假设） | done | **运行时**同样触发 `documentationPluginsBootstrapper` NPE（非仅测试），actuator PathPattern 端点不受 matching-strategy 控制；经维护者确认采用 hm-common `SpringfoxCompatibilityConfig`（BeanPostProcessor 剔除 actuator 映射）+ `matching-strategy=ant`，保留 actuator healthcheck 与 knife4j 文档。 |
| 根因6 redis 连 localhost | done | `spring.redis.*` 未配置（item-service 误缩进至 mybatis-plus），RedisHealthIndicator DOWN→503；`SPRING_REDIS_HOST=redis` 修复，实测 redis 组件 UP。 |
| 根因7 cart POST /carts 500 | done | `hm.cart.max-items` 未配置使 `maxItems` 拆箱 NPE；cart application.yaml 补默认值 10，smoke #12-14 通过。 |
| 单一 harness 任务记录 | done | 删除误建的 `active/` 重复副本，跟进发现合并入本 completed 记录（解决 codex-review blocking finding）。 |
