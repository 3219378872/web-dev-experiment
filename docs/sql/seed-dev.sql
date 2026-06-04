-- hmall 开发环境补充种子数据
-- 挂载到 docker-entrypoint-initdb.d，在 init-all-tables.sql 之后执行
-- 前提：user.id 1=admin, 2=testuser；item.id 1-6；categories.id 1-5

SET NAMES utf8mb4;

-- ==================== 收货地址 ====================
INSERT INTO addresses (user_id, receiver_name, phone, province, city, district, detail, is_default, create_time) VALUES
(2, '张三', '13800138000', '广东省', '深圳市', '南山区', '科技园路1号', 1, NOW()),
(2, '张三', '13800138000', '广东省', '广州市', '天河区', '体育西路2号', 0, NOW());

-- ==================== 购物车 ====================
INSERT INTO cart (user_id, item_id, name, spec, price, num, image, create_time, update_time) VALUES
(2, 1, '无线蓝牙耳机', '黑色', 29900, 1, 'https://img.alicdn.com/imgextra/i3/2200721534029/O1CN01XqUJ0J2G7NRh9d5L1_!!2200721534029.jpg', NOW(), NOW()),
(2, 3, '运动跑鞋', '42码 白色', 49900, 2, 'https://img.alicdn.com/imgextra/i4/2200721534029/O1CN01hGVUfK2G7NRdMiDHK_!!2200721534029.jpg', NOW(), NOW());

-- ==================== 优惠券 ====================
INSERT INTO coupons (name, description, discount_type, discount_value, min_amount, total_stock, remaining_stock, start_time, end_time, status, create_time) VALUES
('新人优惠券', '新用户专享满50减10', 1, 1000, 5000, 100, 98, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 1, NOW()),
('全场9折券', '无门槛9折优惠', 2, 90, 0, 50, 45, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 1, NOW()),
('满减券', '满100减5', 1, 500, 10000, 50, 50, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 1, NOW()),
('过期券', '已过期示例', 1, 2000, 20000, 20, 10, '2025-01-01 00:00:00', '2025-06-30 23:59:59', 0, NOW());

-- ==================== 用户优惠券 ====================
INSERT INTO user_coupons (user_id, coupon_id, status, create_time) VALUES
(2, 1, 1, NOW()),
(2, 2, 1, NOW());

-- ==================== 商品收藏 ====================
INSERT INTO user_favorites (user_id, item_id, create_time) VALUES
(2, 1, NOW()),
(2, 3, NOW()),
(2, 5, NOW());

-- ==================== 订单 ====================
INSERT INTO `order` (total_fee, payment_type, user_id, status, create_time) VALUES
(79800, 1, 2, 1, NOW()),      -- 待付款
(29900, 1, 2, 2, NOW()),      -- 已付款
(12900, 1, 2, 3, NOW()),      -- 已发货
(49900, 1, 2, 4, NOW()),      -- 已完成
(199900, 1, 2, 5, NOW());     -- 已取消

-- ==================== 订单详情 ====================
INSERT INTO order_detail (order_id, item_id, name, spec, price, num, image, create_time) VALUES
(1, 1, '无线蓝牙耳机', '黑色', 29900, 2, 'https://img.alicdn.com/imgextra/i3/2200721534029/O1CN01XqUJ0J2G7NRh9d5L1_!!2200721534029.jpg', NOW()),
(1, 2, '轻薄笔记本电脑', '14寸 16GB', 599900, 0, 'https://img.alicdn.com/imgextra/i1/2200721534029/O1CN01Gt9YBn2G7NRaP0WYC_!!2200721534029.jpg', NOW()),
(2, 1, '无线蓝牙耳机', '黑色', 29900, 1, 'https://img.alicdn.com/imgextra/i3/2200721534029/O1CN01XqUJ0J2G7NRh9d5L1_!!2200721534029.jpg', NOW()),
(3, 4, '防晒霜SPF50', '50ml', 12900, 1, 'https://img.alicdn.com/imgextra/i2/2200721534029/O1CN01RRWkqN2G7NRcM1rYn_!!2200721534029.jpg', NOW()),
(4, 3, '运动跑鞋', '42码 白色', 49900, 1, 'https://img.alicdn.com/imgextra/i4/2200721534029/O1CN01hGVUfK2G7NRdMiDHK_!!2200721534029.jpg', NOW()),
(5, 5, '智能手表', '黑色 42mm', 199900, 1, 'https://img.alicdn.com/imgextra/i1/2200721534029/O1CN01lwZ5oU2G7NRfSwVmJ_!!2200721534029.jpg', NOW());

-- ==================== 订单物流 ====================
INSERT INTO order_logistics (order_id, logistics_company, logistics_number, contact, phone, province, city, district, detail, create_time, update_time) VALUES
(3, '顺丰速运', 'SF1234567890', '张三', '13800138000', '广东省', '深圳市', '南山区', '科技园路1号', NOW(), NOW()),
(4, '中通快递', 'ZT9876543210', '张三', '13800138000', '广东省', '广州市', '天河区', '体育西路2号', NOW(), NOW());

-- ==================== 物流轨迹 ====================
INSERT INTO logistics_trace (order_id, node, description, trace_time, create_time) VALUES
(3, '已揽件', '快递员已揽收包裹', NOW(), NOW()),
(3, '运输中', '包裹已到达深圳转运中心', DATE_ADD(NOW(), INTERVAL 2 HOUR), NOW()),
(4, '已签收', '客户已签收', NOW(), NOW());

-- ==================== 支付单 ====================
INSERT INTO pay_order (id, biz_order_no, pay_order_no, biz_user_id, pay_channel_code, amount, pay_type, status, create_time, update_time) VALUES
(3001, 200001, 400001, 2, 'balance', 29900, 5, 3, NOW(), NOW()),
(3002, 200002, 400002, 2, 'balance', 12900, 5, 3, NOW(), NOW()),
(3003, 200003, 400003, 2, 'balance', 49900, 5, 3, NOW(), NOW());

-- ==================== 商品评价 ====================
INSERT INTO item_reviews (user_id, item_id, content, rating, create_time) VALUES
(2, 1, '音质很好，续航也不错，推荐购买！', 5, NOW()),
(2, 3, '穿着很舒服，跑步很轻便。', 4, NOW()),
(2, 4, '防晒效果不错，不油腻。', 5, NOW());

-- ==================== 轮播图 ====================
INSERT INTO banner (image_url, link_url, title, type, position, sort, status, create_time, update_time) VALUES
('https://img.alicdn.com/imgextra/i1/2200721534029/O1CN01XqUJ0J2G7NRh9d5L1_!!2200721534029.jpg', '/item/1', '开业大促', 'carousel', 'home_top', 1, 1, NOW(), NOW()),
('https://img.alicdn.com/imgextra/i2/2200721534029/O1CN01RRWkqN2G7NRcM1rYn_!!2200721534029.jpg', '/item/4', '夏季护肤', 'carousel', 'home_top', 2, 1, NOW(), NOW());

-- ==================== FAQ ====================
INSERT INTO faq (question, answer, sort, status, create_time, update_time) VALUES
('如何下单？', '选择商品后点击“加入购物车”，进入购物车结算即可。', 1, 1, NOW(), NOW()),
('支持哪些支付方式？', '目前支持余额支付，后续将接入微信支付和支付宝。', 2, 1, NOW(), NOW()),
('如何申请退款？', '在订单详情页点击“申请退款”按钮，填写原因后提交。', 3, 1, NOW(), NOW()),
('物流多久能到？', '一般1-3个工作日送达，偏远地区可能延迟。', 4, 1, NOW(), NOW());

-- ==================== 用户反馈 ====================
INSERT INTO feedbacks (user_id, content, status, create_time) VALUES
(2, '希望增加微信支付', 0, NOW()),
(2, '首页加载有点慢', 0, NOW());

-- ==================== 客服留言 ====================
INSERT INTO customer_messages (user_id, content, status, create_time) VALUES
(2, '我的订单什么时候发货？', 0, NOW()),
(2, '怎么修改收货地址？', 1, NOW());
