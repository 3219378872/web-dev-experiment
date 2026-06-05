-- feedbacks 表增加 images 列（用于反馈截图）
-- 执行方式: mysql -u root -p hmall < docs/sql/notify-service-feedbacks-images.sql

ALTER TABLE feedbacks ADD COLUMN images TEXT COMMENT '上传的截图URL，逗号分隔' AFTER content;
