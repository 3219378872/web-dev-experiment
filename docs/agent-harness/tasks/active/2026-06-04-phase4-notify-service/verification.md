# Verification: Phase 4 — notify-service

## Test Strategy

### Unit Tests

`FaqServiceImplTest`（`@SpringBootTest` + `@Transactional` + H2）：

1. `getActiveFaqs_returnsActiveOrderedBySortThenTime` — 验证只返回 status=1 且按 sort ASC + createTime DESC 排序
2. `getActiveFaqs_noActive_returnsEmpty` — 无数据时空列表
3. `getActiveFaqs_allDisabled_returnsEmpty` — 全部禁用时返回空
4. `saveAndGetById_works` — PO 持久化验证
5. `getById_nonExistent_returnsNull` — 不存在 ID 返回 null
6. `getActiveFaqs_sortsBySortAsc` — 排序准确性

### Existing Endpoint Verification

客服消息端点已存在（`CustomerMessageController`）：
- `POST /messages` → `R<Void>`
- `GET /admin/messages` → `PageDTO<CustomerMessage>`
- `PUT /admin/messages/{id}/reply` → `R<Void>`

已有对应测试（`CustomerMessageServiceImplTest`），无需新增。

## Commands

```bash
mvn -B -ntp -pl notify-service -am test
python3 scripts/engineering-lint.py
```
