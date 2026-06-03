# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -q compile` | ✅ pass | Exit 0, no output (quiet mode) |
| `python3 scripts/knowledge_base.py check` | ✅ pass | "knowledge base check passed (K005 skipped: no --base)" |
| `python3 scripts/agent_harness.py check` | ✅ pass | "agent harness check passed" |
| `python3 scripts/engineering-lint.py` | ✅ pass | "engineering-lint: all checks passed" |
| `diff CLAUDE.md AGENTS.md` | ✅ pass | Exit 0, files byte-identical |
| `grep -r "hm-service" --include="*.java" --include="*.xml" --include="*.yaml"` | ✅ pass | No references remain in code/config |
