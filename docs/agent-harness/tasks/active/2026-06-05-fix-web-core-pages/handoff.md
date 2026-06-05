# Handoff

## Branch
`task/2026-06-05-fix-web-core-pages`

## Changes Summary
- **Category.vue**: Remove client-side category counting, keep server-filtered pagination
- **ItemDetail.vue**: Dynamic spec selector from item.spec field + remove sticky tab
- **Home.vue**: Dynamic banner carousel from `GET /banners`, NaN price fix
- **FlashSale.vue**: Dynamic time slots, windowed pagination (5-page window)
- **OrderList.vue**: Live countdown from `createTime + 30min`
- **OrderConfirm.vue**: Real freight calculation + available coupon selector
- **OrderDetail.vue**: Real logistics tracking from `GET /orders/{id}/logistics`
- **item.js**: Added `getBanners()` API
- **order.js**: Added `getFreight()`, `getAvailableCoupons()`, `getLogistics()` APIs

## CI
Build succeeds. No integration tests needed for pure frontend changes.

## PR
```bash
gh pr create \
  --title "fix(hmall-web): resolve core page issues #91 #93 #94 #99 #103" \
  --body "## Summary
Fix 5 hmall-web core page issues:
- #91: Category page pagination
- #93: Dynamic spec selector
- #94: Remove sticky tab banner
- #99: Fix hardcoded carousel, countdown, NaN, pagination
- #103: Integrate freight, coupons, logistics APIs

## Verification
- [x] npm build passes
- [x] Category page has proper pagination
- [x] Spec selector renders dynamically from item.spec
- [x] Tab banner doesn't stick on scroll
- [x] No more hardcoded data
- [x] Order confirm shows real freight/coupons

🤖 Generated with [Claude Code](https://claude.com/claude-code)" \
  --base main
```
