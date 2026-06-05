# Handoff: Fix Order Favorite Display

## Status

已完成。

## Files Changed

- `trade-service/src/main/java/com/hmall/domain/vo/OrderDetailVO.java` (新增)
- `trade-service/src/main/java/com/hmall/domain/vo/OrderVO.java` (修改)
- `trade-service/src/main/java/com/hmall/controller/OrderController.java` (修改)
- `user-service/src/main/java/com/hmall/domain/vo/FavoriteVO.java` (新增)
- `user-service/src/main/java/com/hmall/controller/FavoriteController.java` (修改)
- `user-service/src/test/java/com/hmall/UserServiceTestBase.java` (修改)

## Commands Run

- `mvn -q -pl trade-service -am test` - 通过
- `mvn -q -pl user-service -am test` - 通过

## Known Risks

- 无

## Next Action

提交代码并创建 PR。

## Worktree Or Branch

- `task/2026-06-05-fix-order-favorite-display`
