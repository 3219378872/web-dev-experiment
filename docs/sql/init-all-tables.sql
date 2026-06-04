-- hmall 全量建表 SQL（基础表 + 扩展表）
-- docker exec -i hmall-mysql-1 mysql -uroot -proot hmall < docs/sql/init-all-tables.sql

-- ==================== 基础表 ====================

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(128),
    avatar VARCHAR(512),
    nickname VARCHAR(64),
    role VARCHAR(16) DEFAULT 'user',
    status INT DEFAULT 1 COMMENT '0=FROZEN 1=NORMAL',
    balance INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS `item` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    stock INT DEFAULT 0,
    image VARCHAR(512),
    category VARCHAR(64),
    brand VARCHAR(64),
    spec VARCHAR(255),
    sold INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    isAD BOOLEAN DEFAULT FALSE,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    creater BIGINT,
    updater BIGINT
);

CREATE TABLE IF NOT EXISTS `cart` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    name VARCHAR(255),
    spec VARCHAR(255),
    price INT,
    image VARCHAR(512),
    num INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY,
    total_fee INT NOT NULL,
    payment_type INT,
    user_id BIGINT NOT NULL,
    status INT DEFAULT 1,
    create_time DATETIME,
    pay_time DATETIME,
    consign_time DATETIME,
    end_time DATETIME,
    close_time DATETIME,
    comment_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    name VARCHAR(255),
    spec VARCHAR(255),
    price INT,
    image VARCHAR(512),
    num INT,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS order_logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    logistics_number VARCHAR(64),
    logistics_company VARCHAR(64),
    contact VARCHAR(64),
    phone VARCHAR(20),
    province VARCHAR(32),
    city VARCHAR(32),
    district VARCHAR(32),
    detail VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS pay_order (
    id BIGINT PRIMARY KEY,
    biz_order_no BIGINT NOT NULL,
    pay_order_no BIGINT,
    biz_user_id BIGINT,
    pay_channel_code VARCHAR(32),
    amount INT,
    pay_type INT,
    status INT DEFAULT 0,
    expand_json VARCHAR(1024),
    result_code VARCHAR(32),
    result_msg VARCHAR(255),
    pay_success_time DATETIME,
    pay_over_time DATETIME,
    qr_code_url VARCHAR(255),
    create_time DATETIME,
    update_time DATETIME,
    creater BIGINT,
    updater BIGINT,
    is_delete TINYINT(1) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS mq_outbox_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange_name VARCHAR(128) NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_status_retry (status, retry_count)
);

-- Seata AT 模式回滚日志表（单 hmall 库，所有 RM 共用一张）
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context, such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT = 'AT transaction mode undo table';

-- ==================== 扩展表 ====================

CREATE TABLE IF NOT EXISTS item_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    content TEXT,
    images VARCHAR(1024),
    rating TINYINT NOT NULL DEFAULT 5,
    create_time DATETIME NOT NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_item (user_id, item_id)
);

CREATE TABLE IF NOT EXISTS addresses (
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

CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    discount_type TINYINT NOT NULL,
    discount_value INT NOT NULL,
    min_amount INT DEFAULT 0,
    total_stock INT NOT NULL,
    remaining_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    used_order_id BIGINT,
    create_time DATETIME,
    use_time DATETIME,
    INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

CREATE TABLE IF NOT EXISTS customer_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

CREATE TABLE IF NOT EXISTS uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(64),
    file_type VARCHAR(32),
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS `banner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `link_url` VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `type` VARCHAR(50) NOT NULL DEFAULT 'carousel' COMMENT '类型：carousel=轮播图，ad=广告位',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '广告位槽位标识',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_position` (`type`, `position`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图/广告位表';

CREATE TABLE IF NOT EXISTS `seckill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` INT NOT NULL COMMENT '秒杀价（分）',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `stock` INT NOT NULL COMMENT '库存数量',
    `sold` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时秒杀表';

CREATE TABLE IF NOT EXISTS faq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- ==================== 测试数据 ====================

-- 管理员用户 (password: admin123)
-- UserStatus enum: 0=FROZEN, 1=NORMAL
INSERT INTO `user` (username, password, role, status, create_time) VALUES
('admin', '$2a$10$sx/sI.5QQ4sX4giAI.7YbeP.ZrAIqIE.5mexP.6MsV8DnI8rbKGEW', 'admin', 1, NOW()),
('testuser', '$2a$10$sx/sI.5QQ4sX4giAI.7YbeP.ZrAIqIE.5mexP.6MsV8DnI8rbKGEW', 'user', 1, NOW());

-- 示例商品
INSERT INTO item (name, price, stock, image, category, brand, spec, sold, status, create_time) VALUES
('无线蓝牙耳机', 29900, 500, 'https://img.alicdn.com/imgextra/i3/2200721534029/O1CN01XqUJ0J2G7NRh9d5L1_!!2200721534029.jpg', '电子产品', '品牌A', '黑色', 1280, 1, NOW()),
('轻薄笔记本电脑', 599900, 100, 'https://img.alicdn.com/imgextra/i1/2200721534029/O1CN01Gt9YBn2G7NRaP0WYC_!!2200721534029.jpg', '电子产品', '品牌B', '14寸 16GB', 560, 1, NOW()),
('运动跑鞋', 49900, 300, 'https://img.alicdn.com/imgextra/i4/2200721534029/O1CN01hGVUfK2G7NRdMiDHK_!!2200721534029.jpg', '运动户外', '品牌C', '42码 白色', 3200, 1, NOW()),
('防晒霜SPF50', 12900, 1000, 'https://img.alicdn.com/imgextra/i2/2200721534029/O1CN01RRWkqN2G7NRcM1rYn_!!2200721534029.jpg', '美妆护肤', '品牌D', '50ml', 890, 1, NOW()),
('智能手表', 199900, 200, 'https://img.alicdn.com/imgextra/i1/2200721534029/O1CN01lwZ5oU2G7NRfSwVmJ_!!2200721534029.jpg', '电子产品', '品牌A', '黑色 42mm', 2100, 1, NOW()),
('有机绿茶', 8900, 2000, 'https://img.alicdn.com/imgextra/i3/2200721534029/O1CN01XxGZQo2G7NRbOocQa_!!2200721534029.jpg', '食品饮料', '品牌E', '250g', 5600, 1, NOW());

-- 示例分类
INSERT INTO categories (name, parent_id, sort_order, status, create_time) VALUES
('电子产品', 0, 1, 1, NOW()),
('运动户外', 0, 2, 1, NOW()),
('美妆护肤', 0, 3, 1, NOW()),
('食品饮料', 0, 4, 1, NOW()),
('服饰鞋包', 0, 5, 1, NOW());

-- 示例公告
INSERT INTO notifications (title, content, publish_time, status, create_time) VALUES
('商城开业大促', '全场商品低至5折，快来选购！', NOW(), 1, NOW()),
('新用户注册福利', '注册即送50元优惠券！', NOW(), 1, NOW());
