# hmall 电商平台重构设计文档

**日期**：2026-05-18
**目标**：重构当前 hmall 项目，使其满足《Web开发技术》实验指导书全部要求（基本要求 + 进阶要求 + 拓展要求），达到满分标准。

---

## 1. 整体架构

```
hmall/
├── hm-gateway          # API 网关 (8080, Spring Cloud Gateway)
├── hm-service          # 单体遗留 → @Deprecated，保留为参考
├── hm-api              # Feign 客户端接口定义（共享模块）
├── hm-common           # 公共模块：工具类、异常、拦截器、Redis 配置
├── user-service        # 用户 + 收货地址 + 收藏
├── item-service        # 商品 + 评价 + 搜索 + 商品分类
├── cart-service        # 购物车
├── trade-service       # 订单 + 物流 + 优惠券 + 退款
├── pay-service         # 支付处理
├── notify-service      # ★新增：系统公告、用户反馈、客服留言
├── file-service        # ★新增：文件/图片上传
├── hmall-web/          # ★新增：Vue 3 客户端前端
└── hmall-admin/        # ★新增：Vue 3 管理后台前端
```

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.12 + Spring Cloud 2021.0.3 |
| 注册/配置中心 | Nacos |
| 远程调用 | OpenFeign + LoadBalancer |
| ORM | MyBatis-Plus 3.4.3 |
| 缓存 | Redis 7.0 + Spring Cache + Caffeine |
| 消息队列 | RabbitMQ |
| API 文档 | Knife4j (Swagger) |
| 前端框架 | Vue 3 + Vite + Element Plus + Pinia + Vue Router |
| HTTP 客户端 | Axios |
| 容器化 | Docker + Docker Compose |

### 服务职责

| 服务 | 端口 | 原有职责 | 新增职责 |
|------|------|---------|---------|
| `hm-gateway` | 8080 | JWT 鉴权、路由转发 | 角色区分 (user/admin)，`/admin/**` 路由鉴权 |
| `user-service` | 8081 | 用户注册/登录 | 收货地址 CRUD、商品收藏、邮箱验证码、找回密码、个人信息修改 |
| `item-service` | 8082 | 商品 CRUD、搜索 | 商品评价（含图片）、商品分类管理 |
| `cart-service` | 8083 | 购物车 CRUD | 不变 |
| `trade-service` | 8084 | 订单、物流 | 优惠券管理 + 用户领券、退款处理、管理员订单操作 |
| `pay-service` | 8085 | 模拟支付 | 不变 |
| `notify-service` | 8086 | — | 系统公告 CRUD、用户反馈、客服留言 |
| `file-service` | 8087 | — | 图片上传/下载、Excel 导出 |

---

## 2. 数据库设计（新增/扩展表）

### 商品评价 item_reviews
```sql
CREATE TABLE item_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    content TEXT,
    images VARCHAR(1024),          -- JSON 数组，评价图片 URL
    rating TINYINT NOT NULL DEFAULT 5, -- 1-5 星
    create_time DATETIME NOT NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_user_id (user_id)
);
```

### 商品收藏 user_favorites
```sql
CREATE TABLE user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_item (user_id, item_id)
);
```

### 收货地址 addresses（从 hm-service 迁移到 user-service）
```sql
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    detail VARCHAR(255) NOT NULL,
    is_default TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_user_id (user_id)
);
```

### 优惠券 coupons
```sql
CREATE TABLE coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    discount_type TINYINT NOT NULL,   -- 1-满减, 2-折扣
    discount_value INT NOT NULL,      -- 减免金额(分) 或 折扣百分比
    min_amount INT DEFAULT 0,         -- 最低消费金额(分)
    total_stock INT NOT NULL,
    remaining_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,         -- 1-有效, 0-失效
    create_time DATETIME
);
```

### 用户优惠券 user_coupons
```sql
CREATE TABLE user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,         -- 1-未使用, 2-已使用, 3-已过期
    used_order_id BIGINT,
    create_time DATETIME,
    use_time DATETIME,
    INDEX idx_user_id (user_id)
);
```

### 系统公告 notifications
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,         -- 1-发布, 0-下架
    create_time DATETIME,
    update_time DATETIME
);
```

### 用户反馈 feedbacks
```sql
CREATE TABLE feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,         -- 0-待处理, 1-已回复, 2-已关闭
    create_time DATETIME,
    reply_time DATETIME
);
```

### 客服留言 customer_messages
```sql
CREATE TABLE customer_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);
```

### 文件记录 uploads
```sql
CREATE TABLE uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(64),
    file_type VARCHAR(32),
    create_time DATETIME
);
```

---

## 3. 后端 API 设计

### 3.1 user-service 扩展

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/users/register` | 用户注册（含邮箱验证码） |
| POST | `/users/send-code` | 发送邮箱验证码 |
| POST | `/users/login` | 登录（已有） |
| POST | `/users/reset-password` | 找回密码 |
| PUT | `/users/profile` | 修改个人信息 |
| GET | `/users/{id}` | 获取用户信息 |
| GET | `/addresses` | 地址列表 |
| POST | `/addresses` | 新增地址 |
| PUT | `/addresses/{id}` | 修改地址 |
| DELETE | `/addresses/{id}` | 删除地址 |
| PUT | `/addresses/{id}/default` | 设为默认地址 |
| POST | `/favorites` | 收藏商品 |
| DELETE | `/favorites/{itemId}` | 取消收藏 |
| GET | `/favorites` | 收藏列表 |
| GET | `/favorites/check/{itemId}` | 查询是否已收藏 |

### 3.2 item-service 扩展

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/items` | 商品分页列表（已有，扩展筛选） |
| GET | `/items/{id}` | 商品详情（已有） |
| POST | `/items` | 新增商品 |
| PUT | `/items/{id}` | 编辑商品 |
| PUT | `/items/{id}/status` | 上架/下架 |
| DELETE | `/items/{id}` | 删除商品（逻辑删除） |
| GET | `/items/search` | 商品搜索（已有，扩展排序） |
| GET | `/categories` | 分类列表 |
| POST | `/categories` | 新增分类 |
| PUT | `/categories/{id}` | 修改分类 |
| DELETE | `/categories/{id}` | 删除分类 |
| GET | `/items/{id}/reviews` | 查看商品评价 |
| POST | `/items/{id}/reviews` | 提交评价（含图片） |
| DELETE | `/admin/reviews/{id}` | 删除违规评价 |

### 3.3 trade-service 扩展

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/orders` | 创建订单（已有） |
| GET | `/orders` | 我的订单（已有） |
| PUT | `/orders/{id}/cancel` | 取消订单 |
| PUT | `/orders/{id}/confirm` | 确认收货 |
| POST | `/orders/{id}/refund` | 申请退款 |
| GET | `/admin/orders` | 管理端订单列表 |
| PUT | `/admin/orders/{id}/status` | 修改订单状态 |
| PUT | `/admin/orders/{id}/ship` | 发货（填写物流单号） |
| POST | `/admin/orders/export` | 导出订单 Excel |
| GET | `/coupons` | 可用优惠券列表 |
| POST | `/admin/coupons` | 新增优惠券 |
| DELETE | `/admin/coupons/{id}` | 删除优惠券 |
| POST | `/coupons/{id}/claim` | 领取优惠券 |
| GET | `/my-coupons` | 我的优惠券 |

### 3.4 notify-service（新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/notifications/active` | 客户端获取有效公告 |
| GET | `/admin/notifications` | 管理端公告列表 |
| POST | `/admin/notifications` | 发布公告 |
| PUT | `/admin/notifications/{id}` | 编辑公告 |
| DELETE | `/admin/notifications/{id}` | 删除公告 |
| POST | `/feedbacks` | 用户提交反馈 |
| GET | `/my-feedbacks` | 用户查看自己的反馈 |
| GET | `/admin/feedbacks` | 管理端反馈列表 |
| PUT | `/admin/feedbacks/{id}/reply` | 回复反馈 |
| POST | `/messages` | 客服留言 |
| GET | `/admin/messages` | 管理端留言列表 |
| PUT | `/admin/messages/{id}/reply` | 回复留言 |

### 3.5 file-service（新增）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/upload/image` | 上传图片（返回 URL） |
| GET | `/files/{id}` | 下载/查看文件 |

### 3.6 hm-api Feign 接口扩展

对应上述所有跨服务调用的接口，在 `hm-api` 模块新增对应 Feign Client：
- `ReviewClient`
- `FavoriteClient`
- `CouponClient`
- `NotificationClient`
- `FileClient`

### 3.7 网关安全

扩展 `AuthGlobalFilter`，JWT payload 增加 `role` 字段（`user` / `admin`）：
- `/admin/**` 路由仅允许 role=admin 访问
- 网关公共放行路径：`/users/login`, `/users/register`, `/users/send-code`, `/users/reset-password`, `/items/**`, `/search/**`, `/notifications/active`
- 其余路径需携带有效 JWT token

---

## 4. 前端设计

### 4.1 hmall-web（客户端）Vue 3 项目结构

```
hmall-web/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── App.vue
│   ├── main.js
│   ├── router/index.js
│   ├── stores/
│   │   ├── user.js              # 用户登录状态
│   │   ├── cart.js              # 购物车状态
│   │   └── favorite.js          # 收藏状态
│   ├── api/
│   │   ├── user.js              # 用户相关 API
│   │   ├── item.js              # 商品相关 API
│   │   ├── cart.js              # 购物车 API
│   │   ├── order.js             # 订单 API
│   │   ├── address.js           # 地址 API
│   │   └── common.js            # 公告/反馈/上传 API
│   ├── views/
│   │   ├── Home.vue             # 首页（轮播图、分类导航、热门/新品/促销商品）
│   │   ├── Category.vue         # 商品分类浏览页
│   │   ├── ItemDetail.vue       # 商品详情页
│   │   ├── Search.vue           # 搜索结果页
│   │   ├── Cart.vue             # 购物车页
│   │   ├── OrderConfirm.vue     # 订单确认页（选地址、优惠券）
│   │   ├── OrderList.vue        # 我的订单页（状态Tab切换）
│   │   ├── FavoriteList.vue     # 我的收藏页
│   │   ├── AddressList.vue      # 收货地址管理页
│   │   ├── Login.vue            # 登录/注册/找回密码页
│   │   ├── Profile.vue          # 个人信息修改页
│   │   ├── Feedback.vue         # 用户反馈页
│   │   └── Notifications.vue    # 系统公告列表页
│   ├── components/
│   │   ├── AppHeader.vue        # 顶部导航栏
│   │   ├── AppFooter.vue        # 底部
│   │   ├── ProductCard.vue      # 商品卡片
│   │   ├── StarRating.vue       # 星级评分组件
│   │   ├── ReviewSection.vue    # 评价区域
│   │   └── CouponSelector.vue   # 优惠券选择器
│   └── styles/
│       └── global.css           # 全局样式
```

#### 路由表

| 路径 | 视图 | 说明 |
|------|------|------|
| `/` | Home | 首页 |
| `/category` | Category | 分类浏览 |
| `/item/{id}` | ItemDetail | 商品详情 |
| `/search` | Search | 搜索结果 |
| `/cart` | Cart | 购物车（需登录） |
| `/order/confirm` | OrderConfirm | 订单确认（需登录） |
| `/orders` | OrderList | 我的订单（需登录） |
| `/favorites` | FavoriteList | 收藏列表（需登录） |
| `/addresses` | AddressList | 地址管理（需登录） |
| `/login` | Login | 登录/注册 |
| `/profile` | Profile | 个人信息（需登录） |
| `/feedback` | Feedback | 用户反馈（需登录） |
| `/notifications` | Notifications | 公告列表 |

### 4.2 hmall-admin（管理后台）Vue 3 项目结构

```
hmall-admin/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── App.vue
│   ├── main.js
│   ├── router/index.js
│   ├── stores/
│   │   └── admin.js             # 管理员状态
│   ├── api/
│   │   ├── auth.js              # 登录
│   │   ├── user.js              # 用户管理
│   │   ├── item.js              # 商品管理
│   │   ├── order.js             # 订单管理
│   │   └── system.js            # 公告/反馈/分类
│   ├── views/
│   │   ├── Login.vue            # 管理员登录
│   │   ├── Dashboard.vue        # 首页数据看板
│   │   ├── UserList.vue         # 用户管理
│   │   ├── ItemList.vue         # 商品管理列表
│   │   ├── ItemEdit.vue         # 商品新增/编辑
│   │   ├── CategoryList.vue     # 分类管理
│   │   ├── OrderList.vue        # 订单管理列表
│   │   ├── OrderDetail.vue      # 订单详情
│   │   ├── ReviewList.vue       # 评价管理
│   │   ├── BannerList.vue       # 轮播图管理
│   │   ├── NotificationList.vue # 公告管理
│   │   ├── FeedbackList.vue     # 反馈管理
│   │   └── Profile.vue          # 管理员个人中心
│   └── components/
│       ├── AdminLayout.vue      # 管理后台布局框架
│       ├── StatCard.vue         # 统计卡片
│       ├── ChartPanel.vue       # ECharts 图表面板
│       └── ImageUpload.vue      # 图片上传组件
```

#### 路由表

| 路径 | 视图 | 说明 |
|------|------|------|
| `/login` | Login | 管理员登录 |
| `/dashboard` | Dashboard | 数据看板 |
| `/users` | UserList | 用户管理 |
| `/items` | ItemList | 商品列表 |
| `/items/add` | ItemEdit | 新增商品 |
| `/items/{id}/edit` | ItemEdit | 编辑商品 |
| `/categories` | CategoryList | 分类管理 |
| `/orders` | OrderList | 订单列表 |
| `/orders/{id}` | OrderDetail | 订单详情 |
| `/reviews` | ReviewList | 评价管理 |
| `/banners` | BannerList | 轮播图管理 |
| `/notifications` | NotificationList | 公告管理 |
| `/feedbacks` | FeedbackList | 反馈管理 |
| `/profile` | Profile | 个人中心 |

---

## 5. Redis 缓存设计

| 缓存对象 | 策略 | TTL | 实现方式 |
|----------|------|-----|----------|
| 商品详情 | 查询即缓存，更新即失效 | 30min | `@Cacheable` + `@CacheEvict` |
| 商品分类树 | 启动预加载 + 定时刷新 | 1h | 手动缓存 + `@Scheduled` |
| 首页热门商品 | 每10分钟刷新 | 10min | `@Scheduled` + Redis |
| 用户 Token 黑名单 | 登出时写入 | 等于 token TTL | Redis `SET` |
| 邮箱验证码 | 发送时写入，验证后删除 | 5min | Redis `SETEX` |
| 购物车 | 用户维度 Hash | 30min | Redis Hash（回源 MySQL） |
| 秒杀库存（预留） | 预减库存 | — | Redis `DECR` + Lua |

hm-common 新增 `com.hmall.common.config.RedisConfig`，使用 Jackson 序列化。

---

## 6. Docker 部署

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: hmall
    ports: ["3306:3306"]
    volumes: ["./docker/mysql:/var/lib/mysql"]

  nacos:
    image: nacos/nacos-server:v2.1.0
    environment:
      MODE: standalone
    ports: ["8848:8848"]

  redis:
    image: redis:7.0
    ports: ["6379:6379"]

  rabbitmq:
    image: rabbitmq:3.9-management
    ports: ["5672:5672", "15672:15672"]

  hm-gateway:
    build: ./hm-gateway
    ports: ["8080:8080"]

  user-service:
    build: ./user-service

  item-service:
    build: ./item-service

  cart-service:
    build: ./cart-service

  trade-service:
    build: ./trade-service

  pay-service:
    build: ./pay-service

  notify-service:
    build: ./notify-service

  file-service:
    build: ./file-service

  hmall-web:
    image: nginx:alpine
    volumes: ["./hmall-web/dist:/usr/share/nginx/html"]
    ports: ["80:80"]

  hmall-admin:
    image: nginx:alpine
    volumes: ["./hmall-admin/dist:/usr/share/nginx/html"]
    ports: ["81:80"]
```

每个 Java 服务 Dockerfile 模板：
```dockerfile
FROM openjdk:11-jre
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 7. 实现顺序

按依赖关系分阶段：

| 阶段 | 内容 | 估时 |
|------|------|------|
| **Phase 1** | 后端基础设施：新增 `hm-common` Redis 配置、`hm-api` Feign 接口扩展、网关角色鉴权 | 1d |
| **Phase 2** | 新增微服务：`notify-service` + `file-service` | 0.5d |
| **Phase 3** | 现有服务功能扩展：user-service(地址+收藏+验证码)、item-service(评价+分类)、trade-service(优惠券+退款) | 1d |
| **Phase 4** | hmall-web 客户端前端（Vue 3 + Element Plus + Pinia） | 1d |
| **Phase 5** | hmall-admin 管理后台前端 | 1d |
| **Phase 6** | Redis 缓存集成、Docker Compose 编排 | 0.5d |
| **Phase 7** | 联调测试、修复 | 0.5d |

**总计约 5.5 天**。

---

## 8. 验收对照

对照实验指导书评分标准：

### 基本内容（20分）
- [x] 1.1.1 商品模块：分类浏览、详情、搜索、收藏、评价
- [x] 1.1.2 购物车模块：加购、列表、改数量、勾选、结算
- [x] 1.1.3 订单模块：确认、提交、模拟支付、订单列表、取消/收货/退款/物流
- [x] 1.2.1 管理员认证：登录、退出
- [x] 1.2.2 数据看板：统计卡片 + ECharts 图表
- [x] 1.2.3 用户管理：列表、搜索、禁用/启用
- [x] 1.2.4 商品管理：分类管理、商品 CRUD、上下架、评价管理
- [x] 1.2.5 订单管理：列表筛选、详情、发货、退款处理、导出 Excel

### 进阶分析（20分）
- [x] 2.1 Element Plus + Pinia + 认证鉴权
- [x] 2.2 界面设计要求
- [x] 2.3 客户端进阶：注册/验证码、找回密码、首页轮播/分类/推荐、地址管理、优惠券
- [x] 2.4 管理后台进阶：轮播/广告管理、公告管理、反馈管理、个人中心
- [x] 2.5 其他：响应式布局、图片上传、数据分页

### 创新拓展（10分）
- [x] 3.1 Redis 缓存热点数据
- [x] 3.2 微服务架构拆分
- [x] 3.3 Docker 容器化部署
