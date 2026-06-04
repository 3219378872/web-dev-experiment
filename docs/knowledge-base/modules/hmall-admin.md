---
title: hmall-admin
tracks:
  - hmall-admin/
last_synced_commit: e23d151
last_synced_date: 2026-06-04
sync_note: "对齐全部管理后台页至高保真原型视觉：全局样式/AdminLayout、Dashboard/Login/ItemList/ItemEdit/CategoryList/OrderList/OrderDetail/UserList/ReviewList/BannerList/NotificationList/FeedbackList/Profile；保持 API 与数据逻辑不变。额外更新：Login.vue 完全重写对齐原型（深色侧边栏、统计数字、角色选择、验证码）。2026-06-04 收尾：修复 OrderList 分页功能回归（装饰性→功能性 prev/next/fetch(p)）；AdminLayout topbar 恢复退出登录按钮；删除 OrderList 死代码 allChecked。codex-review 修正：FeedbackList 状态对齐后端真实两态（0 待处理/1 已回复），移除无法持久化的自定义状态编辑与 0/2/3 过滤，回复仅提交 reply。第 4 轮：ItemEdit 改用 getItemById 直连 GET /items/{id}（原先 size:1 列表查找必然漏数据）。第 8 轮：ItemEdit 保存时价格由「元」换算回整数「分」（后端 ItemDTO.price 为 Integer 分，原样回传"299.00"会反序列化失败/价格错乱）。第 11 轮：OrderList 搜索接入真实过滤（orderId 走后端，客户/支付/日期客户端过滤），支付列按 paymentType(1支付宝/2微信/3余额) 映射；AdminLayout 顶栏徽标改真实接口（待发货数/待处理反馈数）、身份取自 adminInfo，去硬编码。Dashboard 说明：看板运营统计（成交额/趋势/品类/访客/新增用户/待办/最新订单/热销）后端接口暂不存在（见 docs/backend-api.md B4），本页为视觉对齐+演示数据，待后端统计接口接入后替换为真实数据。"
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
