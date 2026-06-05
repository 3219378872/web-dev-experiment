# Context: Fix Cart Num Buy Now

## Objective
修复购物车数量字段缺失和立即购买按钮无功能的问题。

## Scope
- In scope:
  - CartFormDTO 添加 num 字段
  - CartMapper.updateNum 方法支持累加指定数量
  - CartServiceImpl.addItem2Cart 方法使用 DTO 中的 num 字段
  - ItemDetail.vue "立即购买"按钮改为先添加购物车再跳转
- Out of scope:
  - 其他购物车功能

## Related Artifacts
- Spec: none - 简单bug修复，无需单独spec
- Plan: none - 修复方案明确，无需单独plan

## Likely Files
- `cart-service/src/main/java/com/hmall/cart/domain/dto/CartFormDTO.java`
- `hm-api/src/main/java/com/hmall/api/dto/CartFormDTO.java`
- `cart-service/src/main/java/com/hmall/cart/mapper/CartMapper.java`
- `cart-service/src/main/java/com/hmall/cart/service/impl/CartServiceImpl.java`
- `hmall-web/src/views/ItemDetail.vue`

## Runtime Evidence To Inspect First
- none

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.

## Worktree Or Branch
- `task/2026-06-05-fix-cart-num-buy-now`

## Open Questions
- none
