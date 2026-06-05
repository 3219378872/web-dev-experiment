# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Issue #86 根因修复（feign-slf4j） | satisfied | `hm-api/pom.xml` 已添加 `io.github.openfeign:feign-slf4j`，版本由 `spring-cloud-dependencies` BOM 管理。 |
| CI smoke 真正门控 | satisfied | `.github/workflows/ci.yml` smoke job 已去掉 `docker compose up` 与 `smoke.sh` 的 `|| true`，并在 compose up 前执行 `mvn -DskipTests package` 生成 jar。 |
| 业务服务 healthcheck | satisfied | 全部 8 个 Java 服务（gateway + 7 业务）的 `pom.xml` 添加了 `spring-boot-starter-actuator`；`docker-compose.yml` 的 `x-java-service` anchor 增加了 `healthcheck`（`curl -sf http://localhost:8080/actuator/health`）。 |
| 非 mock Feign 上下文加载测试 | satisfied | `user-service/src/test/java/com/hmall/UserServiceFeignContextLoadTest.java` 不 mock `ItemClient`，强制导入 `FeignAutoConfiguration`，验证启动期真实 Feign 装配。 |
| 不破坏现有测试 | satisfied | 全部模块 `mvn test` 通过；为避免 actuator + Springfox 2.x 兼容性问题，根 `pom.xml` 的 `maven-surefire-plugin` 配置了 `<knife4j.enable>false</knife4j.enable>` 系统属性。 |
