CREATE TABLE IF NOT EXISTS uploads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(255),
    file_path VARCHAR(512),
    file_size BIGINT,
    content_type VARCHAR(128),
    file_type VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
