# Context: C 端首页与商品详情接入真实后端数据

## Objective
修复 hmall-web 首页（#127）热门/新品/促销/秒杀分区与商品详情页（#128）图片/优惠券/评价筛选的
静态或未接后端行为，使其消费真实 item-service / trade-service 数据。

## Scope
- In scope:
  - `hmall-web/src/views/Home.vue`：分页参数改用后端字段、促销分区基于 isRecommend、
    秒杀区接入 `/seckill/active`。
  - `hmall-web/src/views/ItemDetail.vue`：渲染后端 `item.image`、本店优惠券接入
    `/coupons` + `/coupons/{id}/claim`、评价筛选（有图/好评/中评/差评）客户端过滤、
    “查看全部”改为加载更多。
  - `hmall-web/src/api/item.js` + 新增 `hmall-web/src/api/pageParams.js`：统一 page/size→pageNo/pageSize。
  - 新增纯逻辑工具 `src/utils/{itemImages,reviewFilters,homeSections,seckill}.js` + Vitest 单测。
- Out of scope:
  - 后端无需改动（所需 endpoint 与字段均已存在：`/items/page` PageQuery、
    `/seckill/active` SeckillVO.itemImage、`/coupons`、ItemDTO.image/isRecommend、
    ItemReview.images/rating）。
  - FlashSale.vue 行为（已正确接 `/seckill/active`，不在 issue 范围）。
  - 秒杀下单链路（后端无该接口，详情页跳转保持现状）。

## Related Artifacts
- Spec: none —— 纯前端缺陷修复，复用既有后端契约，无新设计决策。
- Plan: none —— 单 PR 范围内的有界缺陷修复，逐文件改即可。

## Likely Files
- `hmall-web/src/views/Home.vue`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/api/item.js`
- `hmall-web/src/api/pageParams.js`（新增）
- `hmall-web/src/utils/{itemImages,reviewFilters,homeSections,seckill}.js`（新增）

## Runtime Evidence To Inspect First
- 后端 `PageQuery` 绑定 `pageNo/pageSize`，前端原传 `page/size` 不生效。
- `SeckillVO` 图片字段为 `itemImage`（非 `image`），无 `category` 字段。

## Safety Constraints
- 不修改既有 endpoint 形态；跨服务调用沿用前端既有 axios 拦截器约定。
- 不在源码硬编码密钥。
- 不破坏 `R<T>` / `PageDTO<T>` 公共契约。

## Worktree Or Branch
- `task/2026-06-06-cwh-home-detail`

## Open Questions
- none
