# Context: Fix Issue 58 59 Seed Data Encoding

## Objective
修复开发栈中文种子数据双重编码乱码（#59），并补充空业务表的种子数据（#58）。

## Scope
- In scope:
  - init-all-tables.sql 添加 SET NAMES utf8mb4;
  - 新增 seed-dev.sql 为空表补自洽演示数据
  - docker-compose.yml 调整 init 脚本挂载顺序
- Out of scope:
  - 后端业务代码改动
  - 生产环境数据迁移

## Related Artifacts
- Spec: null —— docker-compose/SQL 修复，无需独立 spec。
- Plan: null —— 变更范围明确，无需独立 plan。

## Likely Files
- `docs/sql/init-all-tables.sql`
- `docs/sql/seed-dev.sql`
- `docker-compose.yml`

## Runtime Evidence To Inspect First
- `docker compose down -v && docker compose up -d` 后检查商品名是否为中文
- `SELECT CHAR_LENGTH(name), HEX(name) FROM hmall.item WHERE id=1;`

## Safety Constraints
- 不在 SQL 中硬编码密钥或敏感信息。
- seed 数据外键必须自洽（order.id ↔ pay_order.biz_order_no、order.total_fee ↔ order_detail 汇总）。

## Worktree Or Branch
- `task/2026-06-04-fix-issue-58-59-seed-data-encoding`

## Open Questions
- none
