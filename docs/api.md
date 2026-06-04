# 前后端接口文档

> 自动生成于 2026-06-03，基于源码分析。

---

## 目录

- [1. 概览](#1-概览)
- [2. 认证机制](#2-认证机制)
- [3. Gateway 路由](#3-gateway-路由)
- [4. 用户服务 user-service](#4-用户服务-user-service)
- [5. 商品服务 item-service](#5-商品服务-item-service)
- [6. 购物车服务 cart-service](#6-购物车服务-cart-service)
- [7. 交易服务 trade-service](#7-交易服务-trade-service)
- [8. 支付服务 pay-service](#8-支付服务-pay-service)
- [9. 通知服务 notify-service](#9-通知服务-notify-service)
- [10. 文件服务 file-service](#10-文件服务-file-service)
- [11. 聚合服务 hm-service](#11-聚合服务-hm-service)
- [12. 前端 API 调用清单](#12-前端-api-调用清单)
- [13. 通用数据类型](#13-通用数据类型)

---

## 1. 概览

本项目采用 Spring Cloud 微服务架构，通过 `hm-gateway` 统一路由。所有前端请求经 `/api` 前缀代理到 Gateway，再分发至各微服务。

| 服务 | 端口 | 职责 | 端点数 |
|------|------|------|--------|
| user-service | 8082 | 用户、地址、收藏 | 16 |
| item-service | 8081 | 商品、分类、搜索、评价 | 16 |
| cart-service | 8083 | 购物车 | 5 |
| trade-service | 8084 | 订单、优惠券 | 16 |
| pay-service | 8085 | 支付 | 2 |
| notify-service | 8086 | 通知、留言、反馈 | 11 |
| file-service | 8087 | 文件上传/下载 | 3 |
| hm-service | 8080 | 聚合演示 | 24 |
| **合计** | | | **93** |

---

## 2. 认证机制

### 2.1 JWT Token

- 登录成功后返回 `token`，前端存储于 `localStorage`
- 请求时通过 `authorization` 请求头传递
- Gateway 解析 JWT，将 `userId` 和 `role` 写入下游 header

### 2.2 认证级别

| 级别 | 说明 | 适用路径 |
|------|------|----------|
| **完全公开** | 无需 Token | `/search/**`, `/users/login`, `/users/register`, `/users/send-code`, `/users/reset-password`, `/hi`, `/notifications/active`, `/upload/**`, `/files/**` |
| **读公开** | GET/HEAD/OPTIONS 免认证，写操作需认证 | `/categories/**`, `/items/**` |
| **需认证** | 必须携带有效 JWT | 其他所有路径 |
| **需管理员** | 需认证 + `role=admin` | `/admin/**` |

---

## 3. Gateway 路由

| 路由 ID | 目标服务 | 路径模式 |
|---------|---------|----------|
| item-service | `lb://item-service` | `/items/**`, `/search/**`, `/categories/**`, `/admin/items/**`, `/admin/reviews/**` |
| user-service | `lb://user-service` | `/users/**`, `/addresses/**`, `/favorites/**` |
| cart-service | `lb://cart-service` | `/carts/**` |
| trade-service | `lb://trade-service` | `/orders/**`, `/coupons/**`, `/my-coupons/**`, `/admin/coupons/**`, `/admin/orders/**` |
| pay-service | `lb://pay-service` | `/pay-orders/**` |
| notify-service | `lb://notify-service` | `/notifications/**`, `/messages/**`, `/admin/notifications/**`, `/admin/messages/**`, `/feedbacks/**`, `/my-feedbacks/**`, `/admin/feedbacks/**` |
| file-service | `lb://file-service` | `/upload/**`, `/files/**` |
| hm-service | `lb://hm-service` | `/hi` |

---

## 4. 用户服务 user-service

### 4.1 UserController (`/users`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/users/login` | 用户登录 | 公开 |
| POST | `/users/register` | 用户注册 | 公开 |
| POST | `/users/send-code` | 发送邮箱验证码 | 公开 |
| POST | `/users/reset-password` | 重置密码 | 公开 |
| PUT | `/users/profile` | 修改个人信息 | 需认证 |
| PUT | `/users/money/deduct` | 扣减账户余额 | 需认证 |
| GET | `/users/{id}` | 根据 ID 查询用户 | 需认证 |

#### POST `/users/login`

**请求体** `LoginFormDTO`：
```json
{
  "username": "string - 用户名",
  "password": "string - 密码",
  "rememberMe": "boolean - 是否记住我 (可选)"
}
```

**响应** `UserLoginVO`：
```json
{
  "token": "string - JWT 令牌",
  "userId": "long - 用户 ID",
  "username": "string - 用户名",
  "balance": "integer - 账户余额(分)"
}
```

#### POST `/users/register`

**请求体** `RegisterFormDTO`：
```json
{
  "username": "string - 用户名",
  "password": "string - 密码",
  "email": "string - 邮箱",
  "code": "string - 验证码"
}
```

**响应**：`R<Void>`

#### POST `/users/send-code`

**查询参数**：`email` (string) - 目标邮箱

**响应**：`R<Void>`

#### POST `/users/reset-password`

**请求体** `ResetPasswordDTO`：
```json
{
  "email": "string - 邮箱",
  "code": "string - 验证码",
  "newPassword": "string - 新密码"
}
```

**响应**：`R<Void>`

#### PUT `/users/profile`

**请求体** `User`：
```json
{
  "nickname": "string - 昵称",
  "email": "string - 邮箱",
  "avatar": "string - 头像 URL",
  "phone": "string - 手机号"
}
```

**响应**：`R<Void>`

#### PUT `/users/money/deduct`

**查询参数**：
- `pw` (string) - 支付密码
- `amount` (integer) - 扣减金额(分)

**响应**：`void`

#### GET `/users/{id}`

**路径参数**：`id` (long) - 用户 ID

**响应** `User`：
```json
{
  "id": "long",
  "username": "string",
  "phone": "string",
  "email": "string",
  "avatar": "string",
  "nickname": "string",
  "balance": "integer - 余额(分)"
}
```

---

### 4.2 AddressController (`/addresses`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/addresses` | 查询当前用户地址列表 | 需认证 |
| POST | `/addresses` | 新增收货地址 | 需认证 |
| PUT | `/addresses/{id}` | 修改收货地址 | 需认证 |
| DELETE | `/addresses/{id}` | 删除收货地址 | 需认证 |
| PUT | `/addresses/{id}/default` | 设为默认地址 | 需认证 |

#### GET `/addresses`

**响应** `List<Address>`：
```json
[
  {
    "id": "long",
    "userId": "long",
    "receiverName": "string - 收件人",
    "phone": "string - 联系电话",
    "province": "string - 省",
    "city": "string - 市",
    "district": "string - 区/县",
    "detail": "string - 详细地址",
    "isDefault": "integer - 是否默认(1是/0否)",
    "createTime": "datetime",
    "updateTime": "datetime"
  }
]
```

#### POST `/addresses`

**请求体** `Address`：
```json
{
  "receiverName": "string - 收件人",
  "phone": "string - 联系电话",
  "province": "string - 省",
  "city": "string - 市",
  "district": "string - 区/县",
  "detail": "string - 详细地址",
  "isDefault": "integer - 是否默认(1是/0否)"
}
```

**响应**：`R<Void>`

#### PUT `/addresses/{id}`

**路径参数**：`id` (long) - 地址 ID

**请求体**：同 POST `/addresses`

**响应**：`R<Void>`

#### DELETE `/addresses/{id}`

**路径参数**：`id` (long) - 地址 ID

**响应**：`R<Void>`

#### PUT `/addresses/{id}/default`

**路径参数**：`id` (long) - 地址 ID

**响应**：`R<Void>`

---

### 4.3 FavoriteController (`/favorites`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/favorites` | 查询收藏列表 | 需认证 |
| POST | `/favorites` | 添加收藏 | 需认证 |
| DELETE | `/favorites/{itemId}` | 取消收藏 | 需认证 |
| GET | `/favorites/check/{itemId}` | 检查是否已收藏 | 需认证 |

#### GET `/favorites`

**响应** `List<UserFavorite>`：
```json
[
  {
    "id": "long",
    "userId": "long",
    "itemId": "long - 商品 ID",
    "createTime": "datetime"
  }
]
```

#### POST `/favorites`

**查询参数**：`itemId` (long) - 商品 ID

**响应**：`R<Void>`

#### DELETE `/favorites/{itemId}`

**路径参数**：`itemId` (long) - 商品 ID

**响应**：`R<Void>`

#### GET `/favorites/check/{itemId}`

**路径参数**：`itemId` (long) - 商品 ID

**响应**：`boolean`

---

## 5. 商品服务 item-service

### 5.1 ItemController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/items/page` | 分页查询商品 | 读公开 |
| GET | `/items` | 批量查询商品 | 读公开 |
| GET | `/items/{id}` | 查询单个商品 | 读公开 |
| POST | `/items` | 新增商品 | 需认证 |
| PUT | `/items/stock/deduct` | 批量扣减库存 | 需认证 |
| GET | `/admin/items` | 管理端分页查询 | 需管理员 |
| PUT | `/admin/items/{id}` | 更新商品 | 需管理员 |
| PUT | `/admin/items/{id}/status` | 更新商品状态 | 需管理员 |
| DELETE | `/admin/items/{id}` | 删除商品 | 需管理员 |

#### GET `/items/page`

**查询参数** `PageQuery`：
- `pageNo` (integer, 默认 1) - 页码
- `pageSize` (integer, 默认 20) - 每页数量
- `isAsc` (boolean, 默认 true) - 是否升序
- `sortBy` (string) - 排序字段

**响应** `PageDTO<ItemDTO>`

#### GET `/items`

**查询参数**：`ids` (List<Long>) - 商品 ID 列表

**响应** `List<ItemDTO>`

#### GET `/items/{id}`

**路径参数**：`id` (long) - 商品 ID

**响应** `ItemDTO`：
```json
{
  "id": "long",
  "name": "string - SKU 名称",
  "price": "integer - 价格(分)",
  "stock": "integer - 库存",
  "image": "string - 图片 URL",
  "category": "string - 类目",
  "brand": "string - 品牌",
  "spec": "string - 规格 JSON",
  "sold": "integer - 销量",
  "commentCount": "integer - 评论数",
  "isAD": "boolean - 是否推广",
  "status": "integer - 状态(1正常/2下架/3删除)"
}
```

#### POST `/items`

**请求体** `ItemDTO`：同上 ItemDTO 字段

**响应**：`R<Void>`

#### PUT `/items/stock/deduct`

**请求体** `List<OrderDetailDTO>`：
```json
[
  {
    "itemId": "long - 商品 ID",
    "num": "integer - 扣减数量"
  }
]
```

**响应**：`void`

#### GET `/admin/items`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页数量
- `name` (string, 可选) - 商品名称模糊查询
- `category` (string, 可选) - 分类筛选
- `status` (integer, 可选) - 状态筛选

**响应** `PageDTO<ItemDTO>`

#### PUT `/admin/items/{id}`

**路径参数**：`id` (long)

**请求体** `ItemDTO`：更新数据

**响应**：`R<Void>`

#### PUT `/admin/items/{id}/status`

**路径参数**：`id` (long)

**查询参数**：`status` (integer) - 目标状态

**响应**：`R<Void>`

#### DELETE `/admin/items/{id}`

**路径参数**：`id` (long)

**响应**：`R<Void>`

---

### 5.2 SearchController (`/search`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/search/list` | 搜索商品 | 公开 |

#### GET `/search/list`

**查询参数** `ItemPageQuery`（继承 PageQuery）：
- `key` (string) - 搜索关键字
- `category` (string) - 分类
- `brand` (string) - 品牌
- `minPrice` (integer) - 最低价格
- `maxPrice` (integer) - 最高价格
- `pageNo` (integer, 默认 1) - 页码
- `pageSize` (integer, 默认 20) - 每页数量

**响应** `PageDTO<ItemDTO>`（仅返回上架商品）

---

### 5.3 CategoryController (`/categories`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/categories` | 查询分类列表 | 读公开 |
| POST | `/categories` | 新增分类 | 需认证 |
| PUT | `/categories/{id}` | 更新分类 | 需认证 |
| DELETE | `/categories/{id}` | 删除分类 | 需认证 |

#### GET `/categories`

**响应** `List<Category>`：
```json
[
  {
    "id": "long",
    "name": "string - 分类名称",
    "parentId": "long - 父分类 ID",
    "sortOrder": "integer - 排序",
    "status": "integer - 状态"
  }
]
```

#### POST `/categories`

**请求体** `Category`：同上字段

**响应**：`R<Void>`

#### PUT `/categories/{id}`

**路径参数**：`id` (long)

**请求体** `Category`

**响应**：`R<Void>`

#### DELETE `/categories/{id}`

**路径参数**：`id` (long)

**响应**：`R<Void>`

---

### 5.4 ReviewController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/items/{itemId}/reviews` | 查询商品评价 | 读公开 |
| POST | `/items/{itemId}/reviews` | 提交评价 | 需认证 |
| DELETE | `/admin/reviews/{id}` | 删除评价 | 需管理员 |

#### GET `/items/{itemId}/reviews`

**路径参数**：`itemId` (long)

**响应** `List<ItemReview>`

#### POST `/items/{itemId}/reviews`

**路径参数**：`itemId` (long)

**请求体** `ReviewDTO`：
```json
{
  "content": "string - 评价内容",
  "images": "string - 评价图片 JSON 数组",
  "rating": "integer - 评分(1-5)"
}
```

**响应**：`R<Void>`

#### DELETE `/admin/reviews/{id}`

**路径参数**：`id` (long)

**响应**：`R<Void>`

---

## 6. 购物车服务 cart-service

### 6.1 CartController (`/carts`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/carts` | 查询购物车列表 | 需认证 |
| POST | `/carts` | 添加商品到购物车 | 需认证 |
| PUT | `/carts` | 更新购物车 | 需认证 |
| DELETE | `/carts/{id}` | 删除购物车项 | 需认证 |
| DELETE | `/carts` | 批量删除 | 需认证 |

#### GET `/carts`

**响应** `List<CartVO>`：
```json
[
  {
    "id": "long - 购物车条目 ID",
    "itemId": "long - 商品 ID",
    "num": "integer - 数量",
    "name": "string - 商品标题",
    "spec": "string - 规格",
    "price": "integer - 价格(分)",
    "newPrice": "integer - 最新价格(分)",
    "status": "integer - 商品状态",
    "stock": "integer - 库存",
    "image": "string - 图片",
    "createTime": "datetime"
  }
]
```

#### POST `/carts`

**请求体** `CartFormDTO`：
```json
{
  "itemId": "long - 商品 ID",
  "name": "string - 商品标题",
  "spec": "string - 规格",
  "price": "integer - 价格(分)",
  "image": "string - 图片"
}
```

**响应**：`void`

#### PUT `/carts`

**请求体** `Cart`：
```json
{
  "id": "long - 购物车条目 ID",
  "num": "integer - 数量"
}
```

**响应**：`void`

#### DELETE `/carts/{id}`

**路径参数**：`id` (long)

**响应**：`void`

#### DELETE `/carts`

**查询参数**：`ids` (List<Long>) - 购物车条目 ID 列表

**响应**：`void`

---

## 7. 交易服务 trade-service

### 7.1 OrderController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/orders` | 查询我的订单 | 需认证 |
| GET | `/orders/{id}` | 查询订单详情 | 需认证 |
| POST | `/orders` | 创建订单 | 需认证 |
| PUT | `/orders` | 更新订单 | 需认证 |
| PUT | `/orders/{id}/cancel` | 取消订单 | 需认证 |
| PUT | `/orders/{id}/confirm` | 确认收货 | 需认证 |
| POST | `/orders/{id}/refund` | 申请退款 | 需认证 |
| PUT | `/orders/{orderId}` | 标记已支付 | 需认证 |
| GET | `/admin/orders` | 管理端查询订单 | 需管理员 |
| PUT | `/admin/orders/{id}/status` | 修改订单状态 | 需管理员 |
| PUT | `/admin/orders/{id}/ship` | 发货 | 需管理员 |

#### GET `/orders`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数
- `status` (integer, 可选) - 订单状态筛选

**订单状态枚举**：
| 值 | 含义 |
|----|------|
| 1 | 未付款 |
| 2 | 已付款未发货 |
| 3 | 已发货未确认 |
| 4 | 确认收货 |
| 5 | 交易取消 |
| 6 | 已评价 |

**响应** `PageDTO<OrderVO>`

#### GET `/orders/{id}`

**路径参数**：`id` (long)

**响应** `OrderVO`

#### POST `/orders`

**请求体** `OrderFormDTO`：
```json
{
  "addressId": "long - 收货地址 ID",
  "paymentType": "integer - 支付类型",
  "details": [
    {
      "itemId": "long - 商品 ID",
      "num": "integer - 数量"
    }
  ]
}
```

**响应**：`Long`（订单 ID）

#### PUT `/orders/{id}/cancel`

**路径参数**：`id` (long)

**响应**：`R<Void>`

#### PUT `/orders/{id}/confirm`

**路径参数**：`id` (long)

**响应**：`R<Void>`

#### POST `/orders/{id}/refund`

**路径参数**：`id` (long)

**响应**：`R<Void>`

#### GET `/admin/orders`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数
- `orderId` (long, 可选) - 按订单 ID 筛选
- `status` (integer, 可选) - 按状态筛选

**响应** `PageDTO<OrderVO>`

#### PUT `/admin/orders/{id}/status`

**路径参数**：`id` (long)

**查询参数**：`status` (integer) - 新状态值

**响应**：`R<Void>`

#### PUT `/admin/orders/{id}/ship`

**路径参数**：`id` (long)

**查询参数**：`trackingNumber` (string) - 物流单号

**响应**：`R<Void>`

---

### 7.2 CouponController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/coupons` | 查询可领取优惠券 | 需认证 |
| POST | `/coupons/{id}/claim` | 领取优惠券 | 需认证 |
| GET | `/my-coupons` | 查询我的优惠券 | 需认证 |
| POST | `/admin/coupons` | 创建优惠券 | 需管理员 |
| DELETE | `/admin/coupons/{id}` | 删除优惠券 | 需管理员 |

#### GET `/coupons`

**响应** `List<Coupon>`

#### POST `/coupons/{id}/claim`

**路径参数**：`id` (long) - 优惠券 ID

**响应**：`R<Void>`

#### GET `/my-coupons`

**响应** `List<Coupon>`

#### POST `/admin/coupons`

**请求体** `Coupon`：
```json
{
  "name": "string - 优惠券名称",
  "description": "string - 描述",
  "discountType": "integer - 折扣类型",
  "discountValue": "integer - 折扣值",
  "minAmount": "integer - 最低消费(分)",
  "totalStock": "integer - 总库存",
  "startTime": "datetime - 开始时间",
  "endTime": "datetime - 结束时间",
  "status": "integer - 状态"
}
```

**响应**：`R<Void>`

#### DELETE `/admin/coupons/{id}`

**路径参数**：`id` (long)

**响应**：`R<Void>`

---

## 8. 支付服务 pay-service

### 8.1 PayController (`/pay-orders`)

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/pay-orders` | 生成支付单 | 需认证 |
| POST | `/pay-orders/{id}` | 余额支付 | 需认证 |

#### POST `/pay-orders`

**请求体** `PayApplyDTO`

**响应**：`String`（支付单号）

#### POST `/pay-orders/{id}`

**路径参数**：`id` (long) - 支付单 ID

**请求体** `PayOrderFormDTO`

**响应**：`void`

---

## 9. 通知服务 notify-service

### 9.1 CustomerMessageController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/messages` | 发送留言 | 需认证 |
| GET | `/admin/messages` | 管理端查询留言 | 需管理员 |
| PUT | `/admin/messages/{id}/reply` | 回复留言 | 需管理员 |

#### POST `/messages`

**请求体** `CustomerMessage`

**响应**：`R<Void>`

#### GET `/admin/messages`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数

**响应** `PageDTO<CustomerMessage>`

#### PUT `/admin/messages/{id}/reply`

**路径参数**：`id` (long)

**请求体** `CustomerMessage`

**响应**：`R<Void>`

---

### 9.2 FeedbackController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/feedbacks` | 提交反馈 | 需认证 |
| GET | `/my-feedbacks` | 查询我的反馈 | 需认证 |
| GET | `/admin/feedbacks` | 管理端查询反馈 | 需管理员 |
| PUT | `/admin/feedbacks/{id}/reply` | 回复反馈 | 需管理员 |

#### POST `/feedbacks`

**请求体** `Feedback`

**响应**：`R<Void>`

#### GET `/my-feedbacks`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数

**响应** `PageDTO<Feedback>`

#### GET `/admin/feedbacks`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数

**响应** `PageDTO<Feedback>`

#### PUT `/admin/feedbacks/{id}/reply`

**路径参数**：`id` (long)

**请求体** `Feedback`

**响应**：`R<Void>`

---

### 9.3 NotificationController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/notifications/active` | 获取有效通知 | 公开 |
| GET | `/admin/notifications` | 管理端查询通知 | 需管理员 |
| POST | `/admin/notifications` | 创建通知 | 需管理员 |
| PUT | `/admin/notifications/{id}` | 更新通知 | 需管理员 |
| DELETE | `/admin/notifications/{id}` | 删除通知 | 需管理员 |

#### GET `/notifications/active`

**响应** `List<Notification>`

#### GET `/admin/notifications`

**查询参数**：
- `page` (integer, 默认 1) - 页码
- `size` (integer, 默认 10) - 每页条数

**响应** `PageDTO<Notification>`

#### POST `/admin/notifications`

**请求体** `Notification`

**响应**：`R<Void>`

#### PUT `/admin/notifications/{id}`

**路径参数**：`id` (long)

**请求体** `Notification`

**响应**：`R<Void>`

#### DELETE `/admin/notifications/{id}`

**路径参数**：`id` (long)

**响应**：`R<Void>`

---

## 10. 文件服务 file-service

### 10.1 FileController

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/upload/image` | 上传图片 | 公开 |
| GET | `/files/{id}` | 按 ID 下载文件 | 公开 |
| GET | `/files/**` | 按路径下载文件 | 公开 |

#### POST `/upload/image`

**请求**：`multipart/form-data`，字段 `file` (MultipartFile)

**响应** `Map<String, String>`：
```json
{
  "id": "string - 文件 ID",
  "url": "string - 文件访问 URL"
}
```

#### GET `/files/{id}`

**路径参数**：`id` (Long) - 文件 ID

**响应**：文件流（代理到 MinIO/本地存储）

#### GET `/files/**`

**路径**：文件 key（如 `/files/2026/06/03/uuid.jpg`）

**响应**：文件流

---

## 11. 聚合服务 hm-service

> hm-service 是早期聚合演示服务，大部分接口已被独立微服务替代。保留用于向后兼容。

| 方法 | 路径 | 描述 | 对应微服务 |
|------|------|------|-----------|
| GET | `/hi` | 健康检查 | - |
| POST | `/users/login` | 用户登录 | user-service |
| PUT | `/users/money/deduct` | 扣减余额 | user-service |
| GET | `/addresses` | 查询地址列表 | user-service |
| GET | `/addresses/{id}` | 查询地址详情 | user-service |
| GET | `/items/page` | 分页查询商品 | item-service |
| GET | `/items` | 批量查询商品 | item-service |
| GET | `/items/{id}` | 查询商品详情 | item-service |
| POST | `/items` | 新增商品 | item-service |
| PUT | `/items` | 更新商品 | item-service |
| PUT | `/items/status/{id}/{status}` | 更新商品状态 | item-service |
| DELETE | `/items/{id}` | 删除商品 | item-service |
| PUT | `/items/stock/deduct` | 扣减库存 | item-service |
| GET | `/search/list` | 搜索商品 | item-service |
| GET | `/carts` | 查询购物车 | cart-service |
| POST | `/carts` | 添加购物车 | cart-service |
| PUT | `/carts` | 更新购物车 | cart-service |
| DELETE | `/carts/{id}` | 删除购物车项 | cart-service |
| DELETE | `/carts?ids=` | 批量删除购物车 | cart-service |
| GET | `/orders/{id}` | 查询订单 | trade-service |
| POST | `/orders` | 创建订单 | trade-service |
| PUT | `/orders/{orderId}` | 标记已支付 | trade-service |
| POST | `/pay-orders` | 生成支付单 | pay-service |
| POST | `/pay-orders/{id}` | 余额支付 | pay-service |

---

## 12. 前端 API 调用清单

### 12.1 hmall-web（用户端）

共 **34** 个 API 函数，覆盖 **13** 个页面。

| 模块 | 函数 | 方法 | 路径 | 调用页面 |
|------|------|------|------|----------|
| 用户 | `login` | POST | `/users/login` | Login.vue |
| 用户 | `register` | POST | `/users/register` | Login.vue |
| 用户 | `sendCode` | POST | `/users/send-code` | Login.vue |
| 用户 | `resetPassword` | POST | `/users/reset-password` | Login.vue |
| 用户 | `updateProfile` | PUT | `/users/profile` | Profile.vue |
| 商品 | `getItems` | GET | `/items/page` | Home.vue |
| 商品 | `getItemById` | GET | `/items/{id}` | ItemDetail.vue |
| 商品 | `searchItems` | GET | `/items/search` | Search.vue |
| 商品 | `getReviews` | GET | `/items/{itemId}/reviews` | ItemDetail.vue |
| 商品 | `submitReview` | POST | `/items/{itemId}/reviews` | ItemDetail.vue |
| 商品 | `getCategories` | GET | `/categories` | Category.vue |
| 购物车 | `getCart` | GET | `/carts` | Cart.vue |
| 购物车 | `addToCart` | POST | `/carts` | ItemDetail.vue |
| 购物车 | `updateCartItem` | PUT | `/carts/{id}` | Cart.vue |
| 购物车 | `deleteCartItem` | DELETE | `/carts/{id}` | Cart.vue |
| 订单 | `createOrder` | POST | `/orders` | OrderConfirm.vue |
| 订单 | `getOrders` | GET | `/orders` | OrderList.vue |
| 订单 | `cancelOrder` | PUT | `/orders/{id}/cancel` | OrderList.vue |
| 订单 | `confirmOrder` | PUT | `/orders/{id}/confirm` | OrderList.vue |
| 订单 | `refundOrder` | POST | `/orders/{id}/refund` | OrderList.vue |
| 订单 | `getCoupons` | GET | `/coupons` | ⚠️ 未使用 |
| 订单 | `claimCoupon` | POST | `/coupons/{id}/claim` | ⚠️ 未使用 |
| 订单 | `getMyCoupons` | GET | `/my-coupons` | ⚠️ 未使用 |
| 地址 | `getAddresses` | GET | `/addresses` | AddressList.vue |
| 地址 | `saveAddress` | POST | `/addresses` | AddressList.vue |
| 地址 | `updateAddress` | PUT | `/addresses/{id}` | ⚠️ 未使用 |
| 地址 | `deleteAddress` | DELETE | `/addresses/{id}` | AddressList.vue |
| 地址 | `setDefaultAddress` | PUT | `/addresses/{id}/default` | AddressList.vue |
| 公共 | `submitFeedback` | POST | `/feedbacks` | Feedback.vue |
| 公共 | `getNotifications` | GET | `/notifications/active` | Notifications.vue |
| 公共 | `addFavorite` | POST | `/favorites` | ItemDetail.vue |
| 公共 | `removeFavorite` | DELETE | `/favorites/{itemId}` | ItemDetail.vue |
| 公共 | `getFavorites` | GET | `/favorites` | FavoriteList.vue |
| 公共 | `checkFavorite` | GET | `/favorites/check/{itemId}` | ItemDetail.vue |

### 12.2 hmall-admin（管理端）

共 **23** 个 API 函数，覆盖 **13** 个页面。

| 模块 | 函数 | 方法 | 路径 | 调用页面 |
|------|------|------|------|----------|
| 认证 | `login` | POST | `/users/login` | Login.vue |
| 商品 | `getItems` | GET | `/admin/items` | ItemList.vue |
| 商品 | `saveItem` | POST | `/items` | ItemEdit.vue |
| 商品 | `updateItem` | PUT | `/admin/items/{id}` | ItemEdit.vue |
| 商品 | `updateItemStatus` | PUT | `/admin/items/{id}/status` | ItemList.vue |
| 商品 | `deleteItem` | DELETE | `/admin/items/{id}` | ItemList.vue |
| 商品 | `getCategories` | GET | `/categories` | CategoryList.vue |
| 商品 | `saveCategory` | POST | `/categories` | CategoryList.vue |
| 商品 | `updateCategory` | PUT | `/categories/{id}` | CategoryList.vue |
| 商品 | `deleteCategory` | DELETE | `/categories/{id}` | CategoryList.vue |
| 商品 | `getReviews` | GET | `/admin/reviews` | ReviewList.vue |
| 商品 | `deleteReview` | DELETE | `/admin/reviews/{id}` | ReviewList.vue |
| 订单 | `getOrders` | GET | `/admin/orders` | OrderList.vue |
| 订单 | `shipOrder` | PUT | `/admin/orders/{id}/ship` | OrderList.vue |
| 订单 | `updateOrderStatus` | PUT | `/admin/orders/{id}/status` | OrderList.vue |
| 用户 | `getUsers` | GET | `/admin/users` | UserList.vue |
| 用户 | `toggleUserStatus` | PUT | `/admin/users/{id}/status` | UserList.vue |
| 系统 | `getNotifications` | GET | `/admin/notifications` | NotificationList.vue |
| 系统 | `saveNotification` | POST | `/admin/notifications` | NotificationList.vue |
| 系统 | `updateNotification` | PUT | `/admin/notifications/{id}` | NotificationList.vue |
| 系统 | `deleteNotification` | DELETE | `/admin/notifications/{id}` | NotificationList.vue |
| 系统 | `getFeedbacks` | GET | `/admin/feedbacks` | FeedbackList.vue |
| 系统 | `replyFeedback` | PUT | `/admin/feedbacks/{id}/reply` | FeedbackList.vue |

### 12.3 前端发现

| 发现 | 说明 |
|------|------|
| ⚠️ 未使用的 API | `getCoupons`、`claimCoupon`、`getMyCoupons` 已定义但未被页面调用 |
| ⚠️ 未使用的 API | `updateAddress` 已定义但页面未实现编辑功能 |
| ⚠️ 纯前端页面 | `BannerList.vue`（轮播图）无 API 调用，仅本地 state |
| ⚠️ 纯前端页面 | `Dashboard.vue`（数据看板）使用硬编码假数据 |
| ⚠️ 纯前端页面 | `Profile.vue`（admin）密码修改无实际 API 调用 |

---

## 13. 通用数据类型

### 13.1 响应封装 `R<T>`

```json
{
  "code": "integer - 状态码(200成功)",
  "msg": "string - 提示信息",
  "data": "T - 业务数据"
}
```

### 13.2 分页封装 `PageDTO<T>`

```json
{
  "total": "long - 总记录数",
  "list": "List<T> - 当前页数据"
}
```

### 13.3 金额单位

所有金额字段（`price`、`newPrice`、`balance`、`totalFee`、`discountValue`、`minAmount` 等）单位均为 **分（integer）**。

### 13.4 时间格式

时间字段使用 `LocalDateTime`，JSON 序列化格式为 ISO 8601（如 `2026-06-03T12:00:00`）。
