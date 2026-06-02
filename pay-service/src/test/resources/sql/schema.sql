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
