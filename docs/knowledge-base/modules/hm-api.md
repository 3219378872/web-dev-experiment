---
title: hm-api
tracks:
  - hm-api/
last_synced_commit: 96cde0f
last_synced_date: 2026-06-05
sync_note: "CartFormDTO 添加 num 字段并添加验证"
last_synced_date: 2026-06-04
sync_note: "Phase 3: UserClient 新增 countNewUsers(days) 方法"
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
