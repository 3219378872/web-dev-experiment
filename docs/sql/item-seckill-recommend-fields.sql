-- 商品发布设置字段迁移：seckill / recommend / seven_day_return
-- 对已有 item 表新增三列（新安装直接执行 init-all-tables.sql 即可，此文件仅用于存量 DB 升级）
ALTER TABLE `item`
    ADD COLUMN IF NOT EXISTS `is_seckill` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '加入秒杀',
    ADD COLUMN IF NOT EXISTS `is_recommend` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '推荐到首页',
    ADD COLUMN IF NOT EXISTS `is_seven_day_return` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '支持七天无理由';
