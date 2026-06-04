# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -q -pl user-service -am test` | PASS | 62 tests, 0 failures；包含更新后的 `userNotFound_throws` 断言 |
