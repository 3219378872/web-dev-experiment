# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Issue #132 reproduced from code | done | `OrderList.vue` sent `keyword`, but pre-fix `OrderController.myOrders` ignored it; refund tab used status `5`; detail after-sales linked to `/service`; extend-receipt button had no handler. |
| Issue #132 backend search | done | `OrderController.myOrders` now accepts `keyword`; `OrderServiceImpl.queryUserOrders` filters by order id or `OrderDetail.name` and returns details. |
| Issue #132 frontend refund/status actions | done | `hmall-web/src/utils/orderStatus.js` maps refund tab/status to `6`; list/detail call `refundOrder`; status `6` displays refund processing. |
| Issue #132 review/extend receipt cleanup | done | Completed-order review links route to `/item/{itemId}?tab=reviews`; unsupported extend-receipt control removed. |
| Issue #134 reproduced from code | done | Pre-fix `Coupons.vue` mapped all `/my-coupons` rows through `mapCoupon(c, 'use')`; backend returned `List<Coupon>` without `UserCoupon.status`. |
| Issue #134 backend user-coupon status | done | `UserCouponVO` carries `userCouponStatus`, `usedOrderId`, `useTime`, and `claimTime`; `/my-coupons` returns that VO. |
| Issue #134 frontend coupon tabs | done | `hmall-web/src/utils/couponStatus.js` maps status `1` to unused, status `0` to used, and expired unused coupons to expired. |
| Knowledge base updated | done | `docs/knowledge-base/modules/hmall-web.md`, `docs/knowledge-base/modules/trade-service.md`, and `docs/knowledge-base/flows/order-checkout-flow.md`. |
