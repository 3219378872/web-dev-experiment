---
status: active
slug: fix-admin-pages
title: Fix hmall-admin page issues
---

# Verification

## Build check
```bash
cd hmall-admin && npm run build
```
Expect: No errors. Verify all modified files compile correctly.

## Verification list

### #97 - OrderList.vue
- [ ] Header has working `<el-checkbox>` for select-all
- [ ] Each row has working `<el-checkbox>` 
- [ ] Action buttons show loading state during API calls
- [ ] Error messages shown on API failures
- [ ] Pagination always visible (no v-if)
- [ ] Empty state shown when no orders

### #98 - ReviewList.vue & UserList.vue
- [ ] Review stats show computed avgRating (1 decimal), goodRate (%), pendingCount
- [ ] Review fetch uses pagination params
- [ ] User detail dialog opens on "详情" click
- [ ] User detail shows username, phone, email, role, status, createTime

### #101 - Dashboard.vue
- [ ] Stats loaded from /admin/dashboard/summary API
- [ ] Trend chart from /admin/dashboard/trend API
- [ ] Category share from /admin/dashboard/category-share API
- [ ] Top items from /admin/dashboard/top-items API
- [ ] 待办事项接口 API
- [ ] Latest orders from /admin/dashboard/latest-orders API
- [ ] Loading/error/empty states handled
- [ ] No hardcoded demo data

### #104 - Profile.vue
- [ ] Profile form loads from localStorage (adminInfo from login)
- [ ] Permissions loaded from /admin/profile/permissions API
- [ ] saveProfile calls updateAdminProfile API
- [ ] savePwd validates and calls updateAdminProfile with password
- [ ] Login logs section shows "暂无数据"

### #109 - NotificationList.vue & FeedbackList.vue
- [ ] fetch passes page/size params
- [ ] Category/pinned/popup filter controls removed
- [ ] Pagination uses el-pagination components
- [ ] Placeholder image blocks removed from FeedbackList
- [ ] "转技术" button removed

### #112 - Login.vue
- [ ] Static captcha removed
- [ ] Role selector removed
- [ ] "保持登录" checkbox shown
- [ ] "忘记密码" click shows info toast
- [ ] Left sidebar stats replaced with brand text
