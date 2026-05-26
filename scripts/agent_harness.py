"""Thin shim for the hmall agent harness CLI."""

from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from _agent_harness.cli import main  # noqa: E402

if __name__ == "__main__":
    raise SystemExit(main())
