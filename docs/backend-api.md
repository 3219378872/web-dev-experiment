# 后端接口缺失/待对齐记录

> 本文件记录前端对齐高保真原型过程中发现的后端接口问题。
> **核对方法**：2026-06-04 将 `hmall-web`/`hmall-admin` 全部 `api/*.js` 调用逐条比对
> `docs/api.md`（源码自动生成的真实契约）并回到各服务 Controller 源码核验。
> 上一版本按"页面臆想需求"罗列，未与实际契约核对，存在大量误报与两处关键漏报，本版已修正。
> 技术栈：Vue3 + Element Plus；新增/修改 endpoint 约定：写操作返 `R<Void>`、读操作返 `*VO`/`PageDTO<T>`/`List<*>`。

---

## A. 路径待对齐（前端正在调用，后端已实现但路径不一致 → 当前会 404）

这两项最紧急：前端代码实际发出请求，但后端没有该路径，需对齐前端或后端其一。

| 前端调用 | 前端当前路径 | 后端实际路径 | 后端证据 | 建议 |
|---|---|---|---|---|
| `hmall-web` `searchItems`（Search.vue） | `GET /items/search` | `GET /search/list` | `SearchController` `@RequestMapping("/search")` + `@GetMapping("/list")` | 改前端 `item.js` 调 `/search/list`（后端已具备 key/category/brand/价格区间/分页） |
| `hmall-web` `updateCartItem`（Cart.vue） | `PUT /carts/{id}` | `PUT /carts`（body 携带 `id`+`num`） | `CartController` `@PutMapping`（无 `{id}`） | 改前端 `cart.js` 调 `PUT /carts` 并在 body 传 `{id,num}` |

> 注：`docs/api.md` §12.1 也把 `searchItems` 误记为 `/items/search`，对齐后需同步修订。

---

## B. 真实待补接口（后端无对应端点）

按服务/领域归类。"前端状态"列说明该需求当前是前端已在调用（会报错）还是纯前端占位（尚未接线）。

### B1. user-service —— 管理端用户管理（前端已在调用，缺整套）

| 接口 | 方法/路径（建议） | 前端调用 | 说明 |
|---|---|---|---|
| 用户列表（搜索/分页） | `GET /admin/users`（参数 page/size/keyword） | `hmall-admin` `getUsers`（UserList.vue，已在调用→404） | user-service 仅有 `/users`/`/addresses`/`/favorites`，无 `/admin/users` |
| 启用/禁用用户 | `PUT /admin/users/{id}/status` | `hmall-admin` `toggleUserStatus`（UserList.vue，已在调用→404） | 需在 user 表加 status 字段 |
| 用户详情 | `GET /admin/users/{id}` | UserList.vue 详情弹窗（纯前端占位） | 可复用 `GET /users/{id}` 或新增管理端详情 |

### B2. item-service —— 评价与商品

| 接口 | 方法/路径（建议） | 前端调用 | 说明 |
|---|---|---|---|
| 管理端评价列表 | `GET /admin/reviews`（参数 page/size/rating） | `hmall-admin` `getReviews`（ReviewList.vue，已在调用→404） | `ReviewController` 只有按商品查 + `DELETE /admin/reviews/{id}` |
| 商品批量上下架/删除 | `PUT /admin/items/status`、`DELETE /admin/items`（批量 ids） | ItemList.vue 批量操作（纯前端占位） | 当前仅单条 status/delete |
| 相关推荐 | `GET /items/{id}/related` | ItemDetail.vue 推荐位（纯前端占位） | 无推荐端点 |

### B3. trade-service —— 下单/订单/优惠券补充

| 接口 | 方法/路径（建议） | 前端调用 | 说明 |
|---|---|---|---|
| 运费计算 | `GET /orders/freight`（参数 addressId、商品） | OrderConfirm.vue（纯前端占位） | 无运费端点 |
| 下单可用优惠券查询 | `GET /my-coupons/available`（参数购物车金额） | OrderConfirm.vue 选券（纯前端占位） | 已有 `/my-coupons` 列出全部，但无"对本单可用"过滤 |
| 管理端退款审核 | `PUT /admin/orders/{id}/refund-audit`（通过/驳回） | OrderList.vue 售后（纯前端占位） | 当前退款仅用户侧 `POST /orders/{id}/refund`，管理端只能改状态 |
| 导出订单 | `GET /admin/orders/export` | OrderList.vue 导出按钮（纯前端占位） | 无导出端点 |
| 物流时间轴 | `GET /orders/{id}/logistics` | OrderDetail.vue 物流时间轴（纯前端占位） | 订单详情 `GET /orders/{id}` 已有，但不含物流轨迹 |

### B4. 全新领域（后端尚无对应 Controller，已确认 16 个 Controller 中均无）

| 领域 | 接口（建议） | 前端 | 说明 |
|---|---|---|---|
| 轮播图 | `GET /banners`（前台）、`/admin/banners` CRUD + 排序 | Home.vue 轮播 / BannerList.vue（纯前端） | 无 banner 服务，BannerList 仅本地 state |
| 限时秒杀 | `GET /seckill/active`（含倒计时、库存百分比） | Home.vue 秒杀区 / FlashSale.vue（纯前端） | 无秒杀端点 |
| 数据看板 | `GET /admin/dashboard/*`（成交额/订单/新增用户/访客/趋势/品类占比/热销 TOP5/待办/最新订单） | Dashboard.vue（全硬编码） | 无统计端点 |
| 在线客服 | 客服消息收发（WebSocket/轮询）、`GET /faqs` 常见问题 | Service.vue（纯前端） | `notify-service` 有 `/messages` 留言但非实时客服 |
| 首页广告位 | `GET /ads`（双广告位配置） | Home.vue 广告位（纯前端） | 可由 `isAD` 商品派生，或新增配置端点 |

### B5. 管理端个人中心

| 接口 | 方法/路径（建议） | 前端 | 说明 |
|---|---|---|---|
| 管理员改密码/信息 | 复用 `PUT /users/profile` 或新增 `/admin/profile` | Profile.vue（admin，无实际调用） | 管理端用 adminToken，需确认复用是否可行 |
| 当前管理员角色权限 | `GET /admin/profile/permissions` | Profile.vue（admin，纯前端） | 无权限端点 |

---

## C. 上一版误列、实为已实现（已从待补项移除，仅作澄清记录）

下列条目在旧版被标为缺失，经核验后端均已存在，无需补充：

| 旧条目 | 已实现证据 |
|---|---|
| OrderList 取消/确认/退款/状态筛选 | `OrderController` `/orders/{id}/cancel`、`/confirm`、`/refund`、`GET /orders`(含 status) |
| Coupons 列表/领取/我的优惠券 | `CouponController` `/coupons`、`/coupons/{id}/claim`、`/my-coupons` |
| OrderConfirm 收货地址列表、优惠券列表 | `AddressController GET /addresses`、`CouponController` |
| OrderDetail 订单详情 | `OrderController GET /orders/{id}` → `OrderVO`（物流轨迹除外，见 B3） |
| ItemDetail 商品评价、商品规格 | `ReviewController GET /items/{itemId}/reviews`；`ItemDTO.spec` 内联 |
| Profile 用户信息更新、头像上传 | `UserController PUT /users/profile`；`FileController POST /upload/image` |
| AddressList 地址 CRUD + 默认地址 | `AddressController` 全套（增/删/改/查/`{id}/default`） |
| admin 商品上下架、分类级联 | `ItemController PUT /admin/items/{id}/status`；`CategoryController` |
| admin 订单筛选、发货、改状态 | `OrderController GET /admin/orders`(含 orderId/status)、`/ship`、`/status` |
| admin 公告 CRUD、发布状态 | `NotificationController /admin/notifications` 全套（status 字段控制上下线） |
| admin 反馈列表、回复 | `FeedbackController GET /admin/feedbacks`、`PUT .../reply`（回复接口固定将状态置为 1=已回复，**不接受**自定义 status；如需"已解决/已关闭"等多态状态机需后端扩展 reply 接口与状态字段） |
| 收藏夹、用户反馈提交 | `FavoriteController` 全套；`FeedbackController POST /feedbacks` |

---

## 优先级建议

1. **P0（前端现网会崩）**：A 节两处路径对齐。
2. **P1（前端已接线但 404）**：B1 `/admin/users` 两项、B2 `/admin/reviews`。
3. **P2（页面占位待接线）**：B3 运费/可用券/物流、B4 轮播图/秒杀/看板、B5 管理端个人中心等。
