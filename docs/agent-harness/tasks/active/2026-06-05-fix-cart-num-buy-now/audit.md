# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| CartFormDTO 添加 num 字段 | pass | cart-service 和 hm-api 的 CartFormDTO 已添加 num 字段 |
| CartMapper.updateNum 支持累加指定数量 | pass | updateNum 方法已更新为接收 num 参数 |
| CartServiceImpl 使用 DTO 中的 num 字段 | pass | addItem2Cart 方法已更新 |
| ItemDetail.vue "立即购买"按钮功能 | pass | 已改为先添加购物车再跳转 |
