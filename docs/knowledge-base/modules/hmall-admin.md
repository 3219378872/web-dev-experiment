---
title: hmall-admin
tracks:
  - hmall-admin/
last_synced_commit: b05c078
last_synced_date: 2026-06-05
sync_note: "第 13 轮：Dashboard 接入真实 /admin/dashboard API（summary/trend/category-share/top-items/todo/latest-orders），移除全部 mock 数据。Login.vue 移除静态验证码/角色选择/假运营数字，改用 canvas 动态验证码。OrderList 修复 checkbox（el-checkbox）、全局 tab 计数（基于后端 total）、分页范围限制。ReviewList 接入真实统计（平均评分/好评率/差评数），修复 username/images 字段映射，服务端分页+评分过滤。Profile.vue 接入 /admin/profile API，从 localStorage 加载管理员信息，动态获取权限列表。NotificationList/FeedbackList 修复分页（传 page/size 参数），移除后端不存在的字段（category/pinned/popup/type）。"
---

# hmall-admin

## 职责

管理后台前端。Vue 3 + Element Plus + ECharts + Pinia + Vite，
覆盖商品、分类、订单、用户、优惠券、评价、文件、统计大盘共 13 页面。

## 公开接口与契约

- 所有 API 请求经 [hm-gateway](hm-gateway.md)；走管理端 `/admin/**` 路由，
  需要管理员角色 JWT。
- ECharts 大盘数据走 `/admin/stats/**`（trade/item/user 聚合接口）。
- 文件上传走 [file-service](file-service.md) `/files/upload`。

## 上游

后台运营人员浏览器。

## 下游

[hm-gateway](hm-gateway.md)；上传走 [file-service](file-service.md)。

## 关键文件

- `package.json`、`vite.config.ts`。
- `.eslintrc.cjs`、`.prettierrc` —— 代码质量与格式化配置。
- `src/api/` —— 管理端 API。
- `src/router/index.ts` —— 路由，含权限 meta。
- `src/views/` —— 13 个页面（商品/订单/用户/分类/优惠券/评价/文件/统计 等）。
- `src/components/` —— 表格、表单、图表共享组件。

## 注意事项与陷阱

- 管理端操作必须走 `R` 二次确认（如发货、退款、删除）。
- 大盘图表数据按日聚合，避免实时全量扫描。
- 富文本/图片上传只能走 [file-service](file-service.md) 预签名，不允许直传 MinIO。
- 修改商品状态必须显示当前状态机，避免误操作。
- 提交前运行 `npm run lint` 和 `npm run format`；pre-commit hook 自动检查。
