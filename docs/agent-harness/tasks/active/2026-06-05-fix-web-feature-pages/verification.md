# Verification

## Build Check
- `npm run build` - SUCCESS (1701 modules transformed, build completed in 5.10s)

## Issue #105 - Login Page
- [x] SMS login tab removed, only "账号登录" shown
- [x] Social login buttons and "其他登录方式" divider removed
- [x] captcha field removed from reset password form
- [x] Confirm password validation added before register submission
- [x] Remember-me saves/loads username from localStorage

## Issue #106 - Feedback/Service Pages
- [x] `uploadImage` API exported from `common.js`
- [x] Feedback.vue: file input triggers real upload, images preview with remove
- [x] Feedback.vue: submits `images` field with comma-separated URLs
- [x] `getFaqs` API exported from `common.js`
- [x] Service.vue: `quickQuestions` loaded from `getFaqs()` on mount
- [x] Service.vue: static welcome messages removed
- [x] Service.vue: toolbar buttons disabled with `.disabled` class

## Issue #107 - Review Image Upload
- [x] Upload button visible in review form area
- [x] Images upload via `uploadImage()` API
- [x] Preview thumbnails shown with remove button
- [x] `images` field included in review submission

## Issue #110 - FlashSale Page
- [x] `getActiveSeckills` API added to `item.js`
- [x] `loadFlash()` calls `/seckill/active` endpoint
- [x] Time slots built dynamically from seckill startTime
- [x] Product card shows seckill price, original price, stock bar
- [x] "立即抢购" navigates to item detail page

## Issue #111 - Search Page
- [x] Sort mapping sends `sortBy`/`isAsc` to backend
- [x] Client-side `sortedItems` computed removed, uses `items` directly
- [x] Sort change resets to page 1 and re-fetches
