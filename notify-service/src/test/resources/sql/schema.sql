CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);

CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT,
    reply TEXT,
    status INT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);

CREATE TABLE IF NOT EXISTS customer_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT,
    reply TEXT,
    status INT DEFAULT 0,
    create_time DATETIME,
    reply_time DATETIME
);
