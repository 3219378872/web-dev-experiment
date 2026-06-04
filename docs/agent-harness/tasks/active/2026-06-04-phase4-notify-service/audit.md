# Audit: Phase 4 — notify-service

## Changes Summary

| Category | Files | Description |
|---|---|---|
| PO | `Faq.java` | 新增 FAQ 持久化对象 |
| VO | `FaqVO.java` | FAQ 视图对象（id, question, answer） |
| Mapper | `FaqMapper.java` | MyBatis-Plus BaseMapper |
| Service | `IFaqService.java`, `FaqServiceImpl.java` | FAQ 业务逻辑 |
| Controller | `FaqController.java` | `GET /faqs` 端点 |
| SQL | `docs/sql/notify-service-faq.sql` | DDL 迁移脚本 |
| Test Schema | `schema.sql`（两处） | H2 测试表定义 |
| Unit Test | `FaqServiceImplTest.java` | 6 个测试用例 |
| Docs | `notify-service.md` (KB) | 知识库更新 |
| Harness | 5 files | 任务记录 |

## Convention Compliance

- [x] 写操作：无需新增（FAQ 仅读端点）
- [x] 读操作：`List<FaqVO>` — 符合约定
- [x] SQL 迁移脚本独立文件，不改写既有 DDL
- [x] PO 遵循现有模式（Lombok @Data, @TableName, @TableId AUTO）
- [x] 无硬编码密钥
- [x] 无跨服务调用

## Risk Assessment

- **低风险**：纯新增代码，不修改任何现有文件内容（仅追加 test schema）
- 客服占位确认：现有端点 + 测试已存在，无需改动
