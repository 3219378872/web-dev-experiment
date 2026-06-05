# Handoff

## Branch
`task/2026-06-05-fix-web-feature-pages` (based on `origin/main`)

## Commit
After verification, commit with:
```
fix(hmall-web): resolve feature page issues #105 #106 #107 #110 #111

- login: remove unimplemented SMS/social/captcha features, fix sendCode params (#105)
- feedback/service: add image upload, FAQ loading, clean up static data (#106)
- item-detail: add review image upload support (#107)
- flashsale: integrate /seckill/active API, real time slots and prices (#110)
- search: send sortBy/isAsc to backend, remove client-side sorting (#111)

Closes #105
Closes #106
Closes #107
Closes #110
Closes #111

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
```

## PR
Create PR against `main` with title:
"fix(hmall-web): resolve feature page issues #105 #106 #107 #110 #111"

## Post-Merge
- Delete remote branch `task/2026-06-05-fix-web-feature-pages`
- Move this task to `completed/` and set `task.yaml` status to `done`
