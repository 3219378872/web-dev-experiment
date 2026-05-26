CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    publish_time DATETIME,
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
