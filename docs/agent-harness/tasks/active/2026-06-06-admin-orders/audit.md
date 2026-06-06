# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Issue #140 export uses backend and current filters | done | `OrderList.vue` calls `exportOrders(currentQueryParams(..., false))`; `order.js` maps `/admin/orders/export`; backend export accepts `orderId/userId/status`. |
| Issue #140 user filter contract exists end to end | done | `OrderList.vue` adds user ID filter; `adminOrderQueryParams` includes `userId`; `OrderController.adminList/exportOrders` filter `Order::getUserId`; `OrderControllerAdminTest` covers list and export. |
| Issue #140 batch shipping reaches backend | done | `OrderList.vue` collects checked `status=2` rows and calls existing `shipOrder` for each selected order with the entered tracking number. |
| Issue #140 refund audit visible for refund status only | done | `canRequestRefundAudit` gates actions to `status=6`; list actions call `/admin/orders/{id}/refund-audit` with `{ approved, reason }`. |
| Issue #140 unsafe visible actions limited by status | done | `canShipOrder` gates ship to `status=2`; `canCancelOrder` gates cancel to `status=1/2`; unsupported statuses no longer show cancel. |
| Issue #141 detail logistics is not hardcoded | done | `OrderDetail.vue` loads `getOrderLogistics(route.params.id)` and renders mapped `LogisticsTraceVO` rows or an empty state. |
| Issue #141 detail payment display uses backend field | done | `OrderDetail.vue` renders `paymentTypeText(order?.paymentType)`; utility test covers values 1/2/3. |
| Issue #141 fake/unwired detail actions handled | done | Print calls `window.print()`, export calls `/admin/orders/export?orderId=...`, refund status shows audit actions, and unsupported modify-address action is hidden. |
| No separate spec required | not applicable | Issues #140/#141 are bounded frontend audit bug reports; `task.yaml.spec_waiver` records the reason. |
| No separate plan required | not applicable | The grouped fix is small and tracked by this context/audit/verification record; `task.yaml.plan_waiver` records the reason. |
