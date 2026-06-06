# Context: Admin Orders

## Objective
Fix admin order list/detail gaps reported in GitHub issues #140 and #141 so visible operations map to real backend contracts.

## Scope
- In scope:
  - `hmall-admin` order list export, user filter, batch shipping, refund audit actions, and status-gated actions.
  - `hmall-admin` order detail print/export, real logistics traces, payment type display, and refund audit actions.
  - `trade-service` admin order list/export `userId` filtering.
  - Focused frontend utility tests and backend MockMvc coverage for the new admin query/export contract.
- Out of scope:
  - New address-edit backend contract; the unsupported modify-address action is hidden.
  - Harness archival after PR merge; the user requested leaving archival for a later unified pass.
  - Changing legacy success response envelope shapes.

## Related Artifacts
- Spec: none - issues #140 and #141 define concrete bugfix acceptance criteria.
- Plan: none - implementation is a bounded grouped fix across existing controller/API/pages.

## Likely Files
- `hmall-admin/src/views/OrderList.vue`
- `hmall-admin/src/views/OrderDetail.vue`
- `hmall-admin/src/api/order.js`
- `hmall-admin/src/utils/adminOrderActions.js`
- `trade-service/src/main/java/com/hmall/controller/OrderController.java`
- `trade-service/src/test/java/com/hmall/controller/OrderControllerAdminTest.java`
- `docs/knowledge-base/modules/hmall-admin.md`
- `docs/knowledge-base/modules/trade-service.md`
- `docs/knowledge-base/flows/order-checkout-flow.md`

## Runtime Evidence To Inspect First
- Issue #140: admin order list fake/unwired export, batch ship, refund audit, user filter, and unsafe visible cancel action.
- Issue #141: admin order detail fake/unwired print/export/address/refund actions, hardcoded logistics, and `payType` display mismatch.
- Existing backend endpoints: `/admin/orders`, `/admin/orders/export`, `/admin/orders/{id}/ship`, `/admin/orders/{id}/refund-audit`, `/orders/{id}/logistics`.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- Do not change existing order status semantics without a coordinated frontend/backend migration.
- Keep the task active after merge; do not archive in this PR.

## Worktree Or Branch
- `task/2026-06-06-admin-orders`

## Open Questions
- none
