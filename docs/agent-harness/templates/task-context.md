# Context: {title}

## Objective
Describe the task outcome in one sentence.

## Scope
- In scope:
- Out of scope:

## Related Artifacts
- Spec: none - state the reason a separate spec is unnecessary.
- Plan: none - state the reason a separate plan is unnecessary.

## Likely Files
- `path/to/file`

## Runtime Evidence To Inspect First
- none

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.

## Branch And PR
- Base branch: `main`
- Task branch: `{task_branch}`
- Remote branch: `{remote_branch}`
- Pull request: none - create after local acceptance and push.

## Open Questions
- none
