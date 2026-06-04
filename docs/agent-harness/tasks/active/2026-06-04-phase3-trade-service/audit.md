# Audit: Phase 3 — trade-service

## Convention Compliance

- Write operations return `R<Void>`: refundAudit ✓, export is CSV (non-JSON) ✓
- Read operations return `*VO`/`List<*>`: all read endpoints ✓
- Cross-service calls via hm-api Feign: UserClient.countNewUsers ✓
- Gateway /admin/** routes use existing AuthGlobalFilter: all admin endpoints under /admin/ ✓
- UserContext (ThreadLocal) for current user ID: freight and available coupons use UserContext ✓
- New tables use SQL migration scripts: logistics_trace in docs/sql/ ✓
- Logical delete + timestamps: logistics_trace follows existing PO patterns ✓

## Security

- No hardcoded secrets
- Admin endpoints rely on Gateway auth (no bypass)
- Export CSV does not leak sensitive data (no passwords, no tokens)
- UserContext used correctly for user-scoped queries

## Immutability

- Service methods create new objects, do not mutate inputs
- DashboardServiceImpl builds new VO objects for each response
- OrderServiceImpl.refundAudit creates new state via lambdaUpdate

## File Organization

- Each new class in its own file
- Files under 800 lines
- Follows existing package structure (po/, vo/, mapper/, service/, controller/)
