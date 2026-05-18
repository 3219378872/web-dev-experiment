-- hmall 电商平台 扩展表 DDL
-- 执行方式: mysql -u root -p hmall < docs/sql/init-extra-tables.sql

-- 商品评价
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

-- 商品收藏
CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    UNIQUE KEY uk_user_item (user_id, item_id)
);

-- 收货地址
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

-- 优惠券
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

-- 用户优惠券
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

-- 系统公告
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 用户反馈
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

-- 客服留言
CREATE TABLE IF NOT EXISTS customer_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    reply TEXT,
    status TINYINT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

-- 文件记录
CREATE TABLE IF NOT EXISTS uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(64),
    file_type VARCHAR(32),
    create_time DATETIME
);

-- 商品分类
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

-- 轮播图
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

-- 扩展 user 表（MySQL 不支持 IF NOT EXISTS for ALTER，需要手动检查）
-- ALTER TABLE user ADD COLUMN email VARCHAR(128);
-- ALTER TABLE user ADD COLUMN avatar VARCHAR(512);
-- ALTER TABLE user ADD COLUMN nickname VARCHAR(64);
-- ALTER TABLE user ADD COLUMN role VARCHAR(16) DEFAULT 'user';
