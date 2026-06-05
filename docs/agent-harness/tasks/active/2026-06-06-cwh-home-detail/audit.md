# Audit

按 #127 / #128 的验收建议逐项审计。

## #127 首页

| Requirement | Status | Evidence |
| --- | --- | --- |
| 使用 `/items/page` 正确分页字段（pageNo/pageSize） | done | 新增 `api/pageParams.normalizePageParams`，`getItems` 统一转换；6 条单测 |
| 秒杀区接入 `/seckill/active`，与 FlashSale 口径一致 | done | Home `loadFlashItems` 调 `getActiveSeckills`，复用 `utils/seckill.mapSeckillItem` |
| 热门/新品明确后端排序契约 | done | sortBy=sold / create_time + isAsc=false 显式传入（后端 PageQuery 支持） |
| 促销区有稳定后端依据 | done | `buildPromoItems` 优先 ItemDTO.isRecommend，无标记时降级切片，分区不空 |
| 秒杀真实图片 | done | mapSeckillItem 读 SeckillVO.itemImage，模板 `<img>`，无图降级占位 |

## #128 商品详情

| Requirement | Status | Evidence |
| --- | --- | --- |
| 商品图优先渲染后端真实图片 | done | `utils/itemImages.parseItemImages` 解析 ItemDTO.image（逗号分隔多图），主图/缩略图/详情图均渲染 `<img>`，无图降级占位色块 |
| 优惠券接入真实列表与领取接口 | done | 详情页 `getCoupons()` + `claimCoupon(id)`，登录校验 + 领取中禁用态；无券时隐藏模块 |
| 评价筛选实现过滤 | done | `utils/reviewFilters`：有图/好评(4-5)/中评(3)/差评(1-2) 客户端过滤 + 各维度计数；17 条单测 |
| “查看全部”可加载更多 | done | 由 `href="#"` 改为“查看更多评价（还有 N 条）”按钮，每次 +5 条；切换筛选时重置 |
| 评价配图渲染 | done | 评价项渲染 `reviewImages(r)` 缩略图 |

## 契约与安全

| Requirement | Status | Evidence |
| --- | --- | --- |
| 未改既有 endpoint 形态 | done | 仅前端调用既有 `/items/page`、`/seckill/active`、`/coupons`、`/coupons/{id}/claim` |
| 后端无改动 | done | 所需字段/接口均已存在，git diff 仅 hmall-web + harness/KB |
| 不可变写法 | done | 工具函数均返回新对象/数组，单测含不可变断言 |
| 无硬编码密钥 | done | 无新增凭据 |

## Deferred

| Item | Reason |
| --- | --- |
| 秒杀下单链路 | 后端无秒杀下单接口，详情页跳转保持现状（issue 未要求） |
| 评价后端分页接口 | 当前 `/items/{id}/reviews` 返回全量，前端分批展示已满足验收；后端分页可后续独立任务 |
