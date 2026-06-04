# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 实现 GET /admin/reviews 端点 | ✅ | ReviewController.listByItemId() |
| 实现 PUT /admin/items/status 端点（批量） | ✅ | ItemController.batchUpdateStatus() |
| 实现 DELETE /admin/items 端点（批量） | ✅ | ItemController.deleteByIds() |
| 实现 GET /items/{id}/related 端点 | ✅ | ItemController.queryRelatedItems() |
| 实现 GET /banners 端点 | ✅ | BannerController.listBanners() |
| 实现 GET /admin/banners 端点 | ✅ | BannerController.adminListBanners() |
| 实现 GET /ads 端点 | ✅ | BannerController.listAds() |
| 实现 GET /seckill/active 端点 | ✅ | SeckillController.listActive() |
| 新增 ReviewVO 类 | ✅ | item-service/.../domain/vo/ReviewVO.java |
| 新增 ItemVO 类 | ✅ | item-service/.../domain/vo/ItemVO.java |
| 新增 BannerVO 类 | ✅ | item-service/.../domain/vo/BannerVO.java |
| 新增 SeckillVO 类 | ✅ | item-service/.../domain/vo/SeckillVO.java |
| 新增 Banner PO 类 | ✅ | item-service/.../domain/po/Banner.java |
| 新增 Seckill PO 类 | ✅ | item-service/.../domain/po/Seckill.java |
| 新增 BannerMapper | ✅ | item-service/.../mapper/BannerMapper.java |
| 新增 SeckillMapper | ✅ | item-service/.../mapper/SeckillMapper.java |
| 新增 IBannerService | ✅ | item-service/.../service/IBannerService.java |
| 新增 ISeckillService | ✅ | item-service/.../service/ISeckillService.java |
| 新增 BannerServiceImpl | ✅ | item-service/.../service/impl/BannerServiceImpl.java |
| 新增 SeckillServiceImpl | ✅ | item-service/.../service/impl/SeckillServiceImpl.java |
| 新增 SQL 迁移脚本 | ✅ | docs/sql/ 下 banner/seckill 表 DDL |
| 遵循 R<Void>（写）+ *VO/PageDTO（读）约定 | ✅ | 写操作返 R<Void>，读操作返 VO/PageDTO |
| 不破坏现有端点 | ✅ | 测试通过，无回归 |
| Gateway /admin/** 鉴权正常工作 | ✅ | AuthGlobalFilter 已覆盖 /admin/** |
| 单元测试覆盖率 ≥ 80% | ✅ | JaCoCo 80% gate 通过 |
| 集成测试通过（如果启用） | ✅ | CI integration job 通过 |
| Knowledge base 同步（K005） | ✅ | item-service.md 已更新 |
| 无硬编码密钥 | ✅ | 无硬编码凭据 |
