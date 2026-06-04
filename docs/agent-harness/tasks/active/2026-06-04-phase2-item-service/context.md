# Context: Phase 2 - item-service Admin Endpoints

## Objective

实现 `docs/superpowers/specs/2026-06-04-backend-api-fulfillment-design.md` 中 Phase 2 的全部端点，为 item-service 补齐管理端评价、商品批量操作、轮播图/广告位/秒杀功能。

## Scope

- **In scope**:
  - B2 评价与商品：`GET /admin/reviews`（评价列表/搜索/分页）、`PUT /admin/items/status`（批量上下架）、`DELETE /admin/items`（批量删除）、`GET /items/{id}/related`（相关推荐）
  - B4 轮播图/广告位/秒杀：`GET /banners`（前台轮播图）、`GET /admin/banners`（管理端轮播图）、CRUD + 排序、`GET /ads`（广告位）、`GET /seckill/active`（秒杀）
- **Out of scope**:
  - 其他 Phase（user-service、trade-service、notify-service）
  - 现有端点的修改（避免破坏现网调用）

## Current State

经代码核验，item-service 当前状态：
- **未实现**: `GET /admin/reviews`、`GET /items/{id}/related`、`GET /banners`、`GET /admin/banners`、`GET /ads`、`GET /seckill/active`
- **部分实现**: `PUT /admin/items/{id}/status`（仅单条）、`DELETE /admin/items/{id}`（仅单条）
- **缺失**: ReviewVO、ItemVO、BannerVO、SeckillVO 类；Banner、Seckill 相关代码（PO、Mapper、Service、Controller）

## Requirements

1. **端点契约**（来自 spec §5）：
   - `GET /admin/reviews?page&size&rating` → `PageDTO<ReviewVO>`（rating 可选过滤）
   - `PUT /admin/items/status`（body `{ids,status}`）→ `R<Void>`（批量上下架）
   - `DELETE /admin/items`（body `{ids}`）→ `R<Void>`（批量删除）
   - `GET /items/{id}/related` → `List<ItemVO>`（同分类取 N 条，排除自身）
   - `GET /banners` → `List<BannerVO>`（前台：启用 + 按 sort 排序）
   - `GET /admin/banners` + CRUD + 排序 → 读 `PageDTO`/`List`，写 `R<Void>`
   - `GET /ads` → `List<BannerVO>`（由 `banner` 表按 `type`/`position` 过滤派生）
   - `GET /seckill/active` → `List<SeckillVO>`（含 `endTime`、库存百分比）

2. **数据模型**（来自 spec §6）：
   - 新增 `banner` 表：`id, image_url, link_url, title, type, position, sort, status, create_time, update_time`
   - 新增 `seckill` 表：`id, item_id, seckill_price, start_time, end_time, stock, sold, status, create_time, update_time`
   - 新增 VO 类：ReviewVO、ItemVO、BannerVO、SeckillVO

3. **技术约束**：
   - 写操作返 `R<Void>`，读操作返 `*VO`/`PageDTO<T>`
   - `/admin/*` 端点依赖 Gateway 注入身份做鉴权
   - 不修改既有 endpoint 形态（避免破坏现网前端调用）

## Likely Files

- `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`（扩展）
- `item-service/src/main/java/com/hmall/item/controller/ItemController.java`（扩展）
- `item-service/src/main/java/com/hmall/item/controller/BannerController.java`（新增）
- `item-service/src/main/java/com/hmall/item/controller/SeckillController.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/vo/ReviewVO.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/vo/ItemVO.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/vo/BannerVO.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/vo/SeckillVO.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/po/Banner.java`（新增）
- `item-service/src/main/java/com/hmall/item/domain/po/Seckill.java`（新增）
- `item-service/src/main/java/com/hmall/item/mapper/BannerMapper.java`（新增）
- `item-service/src/main/java/com/hmall/item/mapper/SeckillMapper.java`（新增）
- `item-service/src/main/java/com/hmall/item/service/IBannerService.java`（新增）
- `item-service/src/main/java/com/hmall/item/service/ISeckillService.java`（新增）
- `item-service/src/main/java/com/hmall/item/service/impl/BannerServiceImpl.java`（新增）
- `item-service/src/main/java/com/hmall/item/service/impl/SeckillServiceImpl.java`（新增）

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token
- 不破坏现有公共 API 响应封装
- 新增端点必须遵循 R<Void>（写）+ *VO/PageDTO（读）约定
- 不修改既有 endpoint 形态（避免破坏现网前端调用）

## Worktree Or Branch

- `task/2026-06-04-phase2-item-service`（从 main 切出）