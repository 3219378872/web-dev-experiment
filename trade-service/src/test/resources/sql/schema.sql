CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY,
    total_fee INT NOT NULL DEFAULT 0,
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
    image VARCHAR(255),
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

CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128),
    description VARCHAR(255),
    discount_type INT,
    discount_value INT,
    min_amount INT DEFAULT 0,
    total_stock INT,
    remaining_stock INT,
    start_time DATETIME,
    end_time DATETIME,
    status INT DEFAULT 1,
    create_time DATETIME
);

CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status INT DEFAULT 1,
    used_order_id BIGINT,
    use_time DATETIME,
    create_time DATETIME,
    INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS mq_outbox_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exchange_name VARCHAR(128) NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
);

-- Seata AT undo_log（便携写法，兼容 H2 MySQL 模式与真实 MySQL）
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME(6) NOT NULL,
    log_modified DATETIME(6) NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id)
);
