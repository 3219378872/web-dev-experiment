---
title: hmall-web
tracks:
  - hmall-web/
last_synced_commit: e23d151
last_synced_date: 2026-06-04
sync_note: "对齐全部客户端页至高保真原型视觉：全局样式/组件、Home/Category/Search/ItemDetail/Cart/OrderConfirm/OrderList/OrderDetail/Login/Profile/AddressList/FavoriteList/Feedback/Notifications/Coupons/FlashSale/Service；保持 API 与数据逻辑不变。额外更新：Login.vue 注册/找回密码模式结构对齐原型、App.vue 认证页隐藏布局头尾。2026-06-04 收尾：main.js 引入 assets/styles/components.css；App.vue 新增通栏布局（FlashSale）；FavoriteList 卡片重设计移除评分星级；修复 App.spec.ts 路由上下文。codex-review 修正：AppHeader 改绑真实用户名与购物车数量（去硬编码）；FlashSale 接 /items/page 真实加载+真实加购；Service 留言接 POST /messages；Coupons 按真实数据计算我的券计数与平台/品类分类。第 2 轮：Coupons 映射改用真实 Coupon 字段（discountType/discountValue/minAmount/endTime），消除 满undefined。第 4 轮：OrderDetail 改用 getOrderById 直连 GET /orders/{id}（原先翻第一页客户端查找会漏单）。第 6 轮：Service 留言仅在真正提交成功（需登录）时回执，未登录提示登录、失败如实提示，杜绝匿名下静默丢消息却假装成功。第 7 轮：修复 Login 注册协议勾选框未绑定 agreed（导致注册被永久卡住）、找回密码缺新密码输入框（导致重置必失败）两处回归。第 8 轮：Login 注册补回用户名输入（后端 username @NotBlank，原表单缺失致注册必失败）；OrderConfirm 下单补传 addressId。第 9 轮：Feedback 我的反馈接 GET /my-feedbacks；Profile 账户统计（余额/优惠券/收藏/订单状态计数）接真实接口、去会员等级/积分等无后端虚构与假手机号；AccountSidebar/Home 去硬编码积分；OrderDetail 物流轨迹改为基于真实订单状态与时间戳的概要节点（去虚构快递员/网点）。第 10 轮：移除 Coupons 接口失败时的兜底假券（改空态，避免对假 ID 领取）、Notifications 空列表时的 6 条假公告与假字段合并（改真实公告中性映射，空则空态）。第 11 轮：FlashSale 去掉虚构 70% 秒杀价与假抢购率，展示真实商品价（与加购成交价一致）。第 12 轮：OrderDetail 支付方式映射修正为后端真值（1支付宝/2微信/3余额）；Search 接真实 /search/list（key+分页）、死分页器改为功能分页、移除无后端的模糊/精确模式；cart.js updateCartItem 改走 PUT /carts（body id+num）。"
---

# hmall-web

## 职责

客户端前端。Vue 3 + Element Plus + Pinia + Vue Router + Vite，
覆盖商品浏览、详情、加购、结算、订单、用户中心、优惠券、评价共 16 页面。

## 公开接口与契约

- 所有 API 请求经 [hm-gateway](hm-gateway.md)。后端响应封装当前不统一：
  异常一律是 `R<Void>`（`CommonExceptionAdvice`），成功响应可能是 `R<T>`/`R<Void>`、
  `*VO`/`*DTO`/`PageDTO<T>` 或裸 `void`/`Long`/`String`。axios 拦截器
  `r => r.data` 对两种形态都兼容，调用点按对应字段消费（如 `data?.list`）。
- 鉴权走 JWT；登录后 token 存 localStorage，axios 拦截器自动注入 Authorization header。
- Pinia store 持有用户态、购物车摘要、当前订单草稿。

## 上游

浏览器用户。

## 下游

[hm-gateway](hm-gateway.md)。

## 关键文件

- `package.json` —— 依赖与脚本。
- `vite.config.ts` —— 构建/代理配置。
- `.eslintrc.cjs`、`.prettierrc` —— 代码质量与格式化配置。
- `src/api/` —— axios 客户端、按服务划分的 API 调用。
- `src/router/index.ts` —— 路由。
- `src/stores/` —— Pinia store。
- `src/views/` —— 13 个页面。
- `src/components/` —— 共享组件。

## 注意事项与陷阱

- 价格展示一律分→元转换在前端做，避免后端返回 double。
- 鉴权失败（401）必须跳登录并清 token，不要静默重试。
- 商品图懒加载；首屏 LCP 关注度高。
- 路由 meta 标记是否需登录，由全局守卫统一处理。
- 提交前运行 `npm run lint` 和 `npm run format`；pre-commit hook 自动检查。
