# Handoff: C 端首页与商品详情接入真实后端数据

## Status
In progress —— 实现完成，本地验收全绿，待开 PR 过 CI 合并。

## Files Changed
前端视图（2 个）：
- `hmall-web/src/views/Home.vue`（分页参数、促销 isRecommend、秒杀 `/seckill/active`、秒杀图片）
- `hmall-web/src/views/ItemDetail.vue`（真实图片、优惠券领取、评价筛选、加载更多、评价配图）

前端 API（1 个改 + 1 个新增）：
- `hmall-web/src/api/item.js`（getItems 走 normalizePageParams）
- `hmall-web/src/api/pageParams.js`（新增：page/size→pageNo/pageSize）

前端工具（4 个新增）：
- `hmall-web/src/utils/itemImages.js`
- `hmall-web/src/utils/reviewFilters.js`
- `hmall-web/src/utils/homeSections.js`
- `hmall-web/src/utils/seckill.js`

前端单测（5 个新增）：
- `hmall-web/src/__tests__/pageParams.spec.js`
- `hmall-web/src/__tests__/itemImages.spec.js`
- `hmall-web/src/__tests__/reviewFilters.spec.js`
- `hmall-web/src/__tests__/homeSections.spec.js`
- `hmall-web/src/__tests__/seckill.spec.js`

Harness 任务记录：
- `docs/agent-harness/tasks/active/2026-06-06-cwh-home-detail/`

知识库：
- `docs/knowledge-base/modules/hmall-web.md`（bump last_synced_commit + sync_note，覆盖 tracks 共变 K005）

## Backend
无后端改动。所需契约均已存在：`/items/page`(PageQuery)、`/seckill/active`(SeckillVO.itemImage)、
`/coupons` + `/coupons/{id}/claim`、ItemDTO.image/isRecommend、ItemReview.images/rating。

## CI Notes
- `codex-review` job 依赖仓库 secrets `OPENAI_API_KEY` / `OPENAI_RESPONSES_API_ENDPOINT`。
  若仓库未配置该 secret，该 job 无法运行；按 CLAUDE.md 要求在此说明：本任务无法自行注入该 secret，
  其阻塞为环境密钥缺失而非实现缺陷。前 4 个 job（lint/test/integration/smoke）须全绿。
