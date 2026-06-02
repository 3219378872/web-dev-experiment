# Audit

## Security
- [x] No secrets/hardcoded values
- [x] No endpoint changes
- [x] No auth changes
- [x] No public API contract changes (R<T>/PageDTO<T> unchanged)

## Module boundaries
- [x] Only pom.xml + CI workflow modified
- [x] No Java source changes
- [x] Integration profile IT behavior preserved (jacoco.check.skip=true)
- [x] hm-service spring-boot-maven-plugin skip added (pre-existing issue)

## POM changes
- [x] JaCoCo version unchanged (0.8.10)
- [x] haltOnFailure=true on parent gate
- [x] BRANCH counter, PACKAGE element, COVEREDRATIO value
- [x] combine.self="override" on submodule ratchet overrides
- [x] notify-service explicitly excluded

## YAGNI compliance
- [x] No cross-module aggregate report
- [x] No merged unit+IT exec files
- [x] No business test supplementation for lagging modules
- [x] No endpoint/auth/Seata/Gateway route changes
