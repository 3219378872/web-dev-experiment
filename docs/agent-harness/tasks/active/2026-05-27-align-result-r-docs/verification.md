# Verification

## Local Checks

| Command | Result | Evidence |
| --- | --- | --- |
| `cmp CLAUDE.md AGENTS.md` | passed | byte equal（镜像不变） |
| `python3 scripts/agent_harness.py check` | passed | `agent harness check passed` |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | `knowledge base check passed` |
| `python3 scripts/engineering-lint.py` | passed | `engineering-lint: all checks passed` |
| `mvn -B -ntp test` (pre-commit baseline) | passed | 8 modules, 80 tests, 0 failures |

## Evidence Table

| 文件 | 旧文本 | 新文本要点 |
| --- | --- | --- |
| `CLAUDE.md:88-89` | `统一 Result/PageDTO 响应封装 ... 任何接口都必须用 Result<T> 包装` | 改为 `R<T>/PageDTO<T>` + 写返回 `R<Void>`、读返回 `PageDTO<T>`/`*VO`/`*DTO` |
| `CLAUDE.md:176` | `API 必须用 Result<T> 封装，不允许裸返 entity` | 写用 `R<Void>`、读可裸返 DTO/VO，但禁止裸返数据库 entity |
| `AGENTS.md` | 同 CLAUDE.md | 同上（字节镜像） |
| `docs/agent-harness/quality-rules.md:123` | `走 hm-common.dto.Result<T>... 不允许裸返 entity` | `hm-common.domain.R<T>` + read/write 拆分约定 |

## What This Does NOT Change

- 任何 Java 源代码（无 controller / service / DTO 改动）
- `hm-common.domain.R` / `PageDTO` 字段或行为
- 前端 axios 拦截器或调用点

## How to Reverify

```bash
git diff main -- CLAUDE.md AGENTS.md docs/agent-harness/quality-rules.md
cmp CLAUDE.md AGENTS.md
python3 scripts/agent_harness.py check
python3 scripts/knowledge_base.py check --base origin/main
python3 scripts/engineering-lint.py
```
