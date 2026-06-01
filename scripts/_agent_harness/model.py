from __future__ import annotations

import dataclasses
import enum
from pathlib import Path


class HarnessError(RuntimeError):
    pass


class TaskStatus(enum.Enum):
    CREATED = "created"
    IMPLEMENTING = "implementing"
    PR_OPEN = "pr-open"
    MERGED = "merged"
    DONE = "done"
    ABANDONED = "abandoned"


_STATUS_BY_VALUE = {status.value: status for status in TaskStatus}

_FIELDS = (
    "slug",
    "status",
    "base_branch",
    "task_branch",
    "remote_branch",
    "pull_request",
    "ci_status",
    "codex_review",
    "remote_cleanup",
    "spec",
    "plan",
    "spec_waiver",
    "plan_waiver",
    "pr_waiver",
)
_OPTIONAL = {"pull_request", "spec", "plan", "spec_waiver", "plan_waiver", "pr_waiver"}


@dataclasses.dataclass(frozen=True)
class TaskRecord:
    slug: str
    status: TaskStatus
    base_branch: str
    task_branch: str
    remote_branch: str
    pull_request: str | None
    ci_status: str
    codex_review: str
    remote_cleanup: str
    spec: str | None
    plan: str | None
    spec_waiver: str | None
    plan_waiver: str | None
    pr_waiver: str | None


def _parse_scalar(raw: str) -> str | None:
    value = raw.strip()
    if value.lower() in {"null", "~", ""}:
        return None
    if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
        return value[1:-1]
    return value


def _parse_yaml(text: str) -> dict[str, str | None]:
    """Minimal flat ``key: value`` YAML reader. No nesting, no lists."""
    result: dict[str, str | None] = {}
    for line_no, line in enumerate(text.splitlines(), start=1):
        stripped = line.split("#", 1)[0].rstrip()
        if not stripped:
            continue
        if ":" not in stripped:
            raise HarnessError(f"task.yaml line {line_no}: expected 'key: value'")
        key, _, raw = stripped.partition(":")
        result[key.strip()] = _parse_scalar(raw)
    return result


def read_task_yaml(path: Path) -> TaskRecord:
    if not path.exists():
        raise HarnessError(f"missing task.yaml: {path}")
    data = _parse_yaml(path.read_text(encoding="utf-8"))
    unknown = set(data) - set(_FIELDS)
    if unknown:
        raise HarnessError(f"task.yaml unknown keys: {sorted(unknown)}")
    missing = [f for f in _FIELDS if f not in data and f not in _OPTIONAL]
    if missing:
        raise HarnessError(f"task.yaml missing keys: {missing}")
    status_value = data["status"]
    if status_value not in _STATUS_BY_VALUE:
        raise HarnessError(f"task.yaml invalid status: {status_value!r}")
    return TaskRecord(
        slug=data["slug"] or "",
        status=_STATUS_BY_VALUE[status_value],
        base_branch=data.get("base_branch") or "main",
        task_branch=data.get("task_branch") or "",
        remote_branch=data.get("remote_branch") or "",
        pull_request=data.get("pull_request"),
        ci_status=data.get("ci_status") or "not-run",
        codex_review=data.get("codex_review") or "not-run",
        remote_cleanup=data.get("remote_cleanup") or "not-applicable",
        spec=data.get("spec"),
        plan=data.get("plan"),
        spec_waiver=data.get("spec_waiver"),
        plan_waiver=data.get("plan_waiver"),
        pr_waiver=data.get("pr_waiver"),
    )


def write_task_yaml(path: Path, record: TaskRecord) -> None:
    lines: list[str] = []
    for field in _FIELDS:
        value = getattr(record, field)
        if isinstance(value, TaskStatus):
            value = value.value
        lines.append(f"{field}: {'null' if value is None else value}")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")
