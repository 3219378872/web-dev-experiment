# Context: Fix Issue 86 Feign Slf4J

## Objective
在 `hm-api/pom.xml` 统一补充 `feign-slf4j` 依赖，修复 `user-service` 因启动期 eager Feign 注入导致的 `NoClassDefFoundError: feign.slf4j.Slf4jLogger` 崩溃循环，并消除 `item-service` 的同类潜在隐患。

## Scope
- In scope:
  - `hm-api/pom.xml`：添加 `io.github.openfeign:feign-slf4j` 依赖（版本由 `spring-cloud-dependencies` BOM 管理，无需显式 version）。
  - 验证 `user-service` 编译通过。
- Out of scope:
  - 清理 `trade-service/pom.xml` 中已有的局部 `feign-slf4j` 声明（冗余但无害，后续可独立清理）。
  - 修改任何 Java 源码或前端代码。

## Related Artifacts
- Spec: none —— 这是一个单文件依赖修复，Issue #86 已提供完整根因分析与建议方案。
- Plan: none —— 变更范围仅限于一条 Maven 依赖声明。

## Likely Files
- `hm-api/pom.xml`

## Runtime Evidence To Inspect First
- Issue #86 中的 `docker compose logs user-service | grep -i "ClassNotFoundException"`

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.

## Worktree Or Branch
- `task/2026-06-05-fix-issue-86-feign-slf4j`

## Open Questions
- none
