# Context: Fix hmall-web Core Pages Issues

## Summary

Fix 5 issues in the hmall-web (client-side) frontend: pagination, spec selector, sticky tabs,
hardcoded data, and missing API integrations.

## Issues Fixed

### Issue #91 - Category Page Pagination
- **Problem**: Category count was computed client-side from loaded items (only 20 per page),
  showing incorrect numbers.
- **Fix**: Removed `categoriesWithCount` computed and count spans. Server-side filtering was
  already implemented in `loadItems` - just needed to stop displaying wrong client-side counts.

### Issue #93 - Dynamic Spec Selector
- **Problem**: ItemDetail.vue had hardcoded color/version options (`曜石黑`, `云母白`, etc.)
- **Fix**: Parse `item.spec` field with multi-format detection:
  1. JSON format (`{"颜色":["曜石黑","云母白"]}`)
  2. Pipe-delimited key:value format (`颜色:黑色|内存:128GB`)
  3. Slash-delimited list (`黑色/白色/蓝色`)
  4. Fallback to read-only display
  Render dynamic spec groups with `v-for`.

### Issue #94 - Remove Sticky Tab Banner
- **Problem**: `.dtabs` had `position: sticky; top: 118px;` causing the tab bar to overlay
  content on scroll.
- **Fix**: Changed to `position: relative`.

### Issue #99 - Hardcoded Data
- **A. Order countdown**: Replaced static "剩 23:48" with live countdown from `createTime+30min`.
- **B. Home carousel**: Added `GET /banners` API integration with auto-rotation and dot navigation.
- **C. NaN price**: Added `v-if="p.originalPrice"` guard for home flash sale prices.
- **D. FlashSale time slots**: Computed dynamically from current time (08:00-22:00, 2h intervals).
- **E. FlashSale pagination**: Implemented windowed pagination (prev/next + 5-page window).

### Issue #103 - Freight, Coupons, Logistics APIs
- **A. Freight**: Added `GET /orders/freight?addressId=&amount=` call in OrderConfirm.vue.
- **B. Coupons**: Added `GET /my-coupons/available?amount=` with selectable dropdown.
- **C. Logistics**: Replaced fake computed logistics with `GET /orders/{id}/logistics` API call.

## Files Changed
- `hmall-web/src/views/Category.vue`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/views/Home.vue`
- `hmall-web/src/views/FlashSale.vue`
- `hmall-web/src/views/OrderList.vue`
- `hmall-web/src/views/OrderConfirm.vue`
- `hmall-web/src/views/OrderDetail.vue`
- `hmall-web/src/api/item.js`
- `hmall-web/src/api/order.js`
