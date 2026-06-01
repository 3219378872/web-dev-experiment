# Verification

## 检查清单

- [x] `git revert` 4 个合并提交成功
- [x] spec 和 plan 文件已恢复（cherry-pick）
- [x] KB 页面已更新到 post-revert 状态
- [x] `python3 scripts/agent_harness.py check` 通过
- [x] `python3 scripts/engineering-lint.py` 通过
- [x] `mvn -q test` 全仓库测试通过（94 tests, 0 failures）
- [x] CI: lint ✅ test ✅ integration ✅ smoke ✅
- [ ] CI: codex-review 通过

## 回滚前 main HEAD

1f53b9d Merge pull request #27

## 回滚后 main HEAD（目标）

5cb0f2b（revert #29）→ 37f1650（revert #30）→ db6a112（revert #28）→ e4b639a（revert #31）
→ f0e48ae（restore spec）→ bd04dd7（restore plan）→ bb91223（fix KB）
