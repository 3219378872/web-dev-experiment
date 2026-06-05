# Handoff

## Summary

Issue #73 三个 Bug 全部修复：
1. 再次购买：遍历 `order.details` 加购到 CartStore，然后跳转 `/cart`。
2. 删除订单：后端新增 `DELETE /orders/{id}`（status=4/5 + userId 校验），前端 `handleDeleteOrder` 调用后刷新列表。
3. 立即付款：`OrderDetail.vue` 实现支付弹窗，走 `POST /pay-orders` + `POST /pay-orders/{id}` 余额支付流程；`?pay=1` 参数自动触发。

## Open Items

- 多渠道支付（支付宝/微信）暂未实现，目前只支持余额支付（`payType=3`），与 `PayController` 当前约束一致。
- 删除订单为物理删除（`removeById`），因为 `Order` 表无软删除字段；若后续引入软删，需在同一 PR 添加 DDL migration 和 `@TableLogic` 注解。
- CI codex-review job 依赖 `OPENAI_API_KEY` secret，若 secret 缺失则该 job 阻塞合并（已知限制，与 CI 配置一致）。
