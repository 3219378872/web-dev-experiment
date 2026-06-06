# Handoff: Cweb Orders Coupons

## Status
Implementation in progress; local targeted backend, frontend, build, harness, KB, and engineering checks are passing. PR CI and codex-review still need to run before merge.

## Files Changed
- `trade-service/src/main/java/com/hmall/domain/vo/UserCouponVO.java`
- `trade-service/src/main/java/com/hmall/controller/CouponController.java`
- `trade-service/src/main/java/com/hmall/controller/OrderController.java`
- `trade-service/src/main/java/com/hmall/service/ICouponService.java`
- `trade-service/src/main/java/com/hmall/service/IOrderService.java`
- `trade-service/src/main/java/com/hmall/service/impl/CouponServiceImpl.java`
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `trade-service/src/test/java/com/hmall/service/impl/CouponServiceImplTest.java`
- `trade-service/src/test/java/com/hmall/service/impl/OrderServiceImplTest.java`
- `hmall-web/src/views/OrderList.vue`
- `hmall-web/src/views/OrderDetail.vue`
- `hmall-web/src/views/Coupons.vue`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/utils/couponStatus.js`
- `hmall-web/src/utils/orderStatus.js`
- `hmall-web/src/utils/itemDetailTabs.js`
- `hmall-web/src/__tests__/couponStatus.spec.js`
- `hmall-web/src/__tests__/orderStatus.spec.js`
- `hmall-web/src/__tests__/itemDetailTabs.spec.js`
- `docs/knowledge-base/modules/hmall-web.md`
- `docs/knowledge-base/modules/trade-service.md`
- `docs/knowledge-base/flows/order-checkout-flow.md`

## Commands Run
- `gh issue view 132 --json number,title,body,labels,state,url`
- `gh issue view 134 --json number,title,body,labels,state,url`
- `mvn -q -pl trade-service -am -Dtest=CouponServiceImplTest,OrderServiceImplTest -DfailIfNoTests=false test`
- `mvn -q -pl trade-service -am test`
- `cd hmall-web && npm ci`
- `cd hmall-web && npm test -- --run src/__tests__/couponStatus.spec.js src/__tests__/orderStatus.spec.js`
- `cd hmall-web && npm test -- --run src/__tests__/couponStatus.spec.js src/__tests__/orderStatus.spec.js src/__tests__/itemDetailTabs.spec.js`
- `cd hmall-web && npm run lint`
- `cd hmall-web && npm test`
- `cd hmall-web && npm run build`
- `python3 scripts/agent_harness.py check`
- `python3 scripts/knowledge_base.py check --base origin/main`
- `python3 scripts/engineering-lint.py`

## Known Risks
- `/my-coupons` response shape is intentionally enriched from `CouponVO` to `UserCouponVO`; checkout remains on `/my-coupons/available` to avoid behavior drift.
- Completed-order review uses the first order detail's existing item review tab, not a new order-specific review workflow.
- Rendered browser QA was not run locally because the Browser plugin, Playwright, and Chromium are unavailable in this worktree. Unit tests and Vite build cover the changed view logic; CI smoke/build remains required.

## Next Action
Run full frontend/backend/repo verification, push PR, wait for CI and codex-review, merge, then archive the harness task.

## Worktree Or Branch
- `task/2026-06-06-cweb-orders-coupons`
