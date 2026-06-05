# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 再次购买有 @click 绑定并调用 cartStore.addItem | done | OrderList.vue handleRepurchase 遍历 details 逐个加购 |
| 再次购买成功后跳转 /cart | done | router.push('/cart') |
| 删除订单有 @click 绑定并调 DELETE /orders/{id} | done | OrderList.vue handleDeleteOrder + api/order.js deleteOrder |
| 后端 DELETE /orders/{id} 校验归属与状态 | done | OrderController.deleteOrder + OrderServiceImpl.deleteOrder |
| 后端 DELETE /orders/{id} 返回 R<Void> | done | 遵守 CLAUDE.md 新增写接口约定 |
| 立即付款弹窗（OrderDetail.vue） | done | el-dialog 含金额/支付密码/确认按钮 |
| 支付流程：先创建支付单再执行余额支付 | done | confirmPay: createPayOrder → payOrderByBalance |
| ?pay=1 参数自动触发支付弹窗 | done | onMounted 检测 route.query.pay === '1' 且 status === 1 |
| 后端单测（deleteOrder） | done | OrderServiceImplTest 6 个 deleteOrder 测试用例 |
| 前端 Vitest 测试 | done | order-actions.spec.ts 覆盖再次购买/删除/支付函数 |
| 不破坏既有 endpoint 形态 | done | 仅新增接口，未修改现有接口 |
| 越权保护：userId 校验 | done | deleteOrder 检查 order.getUserId().equals(userId) |
