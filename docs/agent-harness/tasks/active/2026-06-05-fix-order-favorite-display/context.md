# Context: Fix Order Favorite Display

## Objective

修复订单详情页面和收藏页面缺少商品名称和图片的问题（GitHub Issue #69）。

## Scope

- In scope:
  - Bug #1: 订单详情页面缺少商品名称和图片
  - Bug #2: 收藏页面缺少商品名称和图片
- Out of scope:
  - 其他页面的显示问题
  - 性能优化

## Related Artifacts

- Spec: none - 这是一个简单的bug修复，不需要单独的spec
- Plan: none - 修复方案明确，不需要单独的plan

## Likely Files

- `trade-service/src/main/java/com/hmall/domain/vo/OrderDetailVO.java` (新增)
- `trade-service/src/main/java/com/hmall/domain/vo/OrderVO.java` (修改)
- `trade-service/src/main/java/com/hmall/controller/OrderController.java` (修改)
- `user-service/src/main/java/com/hmall/domain/vo/FavoriteVO.java` (新增)
- `user-service/src/main/java/com/hmall/controller/FavoriteController.java` (修改)
- `user-service/src/test/java/com/hmall/UserServiceTestBase.java` (修改)

## Runtime Evidence To Inspect First

- 订单详情页面应显示商品名称、图片、价格、数量
- 收藏页面应显示商品名称、图片、价格、销量

## Safety Constraints

- 不要在 `docker-compose.yml`、application.yaml 或 .env 文件中提交密钥
- 不要破坏 `hm-common.dto.Result` / `PageDTO` 返回的公共 API 响应封装

## Worktree Or Branch

- `task/2026-06-05-fix-order-favorite-display`

## Open Questions

- none
