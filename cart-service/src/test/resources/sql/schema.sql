CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    name VARCHAR(255),
    spec VARCHAR(255),
    price INT,
    image VARCHAR(255),
    num INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
