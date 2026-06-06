---
title: hmall-web
tracks:
  - hmall-web/
last_synced_commit: fb8d5f5
last_synced_date: 2026-06-06
sync_note: "fix(#131): OrderConfirm.vue 结算页仅暴露后端支持的余额支付，配送/发票改为只读说明，提交负载由 utils/checkoutOptions.js 限定为 OrderFormDTO 支持字段；新增 checkoutOptions 与 OrderConfirm Vitest 回归测试"
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
- 订单页支付流程（OrderDetail.vue）：`?pay=1` 参数自动弹出余额支付弹窗；支付走两步——
  先 `POST /pay-orders`（`createPayOrder`）拿到支付单ID，再 `POST /pay-orders/{id}`
  （`payOrderByBalance`）执行扣款。支付密码由用户填写，后端校验。
- 结算页支付方式（OrderConfirm.vue）：当前只展示余额支付（`paymentType=3`），避免展示
  pay-service 尚不支持的支付宝/微信渠道。配送方式与发票能力按只读说明呈现；提交订单时
  `utils/checkoutOptions.js` 只构造 `OrderFormDTO` 支持的 addressId/paymentType/freight/details/couponId。
- 再次购买（OrderList.vue / OrderDetail.vue）：遍历 `order.details` 逐条调 `cartStore.addItem`
  后跳转 `/cart`；若 `details` 为空则提示用户商品信息不完整。
- 收藏页加购（FavoriteList.vue）：按收藏记录 `itemId` 调 `cartStore.addItem`（payload 由
  `utils/cartFromFavorite.js` 构造），未登录先提示，后端异常由 axios 拦截器统一弹 msg。
- 个人中心（Profile.vue）：挂载时 `GET /users/profile` 拉真实资料填表并同步 userStore；
  头像走 `/upload/image` 拿 url 后立即 `PUT /users/profile` 持久化；gender/birthday 随
  保存提交；订单统计跨分页累加（`utils/profileStats.js`），不再只统计第一页。绑定手机
  暂无后端更换流程，置为只读说明。
- 登录页（Login.vue）：用户名/邮箱/手机号任一登录（后端 `/users/login` 三标识匹配），
  找回密码仅邮箱验证码；"记住账号"仅存用户名到 localStorage。
