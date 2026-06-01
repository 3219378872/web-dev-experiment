---
title: cart-service
tracks:
  - cart-service/
last_synced_commit: 4d39250
last_synced_date: 2026-06-01
sync_note: "v3 合并 PR：所有阶段测试补足共 51 tests，覆盖率 80.5%"
---

# cart-service

## 职责

购物车增删查改与勾选项汇总。维护登录用户的购物车持久态，提供给
[trade-service](trade-service.md) 在下单时拉取勾选商品。

## 公开接口与契约

- HTTP API：`/carts`（增/删/改/查）、`/carts/checkedItems`（拉取勾选项给下单流程）。
- 对外 Feign：[hm-api](hm-api.md) `CartClient` 暴露给 trade-service。
- 持久层：`hm-cart` 数据库；表 `cart`。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [trade-service](trade-service.md) 下单时拉取勾选项。

## 下游

- MySQL（`hm-cart`）。
- 调用 [item-service](item-service.md) 拉取商品摘要（图片/价格）填充展示。

## 关键文件

- `CartApplication.java`、`controller/CartController.java`。
- `service/ICartService.java` 与 impl。
- `mapper/CartMapper.java`。
- `domain/po/Cart.java`、`domain/vo/CartVO.java`。
- `config/CartProperties.java` —— 单用户最大购物车容量等限制。

## 注意事项与陷阱

- 购物车单用户条目上限要在写入时校验，避免无限制堆积。
- 商品价格只在加购时记录"加购价"用于展示；真实下单价以 item-service 实时为准。
- 下单成功后必须删除对应勾选项，避免重复下单。
