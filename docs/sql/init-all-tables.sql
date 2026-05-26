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
    status VARCHAR(16) DEFAULT 'NORMAL',
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

CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128),
    image_url VARCHAR(512) NOT NULL,
    link_url VARCHAR(512),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- ==================== 测试数据 ====================

-- 管理员用户 (password: admin123)
INSERT INTO `user` (username, password, role, status, create_time) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', 'NORMAL', NOW()),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user', 'NORMAL', NOW());

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
