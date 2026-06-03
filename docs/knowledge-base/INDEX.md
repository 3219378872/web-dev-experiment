# Knowledge Base Index

hmall 项目知识库。维护方式见 [README](README.md)。

## Modules

### 基础与共享
- [hm-common](modules/hm-common.md) — 跨服务共享 DTO、`R`/`PageDTO`、异常、Mybatis-Plus 配置。
- [hm-api](modules/hm-api.md) — Feign 客户端定义与 fallback。
- [hm-gateway](modules/hm-gateway.md) — Spring Cloud Gateway、JWT 鉴权、路由。

### 业务微服务
- [user-service](modules/user-service.md) — 用户、地址、收藏、JWT 登录注册。
- [item-service](modules/item-service.md) — 商品、分类、评价、搜索。
- [cart-service](modules/cart-service.md) — 购物车。
- [trade-service](modules/trade-service.md) — 订单、优惠券、退款、发货、Seata。
- [pay-service](modules/pay-service.md) — 支付单、模拟支付、对账。
- [notify-service](modules/notify-service.md) — 站内信、客服消息、反馈。
- [file-service](modules/file-service.md) — MinIO 文件上传。

### 前端
- [hmall-web](modules/hmall-web.md) — 客户端 Vue 3 + Element Plus + Pinia。
- [hmall-admin](modules/hmall-admin.md) — 管理后台 Vue 3 + Element Plus + ECharts。

## Flows

- [order-checkout-flow](flows/order-checkout-flow.md) — 加购→下单→支付端到端流程。
- [auth-and-gateway-flow](flows/auth-and-gateway-flow.md) — 登录、JWT 颁发与 Gateway 鉴权链。
