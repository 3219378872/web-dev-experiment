---
title: cart-service
tracks:
  - cart-service/
last_synced_commit: 497554c
last_synced_date: 2026-06-03
sync_note: "2026-06-03: 移除 /hi 配置残留（hm-service 清理）"
---

# cart-service

## 职责

购物车增删查改与勾选项汇总。维护登录用户的购物车持久态，提供给
[trade-service](trade-service.md) 在下单时拉取勾选商品，并消费下单成功事件清理
对应商品。

## 公开接口与契约

- HTTP API：`/carts`（增/删/改/查）、`/carts/checkedItems`（拉取勾选项给下单流程）。
- 对外 Feign：[hm-api](hm-api.md) `CartClient` 暴露给 trade-service。
- 持久层：`hm-cart` 数据库；表 `cart`。
- RabbitMQ：消费 `trade.topic/order.create` 绑定到 `cart.order.create.queue`。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [trade-service](trade-service.md) 下单时拉取勾选项。
- [trade-service](trade-service.md) 下单成功后发布 `order.create` 事件。

## 下游

- MySQL（`hm-cart`）。
- 调用 [item-service](item-service.md) 拉取商品摘要（图片/价格）填充展示。

## 关键文件

- `CartApplication.java`、`controller/CartController.java`。
- `service/ICartService.java` 与 impl。
- `mapper/CartMapper.java`。
- `domain/po/Cart.java`、`domain/vo/CartVO.java`。
- `config/CartProperties.java` —— 单用户最大购物车容量等限制。
- `mq/OrderCreatedListener.java` —— 按事件中的 `userId` 与 `itemIds` 删除对应购物车项。

## 注意事项与陷阱

- 购物车单用户条目上限要在写入时校验，避免无限制堆积。
- 商品价格只在加购时记录"加购价"用于展示；真实下单价以 item-service 实时为准。
- 下单成功后通过 MQ 异步删除对应勾选项；listener 必须显式设置 `UserContext`，
  避免 ThreadLocal 缺失或串用户。
- `OrderCreatedListener` 失败时不直接进死信；先通过 [hm-common](hm-common.md)
  `MqConsumerSupport` 进入专用 retry queue，超过上限后进入死信队列。
