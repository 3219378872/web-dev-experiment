# Context: Issue 113 Address Zip Tag

## Objective
Fix the AddressList.vue frontend form so that zipCode and tag fields are removed, aligning with the backend Address entity and front-spec.md requirements.

## Scope
- In scope:
  - Remove zipCode input field from AddressList.vue form
  - Remove tag selection chips from AddressList.vue form
  - Remove tag display from address list cards
  - Update form reset and edit logic to not reference zipCode/tag
  - Update test schema SQL if needed (not needed since fields never existed)
- Out of scope:
  - Adding zipCode/tag to backend entity/database (not required by spec)
  - Changing API response format

## Related Artifacts
- Spec: none - this is a simple frontend-backend alignment fix, no spec needed.
- Plan: none - single file change with clear scope.

## Likely Files
- `hmall-web/src/views/AddressList.vue`

## Runtime Evidence To Inspect First
- Front-spec.md section 2.6 lists only: 收货人、手机号、省市区、详细地址 for address module.
- Backend Address.java has no zipCode or tag fields.
- Backend addresses table has no zip_code or tag columns.

## Safety Constraints
- Do not commit secrets.
- Do not break public API response envelopes.

## Worktree Or Branch
- `task/2026-06-05-issue-113-address-zip-tag`

## Open Questions
- none
