---
title: item-service
tracks:
  - item-service/
last_synced_commit: bd04dd7
last_synced_date: 2026-06-01
sync_note: "回滚过早合并的测试 PR（#28/#29/#30/#31），测试文件已移除待重新提交"
---

# item-service

## 职责

商品、分类、评价、搜索领域服务。维护 SKU 库存、商品上下架、评价聚合，
为前端提供商品列表/详情/搜索 API。

## 公开接口与契约

- HTTP API：`/items/**`、`/categories/**`、`/reviews/**`、`/search/**`。
- 对外 Feign：[hm-api](hm-api.md) `ItemClient` 暴露按 ID 批量查商品、扣减库存。
- 持久层：`hm-item` 数据库；可选 Elasticsearch 索引。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [cart-service](cart-service.md) 加购前查商品摘要。
- [trade-service](trade-service.md) 下单时校验库存、扣减库存。

## 下游

- MySQL（`hm-item`）、（可选）Elasticsearch、Redis 商品缓存。

## 关键文件

- `ItemApplication.java`、`controller/ItemController.java`、
  `controller/CategoryController.java`、`controller/ReviewController.java`、
  `controller/SearchController.java`。
- `service/IItemService.java` / `ICategoryService.java` / `IReviewService.java` 与 impl。
- `mapper/ItemMapper.java` / `CategoryMapper.java` / `ItemReviewMapper.java`。

## 注意事项与陷阱

- 库存扣减必须基于 `update ... where stock >= n` 乐观锁或行锁，禁止先查后扣。
- 商品状态机：草稿 → 上架 → 下架，不允许跳跃。
- 搜索字段若启用 ES，写入要走双写或 binlog 同步，保持一致性。
- 评价均分按需异步重算，避免每次写评价同步聚合。
