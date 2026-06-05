-- 商品发布设置字段迁移：seckill / recommend / seven_day_return
-- 对已有 item 表新增三列（新安装直接执行 init-all-tables.sql 即可，此文件仅用于存量 DB 升级）
-- MySQL 8 兼容：使用存储过程检查列是否存在再添加
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS add_item_publish_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'item'
          AND COLUMN_NAME = 'is_seckill'
    ) THEN
        ALTER TABLE `item` ADD COLUMN `is_seckill` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '加入秒杀';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'item'
          AND COLUMN_NAME = 'is_recommend'
    ) THEN
        ALTER TABLE `item` ADD COLUMN `is_recommend` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '推荐到首页';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'item'
          AND COLUMN_NAME = 'is_seven_day_return'
    ) THEN
        ALTER TABLE `item` ADD COLUMN `is_seven_day_return` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '支持七天无理由';
    END IF;
END$$

DELIMITER ;

CALL add_item_publish_columns();
DROP PROCEDURE IF EXISTS add_item_publish_columns;
