# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 提交分支残留前端视觉对齐改动 | done | hmall-web/hmall-admin 多页 + global.css/AdminLayout 等纳入提交 |
| 保持 API 与数据逻辑不变 | done | diff 扫描仅 1 处涉及调用（OrderList 分页），其余为模板/样式；无 axios/store/router 逻辑改动 |
| 修复分页功能回归（admin OrderList） | done | 装饰性 `<a>1/2/3/›</a>`（无 handler）→ `totalPages`/`pageRange` + `@click="fetch(p)"`/prev/next |
| 修复 hmall-web 单测 | done | `App.spec.ts` 部分 mock `vue-router.useRoute`，保留 `createRouter` 真实导出；2/2 通过 |
| 清理误入文件 | done | 删除空文件 EOF、`e2e/visual/` 下一次性调试脚本（analyze*/check-*/find-diff-regions/test-pixelmatch.js 与 test-mock 调试 spec）；根 test-results/ 取消暂存并加入 `.gitignore` |
| KB co-change（K005） | done | 更新 `docs/knowledge-base/modules/hmall-web.md`、`docs/knowledge-base/modules/hmall-admin.md` sync_note 并 bump last_synced_commit |
| 前端测试/构建通过 | done | 见 verification.md |
| 不改后端 / CLAUDE / AGENTS | done | 变更仅前端、e2e、.gitignore、KB、本任务记录 |
| 修正 backend-api.md 待补接口表述 | done | 逐条核验后端 16 个 Controller：纠正约 12 类误报（OrderList/Coupons/OrderConfirm/OrderDetail/ItemDetail/Profile/Address/admin 商品·订单·公告·反馈 实为已实现）；补 2 处漏报路径不一致（web `/items/search`→`/search/list`、`PUT /carts/{id}`→`PUT /carts`）；保留真实缺失（/admin/users、/admin/reviews、banner、dashboard、秒杀、客服、运费、物流、相关推荐、导出等） |
| codex-review blocking findings 整改 | done | ①AppHeader 去硬编码「林晓」/角标3，改绑 userStore.username 与 cartStore.totalCount（0 时隐藏）②FlashSale 接 /items/page 真实加载+真实加购（移除原型硬编码商品与无效抢购动作）③Service 接 POST /messages（新增 common.sendCustomerMessage）④Coupons 按真实数据计算计数与平台/品类分类（去除未完成标记与演示填充注释） |
| codex-review 第 2 轮整改 | done | Coupons 映射改用真实 Coupon 字段（discountType/discountValue/minAmount/endTime），消除 满undefined；移除冗余二进制 web-mall.zip 并 gitignore *.zip（原型以 prototype/ 解压形式保留） |
| codex-review 第 3 轮整改 | done | FeedbackList 状态对齐后端真实两态（0 待处理/1 已回复）：移除自定义状态 chip 与 0/2/3 tab 过滤、回复仅提交 reply（后端固定置 1）；修正 backend-api.md 中"回复可带 status"的错误表述 |
| codex-review 第 4 轮整改 | done | 详情页取数改用按 id 直连：web OrderDetail 用 getOrderById(GET /orders/{id}) 替代翻第一页客户端查找；admin ItemEdit 用 getItemById(GET /items/{id}) 替代 size:1 列表查找，修复超出首页/列表的订单与商品"不存在"漏数据回归 |
| codex-review 第 6 轮整改 | done | Service 留言（公开路由 + 需登录的 POST /messages）：未登录提示登录不调接口、成功才回执、失败如实提示，消除匿名下静默丢消息的假成功 |
| codex-review 第 7 轮整改 | done | Login：协议勾选框绑定 agreed（原硬编码已勾但 agreed 恒 false，注册被永久拦截）；找回密码补回新密码输入框并校验（原表单缺 newPassword 输入，重置必失败） |
| codex-review 第 8 轮整改 | done | ①Login 注册补回用户名输入（后端 username @NotBlank）②ItemEdit 保存价格元→分换算（后端 Integer 分）③OrderConfirm 下单补传 addressId（后端字段虽暂未使用，补齐更正确） |
| codex-review 第 9 轮整改 | done | Feedback 我的反馈接 GET /my-feedbacks；Profile 统计（余额/优惠券/收藏/订单状态）接真实接口、移除会员等级/积分/成长值等无后端虚构与假手机号；AccountSidebar/Home 去硬编码积分；OrderDetail 物流轨迹改为基于真实状态/时间戳、去除虚构快递员与网点 PII |
| admin Dashboard 运营数据 | out of scope（已披露） | 看板统计后端接口不存在（backend-api.md B4），main 版本亦无后端调用；本页为视觉对齐+演示数据，已在 Dashboard.vue 顶部、context Out of scope、KB、PR 描述中明确声明留待后端接入 |
| 无需独立 spec/plan | not applicable | 纯前端视觉收尾，见 task.yaml spec_waiver/plan_waiver |
| FeedbackList 分页装饰化是否回归 | not applicable | `fetch()` 不传分页参数、数据层本不分页，HEAD 版 `el-pagination` 翻页亦只重拉全量；保留现状非行为回归 |
