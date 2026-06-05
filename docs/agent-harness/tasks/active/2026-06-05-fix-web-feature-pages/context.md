# Context: Fix hmall-web Feature Pages

## Issues

### Issue #105 - Login page unimplemented features
- **SMS login tab**: Removed the entire SMS login template and tab switch button
- **Social login buttons**: Removed WeChat/Alipay/QQ/Weibo static buttons and "other login methods" divider
- **sendCode params**: `sendCode` API already uses `email` as query param name; `sendSmsCode` function removed entirely
- **Reset password captcha**: Removed the captcha field (static "8 K 2 d" graphic) from reset password form
- **Register confirm password**: Added validation checking `password === confirmPassword` before submission
- **Remember me**: Added localStorage persistence for "rememberedUser" username, auto-restored on mount

### Issue #106 - Feedback and Service pages
- **Feedback image upload**: Added `uploadImage(file)` API to `common.js`; implemented real file upload in Feedback.vue via hidden `<input type="file">`; preview and remove uploaded images; submit image URLs (comma-separated) with feedback
- **Service FAQ loading**: Added `getFaqs()` API to `common.js`; `quickQuestions` now loaded from `GET /faqs` on mount, falls back to static list on error
- **Static messages removed**: Removed the hardcoded welcome conversation history; only the initial greeting message remains
- **Disabled toolbar buttons**: Emoji/image/attachment/order buttons in chat toolbar marked with `.disabled` class (grayed out, non-functional)

### Issue #107 - Review image upload
- Added image upload button in review form area on ItemDetail.vue
- Upload via `uploadImage()` API, preview thumbnails, removable
- Image URLs (comma-separated) included in `images` field of review submission

### Issue #110 - FlashSale page seckill API integration
- Added `getActiveSeckills(params)` API to `item.js`
- `loadFlash()` now calls `GET /seckill/active` instead of `GET /items/page`
- Parses SeckillVO fields: `itemId`, `seckillPrice`, `startTime`, `endTime`, `stock`, `sold`, `percent`
- Time slots built dynamically from `startTime` grouping by hour
- Product card shows seckill price, original price (strikethrough), stock progress bar with percentage
- "立即抢购" button navigates to `/item/${itemId}` since no seckill order API exists

### Issue #111 - Search page server-side sorting
- Established sort mapping: `default` -> no params, `sold` -> `sortBy=sold,isAsc=false`, `priceAsc` -> `sortBy=price,isAsc=true`, `priceDesc` -> `sortBy=price,isAsc=false`, `new` -> `sortBy=create_time,isAsc=false`
- Sort parameters passed to `GET /search/list` backend call
- Removed client-side `sortedItems` computed property; template uses `items` directly
- Sort change resets to page 1 and re-fetches from server

## Files Modified
- `hmall-web/src/views/Login.vue`
- `hmall-web/src/views/Feedback.vue`
- `hmall-web/src/views/Service.vue`
- `hmall-web/src/views/ItemDetail.vue`
- `hmall-web/src/views/FlashSale.vue`
- `hmall-web/src/views/Search.vue`
- `hmall-web/src/api/common.js`
- `hmall-web/src/api/item.js`
