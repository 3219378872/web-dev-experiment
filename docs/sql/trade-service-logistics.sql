-- Phase 3: trade-service 物流轨迹表
-- docker exec -i hmall-mysql-1 mysql -uroot -proot hmall < docs/sql/trade-service-logistics.sql

CREATE TABLE IF NOT EXISTS logistics_trace (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    node VARCHAR(64) NOT NULL COMMENT '物流节点名称',
    description VARCHAR(255) COMMENT '节点描述',
    trace_time DATETIME NOT NULL COMMENT '轨迹时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '物流轨迹表';
