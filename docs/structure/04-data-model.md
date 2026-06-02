# 数据模型 E-R 概览

源自 `docs/sql/init-all-tables.sql`，**单库 `hmall`**，18 张表，所有服务共享同一数据库。
表间**无物理外键约束**，全部靠 `*_id` 字段做**逻辑关联**（下图关系线为业务语义，非 DB 级 FK）。
金额字段（`price`/`total_fee`/`amount`/`balance`）均以**整数分**存储。

```mermaid
erDiagram
    user ||--o{ cart : "拥有"
    user ||--o{ order : "下单"
    user ||--o{ addresses : "收货地址"
    user ||--o{ user_favorites : "收藏"
    user ||--o{ user_coupons : "领券"
    user ||--o{ item_reviews : "评价"
    user ||--o{ feedbacks : "反馈"
    user ||--o{ customer_messages : "留言"

    item ||--o{ cart : "加入"
    item ||--o{ order_detail : "购买项"
    item ||--o{ item_reviews : "被评价"
    item ||--o{ user_favorites : "被收藏"

    order ||--o{ order_detail : "含明细"
    order ||--|| order_logistics : "物流"
    order ||--|| pay_order : "支付单(biz_order_no)"

    coupons ||--o{ user_coupons : "被领取"
    categories ||--o{ categories : "父子(parent_id)"

    user {
        bigint id PK
        varchar username
        varchar password
        varchar role "user/admin"
        int status "0冻结 1正常"
        int balance "余额(分)"
    }
    item {
        bigint id PK
        varchar name
        int price "分"
        int stock
        varchar category
        int status
    }
    cart {
        bigint id PK
        bigint user_id FK
        bigint item_id FK
        int num
    }
    order {
        bigint id PK
        bigint user_id FK
        int total_fee "分"
        int status "1未付 2已付..."
        datetime pay_time
    }
    order_detail {
        bigint id PK
        bigint order_id FK
        bigint item_id FK
        int price
        int num
    }
    order_logistics {
        bigint id PK
        bigint order_id FK
        varchar logistics_number
        varchar phone
    }
    pay_order {
        bigint id PK
        bigint biz_order_no "关联 order.id"
        bigint biz_user_id
        int amount
        int status "0待付 ..."
    }
    item_reviews {
        bigint id PK
        bigint user_id FK
        bigint item_id FK
        tinyint rating
    }
    user_favorites {
        bigint id PK
        bigint user_id FK
        bigint item_id FK
    }
    addresses {
        bigint id PK
        bigint user_id FK
        varchar receiver_name
        tinyint is_default
    }
    coupons {
        bigint id PK
        tinyint discount_type
        int discount_value
        int remaining_stock
    }
    user_coupons {
        bigint id PK
        bigint user_id FK
        bigint coupon_id FK
        bigint used_order_id
        tinyint status
    }
    feedbacks {
        bigint id PK
        bigint user_id FK
        text content
        text reply
    }
    customer_messages {
        bigint id PK
        bigint user_id FK
        text content
        text reply
    }
    categories {
        bigint id PK
        varchar name
        bigint parent_id "0=顶级"
        int sort_order
    }
```

## 独立表（无强关联，按业务读取）

| 表 | 归属服务 | 说明 |
| --- | --- | --- |
| `notifications` | notify-service | 站内公告，`/notifications/active` 取 `status=1` |
| `customer_messages` | notify-service | 客服留言（已含于上图，user_id 关联） |
| `feedbacks` | notify-service | 用户反馈（已含于上图） |
| `uploads` | file-service | 上传文件元数据（original_name / file_path / size） |
| `banners` | item/admin | 首页轮播图 |

> **关键关联说明**
> - `order` ↔ `pay_order` 通过 `pay_order.biz_order_no = order.id` 关联（一单一支付单）。
> - `user_coupons.used_order_id` 记录优惠券核销时所用订单。
> - `categories.parent_id = 0` 表示顶级分类，否则指向父分类（自引用树）。
