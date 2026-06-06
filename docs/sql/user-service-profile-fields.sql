-- user-service 个人资料补充字段：性别、生日
-- docker exec -i hmall-mysql-1 mysql -uroot -proot hmall < docs/sql/user-service-profile-fields.sql

ALTER TABLE `user`
    ADD COLUMN gender VARCHAR(1) DEFAULT 'N' COMMENT '性别：M男 F女 N保密' AFTER nickname,
    ADD COLUMN birthday DATE NULL COMMENT '生日' AFTER gender;
