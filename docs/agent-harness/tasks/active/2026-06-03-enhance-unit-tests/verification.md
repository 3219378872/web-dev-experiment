# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -pl user-service verify` | BUILD SUCCESS | JaCoCo 分支覆盖率 97%，门控 0.70 通过 |
| `mvn -B -ntp -pl pay-service verify` | BUILD SUCCESS | JaCoCo 分支覆盖率 91%，门控 0.70 通过 |
| `mvn -B -ntp verify` | BUILD SUCCESS | 全模块构建通过 |
