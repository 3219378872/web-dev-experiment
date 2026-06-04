# Handoff: Frontend Design Finalize

## Status
Implemented，待 PR / CI / review。

## Files Changed
- 前端视觉对齐（hmall-web）：`App.vue`、`main.js`、`components/{AccountSidebar,AppHeader,ProductCard}.vue`、`styles/global.css`、`views/{AddressList,Coupons,FavoriteList,Feedback,FlashSale,Home,ItemDetail,Notifications,OrderList,Profile,Search}.vue`
- 前端视觉对齐（hmall-admin）：`hmall-admin/src/components/AdminLayout.vue`、`views/{BannerList,CategoryList,FeedbackList,ItemEdit,OrderDetail,OrderList}.vue`
- 分页回归修复：`hmall-admin/src/views/OrderList.vue`
- 单测修复：`hmall-web/src/__tests__/App.spec.ts`
- e2e：`e2e/visual/{fixtures.ts,regression.spec.ts,utils.ts}`
- 配置：`.gitignore`（新增 `test-results/` 忽略）
- KB：`docs/knowledge-base/modules/{hmall-web,hmall-admin}.md`（K005 co-change）
- 文档修正：`docs/backend-api.md`（基于 `docs/api.md` 与后端 Controller 源码重写待补接口表）
- 任务记录：`docs/agent-harness/tasks/active/2026-06-03-frontend-design-finalize/*`

## Commands Run
- `cd hmall-web && npm test && npm run build` → 全部通过
- `cd hmall-admin && npm test && npm run build` → 全部通过
- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/engineering-lint.py` → passed（补 KB 两页后）
- `docker compose config -q` → 有效

## Known Risks
- ⚠️ 核对 backend-api.md 时发现 2 处前端↔后端路径不一致（现网会 404）：`hmall-web` `searchItems` 调 `/items/search`（应为 `/search/list`）、`updateCartItem` 调 `PUT /carts/{id}`（应为 `PUT /carts` body 带 id）。本 PR 仅在 `docs/backend-api.md` A 节记录、未改前端调用以免扩大范围；建议后续单独修复（P0）。
- `hmall-admin/src/views/FeedbackList.vue` 分页器为装饰性（页码 2/3 无 handler），因其 `fetch()` 不向后端传分页参数、数据层本不分页；HEAD 版的 `el-pagination` 翻页也只是重拉全量。**非行为回归**，未改动；如后续需要真分页须同时改 `getFeedbacks` 接口与 fetch。
- 视觉对齐为大面积模板/样式改动，已通过两前端 build + 单测；像素级回归由 e2e/visual 套件覆盖（CI smoke 不跑视觉套件，仅 build/test）。
- CI 的 `codex-review` 若因缺 secrets 阻塞，与本变更无关，按 CLAUDE.md 在 PR 描述说明。

## Codex-Review 整改（第 1 轮 blocking findings）
- AppHeader.vue：用户名/购物车角标去硬编码，改绑 `userStore.userInfo.username` 与 `cartStore.totalCount`。
- FlashSale.vue：移除原型硬编码商品与 no-op 抢购，改为 `/items/page` 真实加载 + `cartStore.addItem` 真实加购 + 真实分页。
- Service.vue：留言接 `POST /messages`（新增 `api/common.sendCustomerMessage`），去除未完成标记。
- Coupons.vue：我的券计数与平台/品类分类改为基于真实返回数据，去除未完成标记与演示注释。
- 说明：admin Dashboard、web Notifications 的演示数据未改（统计/富展示字段的后端不存在，见 docs/backend-api.md B 节），本轮 codex 未将其列为 blocking。

## Codex-Review 整改（第 2 轮）
- Coupons.vue：领券与我的券映射改用真实 Coupon 字段（discountType/discountValue/minAmount/endTime），消除真实数据下的「满undefined可用」/空折扣。
- 移除冗余二进制 `web-mall.zip`（与 prototype/ 内容重复），新增 `.gitignore` 忽略 `*.zip`。

## Codex-Review 整改（第 3 轮）
- FeedbackList.vue：状态对齐后端真实两态（0 待处理 / 1 已回复）。移除可编辑状态 chip 与对 0/2/3 的 tab 过滤（后端只产生 0/1），回复仅提交 reply（reply 接口固定置 1）。修正本分支重设计引入的状态不一致回归。
- backend-api.md：修正"反馈回复 body 可带 status"的错误表述（后端忽略并强制置 1）。

## Codex-Review 整改（第 4 轮）
- web OrderDetail.vue：新增 `api/order.getOrderById`，改用 `GET /orders/{id}` 直取订单，替代「翻第一页 size:20 客户端查找」（超出首页的订单会误判不存在）。
- admin ItemEdit.vue：新增 `api/item.getItemById`，改用 `GET /items/{id}` 直取商品，替代「size:1 列表查找」（必然漏数据）；同时移除无效的占位图片路径填充。

## Codex-Review 整改（第 5 轮）—— Dashboard 范围声明
- codex 第 5 轮拦 admin Dashboard「硬编码运营数据」。核查：main 版本 Dashboard 本就无任何后端数据调用（仅 ref+echarts），看板统计接口后端完全不存在（backend-api.md B4）。
- 按决策不回退/不造后端，而是**明确披露范围**：Dashboard.vue 顶部加说明注释；context.md Out of scope、KB hmall-admin、PR 描述均声明「看板运营数据后端暂未提供，本页为视觉对齐+演示数据，留待后端接入」。
- 该项为已知且已披露的范围限制，不属于破坏既有 API/数据逻辑（main 同样无后端调用）。

## Codex-Review 整改（第 6 轮）
- Service.vue：`/service` 为公开路由但 `POST /messages` 需登录。改为：未登录时回执提示先登录、不调用接口；登录后仅在提交成功时回执「已收到」，失败如实提示。消除匿名用户下「静默丢消息却显示已收到」的假成功。
- 第 5 轮的 Dashboard 范围声明已生效，codex 不再就 Dashboard 阻塞。

## Codex-Review 整改（第 7 轮）
- Login.vue 注册：协议勾选框由硬编码 `checkbox on`（视觉已勾但 `agreed` 恒 false）改为 `:class="{on:agreed}" @click` 双向绑定，修复"注册永远提示请同意用户协议"。
- Login.vue 找回密码：重设计后遗漏新密码输入框，补回 `resetForm.newPassword` 输入并在 doReset 增加非空校验，修复"重置必失败"。

## Codex-Review 整改（第 8 轮）
- Login.vue 注册：补回用户名输入框（后端 RegisterFormDTO.username 为 @NotBlank，重设计遗漏导致注册必失败），doRegister 增加用户名/密码校验。
- admin ItemEdit.vue：保存时价格由「元」换算回整数「分」（后端 ItemDTO.price 为 Integer 分，原样回传"299.00"会反序列化失败或价格错乱）。
- OrderConfirm.vue：下单 payload 补传 addressId（后端 OrderFormDTO 有该字段，虽当前未被服务使用，补齐更正确）。

## Next Action
推远程开 PR，过 CI 与 review，合并后删除远程分支，并将本任务移至 completed/（status: done）。

## Worktree Or Branch
- `task/2026-06-03-frontend-design-finalize`（由原 `feat/fronted-design` 按 harness 规范重命名，未推送前重命名，历史完整）
