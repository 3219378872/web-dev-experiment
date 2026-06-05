# Audit

## Requirements

| Requirement | Status | Evidence |
| --- | --- | --- |
| Review all hmall-web pages against `docs/front-spec.md` | done | Routes in `hmall-web/src/router/index.js`; views listed by `find hmall-web/src/views`; issues #89-#94, #99-#100, #103, #105-#107, #110-#111, #113. |
| Review all hmall-admin pages against `docs/front-spec.md` | done | Routes in `hmall-admin/src/router/index.js`; views listed by `find hmall-admin/src/views`; issues #95-#98, #101-#102, #104, #108-#109, #112. |
| Check whether pages use real backend APIs | done | Reviewed `hmall-web/src/api/*.js`, `hmall-admin/src/api/*.js`, and view imports/calls with `rg`; submitted issues for fake/local-only pages and missing API wrappers. |
| Check whether backend endpoints exist when frontend is not wired | done | Reviewed Controller mappings with `rg`; key evidence includes `DashboardController`, `BannerController`, `SeckillController`, `OrderController`, `CouponController`, `AdminProfileController`, `FaqController`, `FileController`. |
| Submit improvement issues for page gaps | done | Submitted #101-#113 during this task; earlier open issues #89-#100 cover already identified page gaps. |

## Page Coverage Matrix

### hmall-web

| Page | Audit result |
| --- | --- |
| `Home.vue` | Issues #90 and #99 cover category navigation, static carousel/countdown/ads; #102 covers backend Banner/Ads not wired. |
| `Category.vue` | Issue #91 covers loading only first 100 items and no pagination. |
| `ItemDetail.vue` | Issues #93, #94, #107 cover hardcoded specs, sticky tab behavior, missing review image upload. |
| `Search.vue` | Issue #111 covers current-page-only sorting instead of backend full-result sorting. |
| `Login.vue` (`/login`, `/register`, `/forgot`) | Issue #105 covers SMS/social/captcha mismatch and unsupported fields. |
| `Notifications.vue` | Issue #109 covers notification category/field mismatch with admin/front notification model. |
| `Cart.vue` | Issue #92 covers missing product name/image/price display. |
| `OrderConfirm.vue` | Issue #103 covers freight, available coupons, invoice/remark submission gaps. |
| `OrderList.vue` | Issues #89, #99, #100 cover order item display, fake countdown, payment failure path. |
| `OrderDetail.vue` | Issues #100 and #103 cover payment 500 and logistics/freight/coupon gaps. |
| `FavoriteList.vue` | Earlier issue #89/#69 family covers product name/image display in order/favorite pages; no new open duplicate submitted. |
| `AddressList.vue` | Issue #113 covers zipCode/tag fields not supported by backend. |
| `Profile.vue` | Current profile API is partially real; related avatar upload gap covered by #106/#107 upload pattern; no separate duplicate submitted. |
| `Feedback.vue` | Issue #106 covers screenshot upload and feedback payload gap. |
| `Coupons.vue` | Main list/claim/my-coupons are wired; order-use gap covered by #103. |
| `FlashSale.vue` | Issues #99 and #110 cover hardcoded UI and missing `/seckill/active` wiring. |
| `Service.vue` | Issue #106 covers FAQ/history/one-way message mismatch. |

### hmall-admin

| Page | Audit result |
| --- | --- |
| `Login.vue` | Issue #112 covers static captcha, role picker, remember state, fake stats. |
| `Dashboard.vue` | Issue #101 covers not using implemented `/admin/dashboard/*` APIs. |
| `UserList.vue` | Issue #98 covers detail placeholder; earlier backend/admin user work means list/status API exists. |
| `ItemList.vue` | Issue #95 covers filters, batch ops, stats and pagination behavior. |
| `ItemEdit.vue` | Issue #108 covers hardcoded categories, fake upload, static specs, fake publish settings. |
| `CategoryList.vue` | Issue #96 covers fake counts and totals. |
| `OrderList.vue` | Issue #97 covers fake selection, stats, pagination and operation feedback. |
| `OrderDetail.vue` | Covered by #97/#103 for admin order operations and logistics/detail data gaps. |
| `ReviewList.vue` | Issue #98 covers stats; #107 covers review images format from frontend submission. |
| `BannerList.vue` | Issue #102 covers local-only state and missing Banner CRUD wiring. |
| `NotificationList.vue` | Issue #109 covers unsupported fields and pagination/filter mismatch. |
| `FeedbackList.vue` | Issue #109 covers fake pagination, fake images, and no-op transfer. |
| `Profile.vue` | Issue #104 covers missing admin profile/permissions wiring. |

## New Issues Submitted In This Task

- #101 `Dashboard.vue`: real dashboard APIs implemented but not wired.
- #102 `BannerList.vue` / `Home.vue`: Banner/Ads CRUD and frontend display not wired.
- #103 `OrderConfirm.vue` / `OrderDetail.vue`: freight, available coupons, logistics not wired.
- #104 `Profile.vue` admin: admin profile/permissions not wired.
- #105 `Login.vue` web: unsupported SMS/social/captcha auth flows.
- #106 `Feedback.vue` / `Service.vue`: FAQ/upload/history gaps.
- #107 `ItemDetail.vue`: review image upload missing.
- #108 `ItemEdit.vue`: hardcoded categories, fake upload/specs/publish controls.
- #109 `NotificationList.vue` / `FeedbackList.vue` / `Notifications.vue`: field and pagination mismatch.
- #110 `FlashSale.vue`: `/seckill/active` not wired.
- #111 `Search.vue`: sorting is current-page only.
- #112 `Login.vue` admin: static captcha/role/stats.
- #113 `AddressList.vue`: zipCode/tag not persisted by backend.
