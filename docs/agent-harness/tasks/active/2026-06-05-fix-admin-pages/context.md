---
status: active
slug: fix-admin-pages
title: Fix hmall-admin page issues #97 #98 #101 #104 #109 #112
created: 2026-06-05
---

# Context

Fix 6 hmall-admin management backend page issues:

## #97 - Order management page issues
- OrderList.vue: Fake `<span class="checkbox">` checkboxes not functional
- Action buttons (shipOrder, updateOrderStatus) without loading/error feedback
- Stats (todayCount, pendingShipCount, refundCount) computed from current page only
- Pagination hidden when totalPages <= 1

## #98 - Review stats & user detail
- ReviewList.vue: avgRating, goodRate, pendingCount placeholders showing `—`
- UserList.vue: "详情" button shows `$message.info('详情')` with no real dialog

## #101 - Dashboard demo data
- Dashboard.vue uses hardcoded demo data (128,640 / 1,286 / 486 / 42,180 etc.)
- 6 backend dashboard APIs already exist but not consumed

## #104 - Profile not connected to API
- Profile.vue uses static demo data
- Save functions don't call real APIs
- Permissions are hardcoded

## #109 - Notification/Feedback pagination
- NotificationList.vue: fetch() doesn't pass page/pageSize params
- Category/pinned/popup fields not supported by backend
- FeedbackList.vue: manual pagination, placeholder image blocks, "转技术" button with no backend

## #112 - Login page static content
- Static captcha, role selector, hardcoded stats numbers
- "忘记密码" has no action
