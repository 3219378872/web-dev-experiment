---
status: active
slug: fix-admin-pages
title: Fix hmall-admin page issues
---

# Handoff

## Summary
Fixed 6 hmall-admin management backend page issues by connecting to real backend APIs, fixing UI components, and removing demo/static data.

## Changed files

### Issue #97 - Order management
- `hmall-admin/src/views/OrderList.vue` - Real checkboxes, loading states, always-show pagination

### Issue #98 - Review stats & user detail
- `hmall-admin/src/views/ReviewList.vue` - Computed avgRating/goodRate/pendingCount, pagination params
- `hmall-admin/src/views/UserList.vue` - User detail dialog with getUserById API
- `hmall-admin/src/api/item.js` - getReviews now accepts params
- `hmall-admin/src/api/user.js` - Added getUserById, toggleUserStatus with data body

### Issue #101 - Dashboard API integration
- `hmall-admin/src/api/dashboard.js` - New API file for 6 dashboard endpoints
- `hmall-admin/src/views/Dashboard.vue` - Real data integration, loading/error states

### Issue #104 - Profile API connection
- `hmall-admin/src/views/Profile.vue` - Real API calls for save, password, permissions
- `hmall-admin/src/api/user.js` - Added updateAdminProfile, getAdminPermissions

### Issue #109 - Notification/Feedback pagination
- `hmall-admin/src/views/NotificationList.vue` - Pagination params, removed unsupported fields
- `hmall-admin/src/views/FeedbackList.vue` - el-pagination, removed placeholders/buttons

### Issue #112 - Login cleanup
- `hmall-admin/src/views/Login.vue` - Removed captcha, role selector, static stats

## Commits
```
fix(hmall-admin): resolve admin page issues #97 #98 #101 #104 #109 #112
```

## PR
```
gh pr create --title "fix(hmall-admin): resolve admin page issues #97 #98 #101 #104 #109 #112" --base main
```
