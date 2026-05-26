INSERT INTO `order` (id, total_fee, payment_type, user_id, status, create_time) VALUES
(1000, 50000, 1, 1, 1, NOW()),
(1001, 30000, 1, 1, 2, NOW()),
(1002, 20000, 1, 1, 3, NOW());

INSERT INTO coupons (id, name, discount_type, discount_value, min_amount, total_stock, remaining_stock, start_time, end_time, status, create_time) VALUES
(500, '新人优惠券', 1, 1000, 5000, 100, 100, '2026-01-01', '2027-12-31', 1, NOW()),
(501, '满减券', 1, 500, 10000, 50, 0, '2026-01-01', '2027-12-31', 1, NOW()),
(502, '过期券', 1, 2000, 20000, 20, 10, '2025-01-01', '2025-06-30', 1, NOW());
