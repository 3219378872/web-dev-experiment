-- password: admin123
INSERT INTO user (id, username, password, role, status, balance, email, create_time) VALUES
(1, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user', 'NORMAL', 10000, 'testuser@test.com', NOW()),
(2, 'existing', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user', 'NORMAL', 5000, 'existing@test.com', NOW());
