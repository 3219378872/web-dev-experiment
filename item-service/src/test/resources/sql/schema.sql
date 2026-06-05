CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image VARCHAR(255),
    category VARCHAR(255),
    brand VARCHAR(255),
    spec VARCHAR(255),
    sold INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    isAD TINYINT(1) DEFAULT 0,
    status INT DEFAULT 1,
    is_seckill TINYINT(1) DEFAULT 0,
    is_recommend TINYINT(1) DEFAULT 0,
    is_seven_day_return TINYINT(1) DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME,
    creater BIGINT,
    updater BIGINT
);

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

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
