---
title: hm-api
tracks:
  - hm-api/
last_synced_commit: d418693
last_synced_date: 2026-06-05
sync_note: "Issue #100: OrderClient 和 UserClient 添加 fallbackFactory 降级实现；设计未变，无需内容更新"
---

# hm-api

## 职责

跨服务调用的契约层：OpenFeign 客户端定义、跨服务 DTO、fallback 实现。
任何跨微服务的同步调用都要走 hm-api，不允许直连 HTTP。

## 公开接口与契约

- `dto/` —— 服务间共享 DTO：`ItemDTO`、`OrderDTO`、`AddressDTO`、`CartFormDTO`、
  `CouponDTO`、`LoginFormDTO`、`PayOrderFormDTO`、`ReviewDTO` 等。
- `client/` —— Feign 客户端接口（`ItemClient`、`CartClient`、`UserClient`、
  `TradeClient`、`PayClient`）。
- `client/fallback/` —— 熔断 fallback 实现，需对应 `@FeignClient(fallbackFactory=...)`。

## 上游

所有业务微服务 controller / service 层。

## 下游

被调用方暴露的 HTTP API。

## 关键文件

- `client/ItemClient.java`、`client/UserClient.java`（含 `countNewUsers`）、
  `client/CouponClient.java` 等 —— Feign 调用契约。
- `dto/ItemDTO.java`、`dto/OrderDTO.java`、`dto/CouponDTO.java`、`dto/AddressDTO.java`
  等 —— 公共数据契约。

## 注意事项与陷阱

- DTO 字段是跨服务公共契约，改名/删字段需要全部调用方同步迁移。
- fallback 必须实现且非空，否则熔断时 NPE。
- 不要在 hm-api 写业务逻辑，仅放接口与 DTO。
- **多个 `@FeignClient` 指向同一目标服务时必须各设唯一 `contextId`**。Spring Cloud
  OpenFeign 以 `<contextId>.FeignClientSpecification` 命名配置 bean，`contextId` 默认等于
  服务名；若两个客户端共用服务名（如 `OrderClient`/`CouponClient` 都指 `trade-service`，
  `UserClient`/`FavoriteClient` 都指 `user-service`，`ItemClient`/`ReviewClient` 都指
  `item-service`）且未设 `contextId`，会产生重名 bean，在
  `allow-bean-definition-overriding=false`（Spring Boot 2.7 默认）下使引用方服务启动崩溃。
  `value`/`name` 仍须保持为服务名以供注册中心路由。
