# Context: docker compose 全栈验证修复

## Objective

修复 `docker compose up -d` 全栈运行时发现的阻塞性问题，使 smoke test 能在
Docker 网络内 10/10 通过。

## Scope

- In scope: gateway 鉴权白名单（含读写分离）、SQL 初始化数据、Nacos 路由初始化自动化、
  hm-service 部署、.gitignore、gateway 路由完整性
- Out of scope: WSL2 宿主机网络修复、e2e 测试适配

## Related Artifacts

- Spec: none — 本次修复均为 bug fix，不需要独立 spec
- Plan: none — 修复项明确，不需要独立 plan

## Likely Files

- `hm-gateway/src/main/resources/application.yaml`
- `hm-gateway/src/main/java/com/hmall/gateway/config/AuthProperties.java`
- `hm-gateway/src/main/java/com/hmall/gateway/filters/AuthGlobalFilter.java`
- `docs/sql/init-all-tables.sql`
- `scripts/init-nacos-routes.sh`（新增）
- `.gitignore`

## Runtime Evidence To Inspect First

- `docker compose exec hm-gateway curl http://localhost:8080/items/page` → 200
- smoke test 10/10 通过（从 Docker 网络内运行）

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token
- 不破坏 `hm-common.domain.R<T>` / `PageDTO<T>` 响应封装
