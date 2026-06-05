# Handoff: Issue 113 Address Zip Tag

## Status
In progress.

## Files Changed
- `hmall-web/src/views/AddressList.vue` — removed zipCode and tag fields from form and list display

## Commands Run
- `python3 scripts/agent_harness.py new issue-113-address-zip-tag`

## Known Risks
- Frontend may have cached old form state; users need to refresh page.

## Next Action
Implement fix in AddressList.vue, run tests, commit and push.

## Worktree Or Branch
- `task/2026-06-05-issue-113-address-zip-tag`
