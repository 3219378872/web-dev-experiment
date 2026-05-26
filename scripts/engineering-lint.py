#!/usr/bin/env python3
"""Engineering-lint: validates md references and harness compliance."""

import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS_DIR = ROOT / "docs"

ACTIVE_DIRS = [
    DOCS_DIR / "agent-harness" / "tasks" / "active",
]


def is_active_file(path: Path) -> bool:
    try:
        path.resolve().relative_to(ROOT)
    except ValueError:
        return False
    for ad in ACTIVE_DIRS:
        try:
            path.resolve().relative_to(ad.resolve())
            return True
        except ValueError:
            continue
    return False


def check_md_file_links():
    errors = []
    ref_patterns = [
        re.compile(r"\[([^\]]+)\]\(([^)]+)\)"),
        re.compile(
            r"`([a-zA-Z0-9_\-\./]+/(?:[a-zA-Z0-9_\-\.]+\.)(?:md|py|yaml|yml|json|ts|js|toml|java|vue))`"
        ),
    ]
    for md_file in DOCS_DIR.rglob("*.md"):
        if not is_active_file(md_file):
            continue
        rel_path = md_file.relative_to(ROOT)
        content = md_file.read_text(encoding="utf-8", errors="ignore")
        lines = content.split("\n")
        for lineno, line in enumerate(lines, 1):
            for pattern in ref_patterns:
                for m in pattern.finditer(line):
                    ref = m.group(2) if m.lastindex and m.lastindex >= 2 else m.group(1)
                    if not ref:
                        continue
                    if ref.startswith(("http://", "https://", "#", "mailto:")):
                        continue
                    rel_to_file = (md_file.parent / ref).resolve()
                    rel_to_root = (ROOT / ref).resolve()
                    if not (rel_to_file.exists() or rel_to_root.exists()):
                        errors.append(
                            f"[MD-REF] {rel_path}:{lineno}: referenced path does not exist: {ref}"
                        )
    return errors


def check_claude_agents_in_sync():
    claude = ROOT / "CLAUDE.md"
    agents = ROOT / "AGENTS.md"
    if not (claude.exists() and agents.exists()):
        return []
    if claude.read_bytes() == agents.read_bytes():
        return []
    return [
        "[CLAUDE-AGENTS] CLAUDE.md and AGENTS.md have drifted; keep them byte-identical."
    ]


def main():
    errors = []
    errors.extend(check_md_file_links())
    errors.extend(check_claude_agents_in_sync())
    harness_check = os.system(
        f"cd {ROOT} && python3 scripts/agent_harness.py check 2>/dev/null"
    )
    kb_check = os.system(
        f"cd {ROOT} && python3 scripts/knowledge_base.py check 2>/dev/null"
    )
    if errors:
        print("\n".join(errors))
        print(f"\n{len(errors)} engineering-lint error(s) found")
    exit_code = 1 if errors or harness_check != 0 or kb_check != 0 else 0
    if exit_code == 0:
        print("engineering-lint: all checks passed")
    sys.exit(exit_code)


if __name__ == "__main__":
    main()
