# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 只展示后端支持的支付方式 | done | `OrderConfirm.vue` 支付选项来自 `paymentOptions`，只含余额支付；`OrderConfirm.spec.js` 断言不出现支付宝/微信。 |
| 订单提交支付语义与 pay-service 一致 | done | `buildOrderPayload()` 固定 `paymentType=3`；`OrderDetail.vue` 仍走 `payChannelCode=balance/payType=5` 的余额支付路径。 |
| 移除不可提交的配送/发票/备注控件 | done | `OrderConfirm.vue` 移除当日达、配送时间 select、发票 select、备注 input；测试断言无 select 和商家留言 input。 |
| payload 仅包含 OrderFormDTO 支持字段 | done | `checkoutOptions.spec.js` 断言仅包含 addressId/paymentType/freight/details/couponId，且不含 remark/invoice/deliveryTime。 |
| KB/harness 共变 | done | `docs/knowledge-base/modules/hmall-web.md` 与本 task 记录已更新；agent_harness、knowledge_base、engineering-lint 均通过。 |
