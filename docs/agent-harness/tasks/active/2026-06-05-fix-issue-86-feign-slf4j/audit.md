# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 根因1：seata 注册 nacos 失败 | done | 从镜像入口脚本与 `NacosRegistryServiceImpl.class` 字节常量确认 seata 1.8.0 原生配置层用 camelCase `registry.nacos.serverAddr`；compose 写成 kebab `server-addr` 被忽略，回退默认 `localhost:8848`。本地以最小 nacos+seata 复现 `all servers([localhost:8848])` 崩溃，改 camelCase 后 `Server started, service listen port: 8091` 且无 start error。 |
| 根因2：seata healthcheck 401 | done | 容器内实测 `/health`→401（受 console 鉴权拦截），`/`→200（在 `seata.security.ignore.urls` 白名单）。改 healthcheck 探 `/`。 |
| 根因3：FeignClient bean 重名 | done | 业务服务日志 `APPLICATION FAILED TO START ... 'trade-service.FeignClientSpecification' already defined`。hm-api 三对 `@FeignClient` 共用同一服务名无 `contextId`（trade-service: Order/Coupon；user-service: User/Favorite；item-service: Review/Item）。各加唯一 `contextId` 使 spec bean 名唯一，`value` 保持服务名不影响路由。 |
| `@FeignClient` value 仍为服务名 | done | 仅新增 `contextId`，`value`/name 不变，注册中心路由不受影响。 |
| 根因4：healthcheck 探错端口 | done | 本 PR 在 `x-java-service` 加的共享 healthcheck 硬编码 `:8080`，但业务服务监听 8081-8087（gateway=8080）。经确认网关路由全用 `lb://service-name`（服务发现），业务服务不发布宿主端口，故用 `SERVER_PORT=8080` 让容器内统一监听 8080（实测 Tomcat 起在 8080）。cart-service 用独立 env 块，单独补该项。 |
| 根因5：actuator↔springfox NPE | done | 本 PR 新增 actuator → `documentationPluginsBootstrapper` NPE（`WebMvcPatternsRequestConditionWrapper.getPatterns` 返回 null），5 个 knife4j 服务崩溃。实测 `matching-strategy=ant_path_matcher` 单独无效（actuator 端点恒为 PathPattern，不受其控）。用户选定方案：hm-common 加 `SpringfoxCompatibilityConfig`（BeanPostProcessor 过滤 patternParser≠null 的映射，剔除 actuator 端点）+ 保留 `matching-strategy=ant`（使应用控制器为 ant 风格、文档仍生成）。hm-common 已含 knife4j starter，无需新增依赖；经 spring.factories 自动装配，对未启用 knife4j 的 notify/file 为无操作。 |
| 不破坏 R<T>/PageDTO 封装 | not applicable | 未触及响应封装。 |
| 不硬编码密钥 | done | compose 改动仅为 registry key 拼写、healthcheck 路径/端口与 matching-strategy，无凭据。 |
