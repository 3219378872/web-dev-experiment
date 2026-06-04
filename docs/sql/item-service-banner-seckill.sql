-- banner 表（轮播图 + 广告位）
CREATE TABLE IF NOT EXISTS `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `type` VARCHAR(50) NOT NULL DEFAULT 'carousel' COMMENT '类型：carousel=轮播图，ad=广告位',
  `position` VARCHAR(100) DEFAULT NULL COMMENT '广告位槽位标识',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_position` (`type`, `position`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图/广告位表';

-- seckill 表（限时秒杀）
CREATE TABLE IF NOT EXISTS `seckill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT NOT NULL COMMENT '商品ID',
  `seckill_price` INT NOT NULL COMMENT '秒杀价（分）',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `stock` INT NOT NULL COMMENT '库存数量',
  `sold` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='限时秒杀表';