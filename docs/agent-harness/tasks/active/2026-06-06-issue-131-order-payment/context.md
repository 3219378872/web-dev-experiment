# Context: Issue 131 Order Payment

## Objective
修复 #131：C 端结算页不得展示后端不支持的支付渠道或可编辑但不会提交/落库的配送、发票、备注控件，订单提交与支付流程保持余额支付语义一致。

## Scope
- In scope:
  - `hmall-web/src/views/OrderConfirm.vue` 仅展示余额支付（`paymentType=3`）。
  - 配送方式/发票在结算页改为只读说明，移除当日达、配送时间、发票类型、订单备注等不可提交控件。
  - 订单提交 payload 只包含 `OrderFormDTO` 支持的字段：addressId/paymentType/freight/details/couponId。
  - 添加前端回归测试覆盖支付选项和提交 payload 契约。
- Out of scope:
  - 补齐支付宝/微信真实支付渠道。
  - 新增配送时间、发票、备注的后端字段、迁移与订单详情展示。
  - 修改 `OrderDetail.vue` 余额支付弹窗；当前已与 pay-service 能力一致。

## Related Artifacts
- Spec: none - issue 已明确指出当前后端只支持余额支付，验收方向是保留真实能力或补齐渠道；本次选择收敛前端能力，无需单独设计文档。
- Plan: none - 单页面前端契约修复，步骤直观，harness 记录足够承载。

## Likely Files
- `hmall-web/src/views/OrderConfirm.vue`
- `hmall-web/src/utils/checkoutOptions.js`
- `hmall-web/src/__tests__/checkoutOptions.spec.js`
- `hmall-web/src/__tests__/OrderConfirm.spec.js`
- `docs/knowledge-base/modules/hmall-web.md`

## Runtime Evidence To Inspect First
- `hm-api/src/main/java/com/hmall/api/dto/OrderFormDTO.java` 支持 addressId/paymentType/details/freight/couponId，不支持配送时间/发票/备注。
- `pay-service/src/main/java/com/hmall/controller/PayController.java` 只接受 `PayType.BALANCE`，非余额支付抛出“抱歉，目前只支持余额支付”。
- `hmall-web/src/views/OrderDetail.vue` 创建支付单固定 `payChannelCode: 'balance'`、`payType: 5`，说明前端实际支付路径为余额支付。

## Safety Constraints
- 不改后端接口响应形态。
- 不新增后端字段或迁移来承诺尚未设计的配送/发票/备注能力。
- 不展示会误导用户选择支付宝/微信的入口，除非同时补齐真实支付渠道。

## Worktree Or Branch
- `task/2026-06-06-issue-131-order-payment`

## Open Questions
- none
