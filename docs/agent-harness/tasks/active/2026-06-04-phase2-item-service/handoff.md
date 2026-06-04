# Handoff: Phase 2 - item-service Admin Endpoints

## Status

已完成。PR #54 已提交，等待 codex-review 通过后合并。

## Objective

实现 `docs/superpowers/specs/2026-06-04-backend-api-fulfillment-design.md` 中 Phase 2 的全部端点，为 item-service 补齐管理端评价、商品批量操作、轮播图/广告位/秒杀功能。

## Scope

### 新增端点
1. `GET /admin/reviews?page&size&rating` → `PageDTO<ReviewVO>`
2. `PUT /admin/items/status`（body `{ids,status}`）→ `R<Void>`
3. `DELETE /admin/items`（body `{ids}`）→ `R<Void>`
4. `GET /items/{id}/related` → `List<ItemVO>`
5. `GET /banners` → `List<BannerVO>`
6. `GET /admin/banners` + CRUD + 排序 → 读 `PageDTO`/`List`，写 `R<Void>`
7. `GET /ads` → `List<BannerVO>`
8. `GET /seckill/active` → `List<SeckillVO>`

### 新增文件
- VO 类：ReviewVO、ItemVO、BannerVO、SeckillVO
- PO 类：Banner、Seckill
- Mapper：BannerMapper、SeckillMapper
- Service：IBannerService、ISeckillService、BannerServiceImpl、SeckillServiceImpl
- Controller：BannerController、SeckillController
- SQL 迁移脚本：banner、seckill 表

### 修改文件
- ReviewController.java（扩展）
- ItemController.java（扩展）

## Likely Files

- `item-service/src/main/java/com/hmall/item/controller/ReviewController.java`
- `item-service/src/main/java/com/hmall/item/controller/ItemController.java`
- `item-service/src/main/java/com/hmall/item/controller/BannerController.java`
- `item-service/src/main/java/com/hmall/item/controller/SeckillController.java`
- `item-service/src/main/java/com/hmall/item/domain/vo/*.java`
- `item-service/src/main/java/com/hmall/item/domain/po/Banner.java`
- `item-service/src/main/java/com/hmall/item/domain/po/Seckill.java`
- `item-service/src/main/java/com/hmall/item/mapper/BannerMapper.java`
- `item-service/src/main/java/com/hmall/item/mapper/SeckillMapper.java`
- `item-service/src/main/java/com/hmall/item/service/IBannerService.java`
- `item-service/src/main/java/com/hmall/item/service/ISeckillService.java`
- `item-service/src/main/java/com/hmall/item/service/impl/BannerServiceImpl.java`
- `item-service/src/main/java/com/hmall/item/service/impl/SeckillServiceImpl.java`
- `docs/sql/`（新增迁移脚本）

## Next Action

所有实现已完成，等待 CI codex-review 通过后合并 PR。

## Worktree Or Branch

- `task/2026-06-04-phase2-item-service`（从 main 切出）