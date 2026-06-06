# Context: Cweb Orders Coupons

## Objective
Fix GitHub issues #132 and #134 so C-side order search/refund actions and coupon status tabs match backend capabilities.

## Scope
- In scope:
  - `GET /orders` keyword filtering by order id or product name.
  - `GET /my-coupons` returning coupon data plus user-coupon status.
  - `hmall-web` order list/detail actions for refund status 6 and `/orders/{id}/refund`.
  - `hmall-web` coupon state mapping for unused/used/expired user coupons.
- Out of scope:
  - New "extend receipt" backend capability.
  - New order-bound review workflow beyond routing to the existing item review tab.
  - Checkout available-coupon behavior, which continues to use `/my-coupons/available`.

## Related Artifacts
- Spec: none - issues #132 and #134 are bounded bug reports with explicit acceptance criteria.
- Plan: none - grouped bugfix uses this harness record as the implementation checklist.

## Likely Files
- `trade-service/src/main/java/com/hmall/controller/OrderController.java`
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `trade-service/src/main/java/com/hmall/controller/CouponController.java`
- `trade-service/src/main/java/com/hmall/service/impl/CouponServiceImpl.java`
- `hmall-web/src/views/OrderList.vue`
- `hmall-web/src/views/OrderDetail.vue`
- `hmall-web/src/views/Coupons.vue`

## Runtime Evidence To Inspect First
- `gh issue view 132 --json number,title,body,labels,state,url`
- `gh issue view 134 --json number,title,body,labels,state,url`

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not change legacy successful response envelopes beyond the issue-specific `/my-coupons` VO enrichment.
- Do not repurpose status 5; trade-service uses status 6 for refund pending.
- Preserve `/my-coupons/available` as the checkout-only available-coupon endpoint.

## Worktree Or Branch
- `task/2026-06-06-cweb-orders-coupons`

## Open Questions
- none
