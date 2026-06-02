# Verification

## RED phase
- [x] mvn verify fails on user-service (0% service.impl BRANCH)
- [x] mvn verify passes on item-service (100%)
- [x] mvn verify passes on cart-service (91.7%)
- [x] mvn verify passes on file-service (75%)
- [x] mvn verify passes on trade-service (76.2%)
- [x] notify-service excluded from check

## GREEN phase
- [x] mvn verify all-green with ratchet overrides (12/12 modules)
- [x] cart-service threshold 0.95 confirms gate is real (fails)
- [x] cart-service back to parent 0.70 confirms recovery (passes)

## Pattern calibration
- [x] JaCoCo PACKAGE element uses Java dotted names (com.hmall.service.impl), not VM slashes
- [x] `*.*.service.impl` matches 4-segment flat convention
- [x] `*.*.*.service.impl` matches 5-segment module convention
- [x] `*.*.notify.service.impl` exclude works for notify-service

## CI dry-run
- [x] mvn -B -ntp -q verify exits 0
- [x] CI YAML change verified (test job → mvn verify)
