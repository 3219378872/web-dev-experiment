# Verification

## Build
```bash
cd hmall-web
npm ci
npm run build
```
- Build passes with no errors
- All chunks compile successfully

## Code Review

### Issue #91 - Category Pagination
- [x] Template no longer shows `cat.count` 
- [x] `categoriesWithCount` computed removed
- [x] `filteredItems` simplified to server-side only
- [x] Pagination controls preserved (`page`, `size`, `total`, `totalPages`)

### Issue #93 - Dynamic Spec Selector
- [x] No hardcoded color/version strings in template
- [x] `specGroups` computed handles JSON / pipe / slash formats
- [x] `selectedSpecs` ref dynamically sized per specGroups
- [x] Fallback to read-only display for unstructured spec text

### Issue #94 - Sticky Tab Banner
- [x] `.dtabs` changed from `position: sticky; top: 118px` to `position: relative`

### Issue #99 - Hardcoded Data
- [x] Order countdown uses live timer from `createTime + 30min`
- [x] Home carousel fetches from `GET /banners` with auto-rotation
- [x] Home flash sale has `v-if="p.originalPrice"` null guard
- [x] FlashSale time slots computed from current time
- [x] FlashSale pagination uses windowed display (5 pages)

### Issue #103 - API Integration
- [x] `getFreight()` API added to order.js
- [x] Freight loaded on address change in OrderConfirm.vue
- [x] `getAvailableCoupons()` API added to order.js
- [x] Coupon selector with discount calculation in OrderConfirm.vue
- [x] `getLogistics()` API added to order.js
- [x] Real logistics data used in OrderDetail.vue (empty state handled)
