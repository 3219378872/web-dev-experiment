# Context: JaCoCo Service Layer Branch Coverage Gate

**Task ID:** 2026-06-01-service-branch-coverage-gate
**Status:** active
**Spec:** docs/superpowers/specs/2026-06-01-service-branch-coverage-gate-design.md
**Plan:** docs/superpowers/plans/2026-06-02-service-branch-coverage-gate.md

## Problem
JaCoCo only configured in `integration` profile which skips unit tests;
existing check rule (BUNDLE/INSTRUCTION 0.10) has `haltOnFailure=false`,
serving as no-op gate.

## Solution
- Move JaCoCo to main build with PACKAGE-level BRANCH COVEREDRATIO 0.70 gate
- JaCoCo PACKAGE includes match Java-style dotted package names: `*.*.service.impl` + `*.*.*.service.impl`
- Ratchet overrides (0.00) for user/pay/hm-service via `combine.self="override"`
- CI test job runs `mvn verify` instead of `mvn test`
- notify-service excluded (0 branches, no gate)
- hm-service spring-boot-maven-plugin skipped (main class commented out)
