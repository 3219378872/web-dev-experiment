# Handoff

## Branch
task/2026-06-01-service-branch-coverage-gate

## PR checklist
- [ ] CI: lint job passes
- [ ] CI: test job passes (mvn verify with coverage gate)
- [ ] CI: integration job passes
- [ ] CI: smoke job passes
- [ ] CI: codex-review passes (blocking findings: none)
- [ ] AGENTS.md ↔ CLAUDE.md byte-identical (unchanged)

## Ratchet modules (for future work)
| Module | Current | Target | Branches |
|--------|---------|--------|----------|
| user-service | 0.00 | 0.70 | 0/34 |
| pay-service | 0.00 | 0.70 | 0/12 |
| hm-service | 0.00 | 0.70 | 0/44 |

## KB pages bumped (K005)
- modules/user-service.md
- modules/pay-service.md
- modules/hm-service.md
- flows/auth-and-gateway-flow.md
- flows/order-checkout-flow.md

## Post-merge
- Delete remote branch `task/2026-06-01-service-branch-coverage-gate`
- Move harness task to `completed/`
- Bump ratchet thresholds as tests are added to lagging modules
