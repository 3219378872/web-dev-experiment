CREATE TABLE IF NOT EXISTS item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price INT,
    stock INT DEFAULT 0,
    image VARCHAR(255),
    category VARCHAR(128),
    brand VARCHAR(128),
    spec VARCHAR(255),
    sold INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    isAD BOOLEAN DEFAULT FALSE,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creater BIGINT,
    updater BIGINT
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128),
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS item_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT DEFAULT 5,
    content TEXT,
    images VARCHAR(1024),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
