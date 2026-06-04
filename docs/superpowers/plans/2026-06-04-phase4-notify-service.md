# Phase 4: notify-service — FAQ + 客服占位收尾

## 目标

实现 Backend API Fulfillment spec Phase 4：

1. `GET /faqs` → `List<FaqVO>` — 来自 `faq` 表，只返回启用状态的 FAQ，按 sort 升序。
2. 客服消息收发 — 确认已有 `/messages`、`/admin/messages` + reply 端点正常工作，保留占位。

## 实施步骤

### Step 1: 数据模型

- [x] 创建 `Faq` PO（`domain/po/Faq.java`）：id, question, answer, sort, status, createTime, updateTime
- [x] 创建 `FaqVO`（`domain/vo/FaqVO.java`）：id, question, answer（不含 sort/status/timestamps）
- [x] 创建 SQL 迁移脚本（`docs/sql/notify-service-faq.sql`）

### Step 2: 持久层

- [x] 创建 `FaqMapper`（`mapper/FaqMapper.java`）extends `BaseMapper<Faq>`

### Step 3: 服务层

- [x] 创建 `IFaqService`（`service/IFaqService.java`）：`getActiveFaqs()` 方法
- [x] 创建 `FaqServiceImpl`（`service/impl/FaqServiceImpl.java`）：
  - 查询 `status=1` 的 FAQ
  - 按 `sort` ASC + `createTime` DESC 排序
  - 转换为 `FaqVO` 返回

### Step 4: 控制层

- [x] 创建 `FaqController`（`controller/FaqController.java`）：`GET /faqs` → `List<FaqVO>`

### Step 5: 客服占位确认

- [x] 确认 `CustomerMessageController` 已有：
  - `POST /messages` — 用户发送消息（`R<Void>`）
  - `GET /admin/messages` — 管理端列表（`PageDTO<CustomerMessage>`）
  - `PUT /admin/messages/{id}/reply` — 管理端回复（`R<Void>`）
- 无需新增代码，保留占位

### Step 6: 测试

- [x] 更新测试 schema（H2）加入 `faq` 表
- [x] 编写 `FaqServiceImplTest` 单元测试：
  - `getActiveFaqs` 只返回 status=1 的记录
  - 排序验证（sort ASC + createTime DESC）
  - 空结果处理
  - PO 持久化验证

### Step 7: 文档与 lint

- [x] 更新 `docs/knowledge-base/modules/notify-service.md`
- [x] 创建 harness 任务记录
- [x] 运行 `engineering-lint.py`

## 验证

```bash
mvn -B -ntp -pl notify-service -am test
python3 scripts/engineering-lint.py
```

## 风险与缓解

- 无跨服务调用，无新依赖
- 客服占位不涉及 WebSocket，仅确认现有端点
