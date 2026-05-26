---
title: hm-service
tracks:
  - hm-service/
last_synced_commit: 33e9420
last_synced_date: 2026-05-26
sync_note: "删除旧 ItemServiceImplTest（迁移至 item-service IT）"
---

# hm-service

## 职责

聚合演示服务（singleton 整体版）：把常用读接口（商品、用户、购物车、订单、地址、
支付单）聚合在一个 Spring Boot app 里，主要用于本地教学/调试场景。

## 公开接口与契约

- HTTP API：覆盖商品、用户、购物车、订单、地址、支付单的常用 CRUD/查询接口。
- 持久层：直接读所有业务库（mapper 全在一个服务里），仅做演示用。

## 上游

仅本地开发与教学课件场景；生产部署用各 `*-service` 拆分版。

## 下游

直接连所有业务库（hm-userinfo、hm-item、hm-cart、hm-trade、hm-pay）。

## 关键文件

- `HMallApplication.java` —— 启动类。
- `mapper/` —— `ItemMapper`、`CartMapper`、`PayOrderMapper`、`AddressMapper`、
  `UserMapper`、`OrderDetailMapper`、`OrderMapper`。
- 对应 controller / service。

## 注意事项与陷阱

- 与 `*-service` 拆分版功能重叠，改逻辑时要确认是否需要同步两端。
- 不要把 hm-service 作为生产真理之源；任何业务约束以拆分版为准。
- 教学场景下默认登录、跨域、鉴权配置宽松，部署前必须收紧。
