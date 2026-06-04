-- notify-service FAQ 表 DDL
-- 执行方式: mysql -u root -p hmall < docs/sql/notify-service-faq.sql

CREATE TABLE IF NOT EXISTS faq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME
);
