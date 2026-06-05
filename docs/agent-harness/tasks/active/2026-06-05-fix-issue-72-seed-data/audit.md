# Audit: Fix Issue #72 — Seed Data 每品类补足 100+ 商品

| Requirement | Status | Evidence |
| --- | --- | --- |
| 每品类 100+ 条商品 | done | 每品类 100 条（5 品牌 × 20 规格），共 500 条 |
| 5 个品类全覆盖 | done | 电子产品、运动户外、美妆护肤、食品饮料、服饰鞋包 |
| category 与 categories 表精确匹配 | done | 与 init-all-tables.sql 中 INSERT INTO categories 一致 |
| 每品类 3-5 个品牌 | done | 每品类 5 个有代表性的真实品牌 |
| price 单位分(INT)，不溢出 | done | 最大值 999900（约 9999 元），远小于 INT_MAX (2147483647) |
| stock 随机 10-1000 | done | random.seed(42) 生成，均在范围内 |
| sold 随机 0-5000 | done | random.seed(42) 生成，均在范围内 |
| spec 有意义 | done | 含尺寸/颜色/容量等真实规格描述 |
| status=1 | done | 所有行 status=1 |
| create_time 适当间隔 | done | DATE_ADD 从 2026-01-01 按分钟均匀分布，跨 150 天 |
| 不破坏已有数据（id 1-6） | done | AUTO_INCREMENT 追加，不指定 id |
| 不改写 init-all-tables.sql | done | 仅修改 seed-dev.sql |
| SQL 语法 MySQL 8 兼容 | done | SET NAMES utf8mb4；DATE_ADD；单引号 '' 转义 |
| harness check 通过 | done | `python3 scripts/agent_harness.py check` 通过 |
| knowledge base check 通过 | done | `python3 scripts/knowledge_base.py check` 通过 |
| engineering-lint 通过 | done | `python3 scripts/engineering-lint.py` 通过 |
