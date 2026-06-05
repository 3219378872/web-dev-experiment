---
title: item-service
tracks:
  - item-service/
last_synced_commit: aa63abd
last_synced_date: 2026-06-05
sync_note: "Issue #86: 添加 actuator 依赖，配合 docker-compose healthcheck"
---

# item-service

## 职责

商品、分类、评价、搜索、轮播图、秒杀领域服务。维护 SKU 库存、商品上下架、评价聚合、
轮播图/广告位管理、秒杀活动配置，为前端提供商品列表/详情/搜索/首页内容 API。

## 公开接口与契约

- HTTP API：`/items/**`、`/categories/**`、`/reviews/**`、`/search/**`、
  `/banners/**`、`/ads/**`、`/seckill/**`。
- 管理端 API：`/admin/items/**`、`/admin/reviews/**`、`/admin/banners/**`。
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
  `controller/SearchController.java`、`controller/BannerController.java`、
  `controller/SeckillController.java`。
- `service/IItemService.java` / `ICategoryService.java` / `IItemReviewService.java` /
  `IBannerService.java` / `ISeckillService.java` 与 impl。
- `mapper/ItemMapper.java` / `CategoryMapper.java` / `ItemReviewMapper.java` /
  `BannerMapper.java` / `SeckillMapper.java`。
- `domain/po/Banner.java` / `Seckill.java`。
- `domain/vo/ItemVO.java` / `ReviewVO.java` / `BannerVO.java` / `SeckillVO.java`。

## 新增接口（Phase 2）

### Banner 管理

- `GET /banners`：查询启用的轮播图列表（type=carousel, status=1），按 sort 排序。
- `GET /ads`：查询启用的广告位列表，支持 type/position 过滤。
- `GET /admin/banners`：管理端分页查询轮播图/广告位，支持 type 过滤。
- `POST /admin/banners`：创建轮播图/广告位，默认 status=1。
- `PUT /admin/banners/{id}`：更新轮播图/广告位。
- `DELETE /admin/banners/{id}`：删除轮播图/广告位。
- `PUT /admin/banners/{id}/status`：更新状态（启用/禁用）。
- `PUT /admin/banners/{id}/sort`：更新排序值。

### 秒杀活动

- `GET /seckill/active`：查询进行中的秒杀活动（status=1, 当前时间在 start_time~end_time 之间）。
  返回 SeckillVO，包含 itemName、itemImage、originalPrice、stockPercent。

### 商品管理扩展

- `GET /items/{id}/related`：查询相关推荐商品（同分类、排除自身、限制数量）。
- `PUT /admin/items/status`：批量更新商品状态（上架/下架）。
- `DELETE /admin/items`：批量删除商品。

### 评价管理扩展

- `GET /admin/reviews`：管理端分页查询评价列表，支持 rating 过滤。

## 注意事项与陷阱

- 库存扣减必须基于 `update ... where stock >= n` 乐观锁或行锁，禁止先查后扣。
- `deductStock` 加 `@Transactional`，作为下单 Seata AT 全局事务的 RM 分支：当
  [trade-service](trade-service.md) 的 `createOrder` 全局回滚时，本服务的库存扣减经
  `undo_log` 自动反向补偿。XID 经 Feign 头传播，需 `seata-spring-boot-starter`（默认关）。
- 商品状态机：草稿 → 上架 → 下架，不允许跳跃。
- 搜索字段若启用 ES，写入要走双写或 binlog 同步，保持一致性。
- 评价均分按需异步重算，避免每次写评价同步聚合。
- Banner 轮播图/广告位支持 type 区分（carousel/ad），position 用于广告位位置标识。
- Seckill 秒杀活动需校验时间窗口（start_time ≤ now ≤ end_time），stockPercent = sold/stock * 100。
