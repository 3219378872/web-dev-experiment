# Handoff: Fix Cart Num Buy Now

## Status
Completed.

## Files Changed
- `cart-service/src/main/java/com/hmall/cart/domain/dto/CartFormDTO.java` - 添加 num 字段
- `hm-api/src/main/java/com/hmall/api/dto/CartFormDTO.java` - 添加 num 字段
- `cart-service/src/main/java/com/hmall/cart/mapper/CartMapper.java` - updateNum 方法支持累加指定数量
- `cart-service/src/main/java/com/hmall/cart/service/impl/CartServiceImpl.java` - 使用 DTO 中的 num 字段
- `hmall-web/src/views/ItemDetail.vue` - "立即购买"按钮改为先添加购物车再跳转

## Commands Run
- `mvn -q -pl cart-service -am test` - 通过

## Known Risks
- 无

## Next Action
提交代码并创建 PR。

## Worktree Or Branch
- `task/2026-06-05-fix-cart-num-buy-now`
