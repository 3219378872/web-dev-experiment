# Context: Fix Issue #72 — Seed Data 每品类补足 100+ 商品

## Objective

修复 GitHub Issue #72：当前 `docs/sql/init-all-tables.sql` 仅有 6 条商品数据，
无法满足开发/测试场景中的分页、搜索、品类筛选等功能验证需求。
目标是为 5 个品类各生成 100 条以上商品，共 500+ 条，追加到 `docs/sql/seed-dev.sql`。

## Scope

- In scope:
  - `docs/sql/seed-dev.sql` — 追加 500 条 item INSERT 数据
  - Harness 任务记录（本目录四文件 + task.yaml）
- Out of scope:
  - 任何业务逻辑、接口、前端代码
  - `docs/sql/init-all-tables.sql` — 核心 migration，不改写

## Related Artifacts

- Issue: https://github.com/3219378872/web-dev-experiment/issues/72
- 表结构参考: `docs/sql/init-all-tables.sql` (item 表定义)
- 数据文件: `docs/sql/seed-dev.sql`

## Likely Files

- `docs/sql/seed-dev.sql` — 唯一修改文件

## Data Generation Strategy

- 5 品类 × 5 品牌 × 20 商品规格模板 = 500 条
- 品类：电子产品 / 运动户外 /美妆护肤 / 食品饮料 / 服饰鞋包（与 categories 表 name 精确匹配）
- 品牌：每品类 5 个真实品牌（电子→苹果/华为/小米/三星/索尼 等）
- price 单位分（INT），随机在合理区间；stock 10-1000；sold 0-5000；status=1
- id 使用 AUTO_INCREMENT，追加不影响已有数据（id 1-6 为原始数据）
- create_time 通过 DATE_ADD 从 2026-01-01 均匀分布，避免全部 NOW()

## Safety Constraints

- 不改写 init-all-tables.sql（不可变 migration）
- 不破坏已有 item id 1-6 数据
- SQL 使用单引号转义（`''`），兼容 MySQL 8
- 不引入任何密钥或硬编码凭据

## Branch And PR

- Base branch: main
- Task branch: task/2026-06-05-fix-issue-72-seed-data
- Remote branch: origin/task/2026-06-05-fix-issue-72-seed-data
- Pull request: to be opened after push
