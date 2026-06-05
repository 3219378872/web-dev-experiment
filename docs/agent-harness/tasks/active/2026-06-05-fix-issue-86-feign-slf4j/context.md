# Context: Fix Issue 86 Feign Slf4J

## Objective
让 Issue #86 PR（#87）的硬化版 smoke job 真正通过：补齐 feign-slf4j
依赖后，进一步修复被硬化 smoke 暴露出的整栈启动阻塞问题，使
`docker compose up -d --wait` 下所有服务达到 healthy。

## Scope
- In scope:
  - `hm-api/pom.xml` 补 feign-slf4j（原 PR 已做）。
  - 各 Java 服务补 actuator（原 PR 已做）。
  - CI smoke job 去 `|| true` + `mvn package` 前置（原 PR 已做）。
  - **本次跟进新增**：修复 seata-server 启动崩溃（registry key 写法错误）。
  - **本次跟进新增**：修复 seata-server healthcheck 端点 401 永不健康。
  - **本次跟进新增**：修复 hm-api 三对 `@FeignClient` 重名导致全部业务服务
    `FeignClientSpecification` bean 冲突、启动崩溃。
- Out of scope:
  - 重构 `@EnableFeignClients` 扫描范围（保持现有行为）。
  - 统一 R<T> 响应封装。

## Related Artifacts
- Spec: none —— bugfix，沿用 PR #87 既有目标。
- Plan: none —— bugfix，逐根因定位即可。
- PR: #87（branch task/2026-06-05-fix-issue-86-feign-slf4j）。

## Likely Files
- `docker-compose.yml`（seata env + healthcheck）
- `hm-api/src/main/java/com/hmall/api/client/*.java`（contextId）

## Runtime Evidence To Inspect First
- CI smoke job 日志：seata `all servers([localhost:8848])` 崩溃。
- 业务服务容器日志：`APPLICATION FAILED TO START ... trade-service.FeignClientSpecification`。

## Safety Constraints
- 不在 compose / yaml 中硬编码真实密钥。
- 不破坏 `R<T>`/`PageDTO` 响应封装。
- `@FeignClient` 的 `value` 必须保持为服务名（用于注册中心路由），仅
  新增 `contextId` 区分 bean。

## Worktree Or Branch
- `task/2026-06-05-fix-issue-86-feign-slf4j`

## Open Questions
- none
