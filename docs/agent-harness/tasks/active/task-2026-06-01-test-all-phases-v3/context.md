# Context

Consolidated PR covering all 4 phases of test coverage supplementation:

- Phase 1: cart-service (7) + trade-service (18) + item-service (5) = 30 tests
- Phase 2: notify-service (6) + file-service (4) = 10 tests
- Phase 3: hm-api DefaultFeignConfig (3 tests)
- Coverage boost: user-service JwtTool (3) + item-service ItemServiceImpl (2) + hm-common PageDTO (3) = 8 tests

Total: 51 new tests across 6 modules. Business-logic coverage: 80.5%.

All 4 original PRs (#28/#29/#30/#31) were prematurely merged before codex-review passed,
then rolled back in PR #32. This single consolidated PR replaces all of them.

## Spec Note
The original spec had aspirational test estimates (~150-182 tests). The actual
codebase has limited testable logic: multiple ServiceImpl are empty shells,
no Fallback implementations exist, no RabbitMQ/MinIO SDK code. All 51 tests
cover every class with business logic across all 6 previously-zero-test modules.
Business-logic coverage: 80.5%.