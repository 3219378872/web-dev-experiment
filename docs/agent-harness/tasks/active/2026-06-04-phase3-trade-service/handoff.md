# Handoff: Phase 3 — trade-service

## Status

Phase 3 实现完成，所有测试通过，JaCoCo 门控满足。

## Dependencies

- Phase 1 (user-service) 实现后，`UserClient.countNewUsers` 将返回真实数据；当前返回 0。
- Phase 2 (item-service) 实现后，`ItemClient.queryItemByIds` 返回的品类信息更完整。
- 数据看板的访客数字段始终返回 0（无埋点系统）。

## Known Limitations

- `GET /admin/orders/export` 导出为 CSV 文本流，BOM 头兼容 Excel。
- 运费计算使用固定规则（满 99 元包邮，否则 10 元），未接入地址级运费。
- DashboardServiceImpl 的 getCategoryShare 和 topItems 依赖 ItemClient 获取品类信息，失败时降级为"其他"。

## Follow-up

- 推送分支到远程，开 PR 合入 main。
- Phase 4 (notify-service) 可独立进行。
