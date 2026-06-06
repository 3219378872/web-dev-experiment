# Context: Admin Catalog Media

## Objective
Fix the admin catalog/media issue group by removing unsupported UI affordances and wiring existing backend contracts for item, category, and banner management.

## Scope
- In scope:
  - #136: admin item list unsupported batch import and pending-review tab.
  - #137: admin item edit fields that the item backend does not persist.
  - #138: admin category management visibility of disabled categories and fake drag/image affordances.
  - #142: admin banner image upload/preview and sort controls.
- Out of scope:
  - Large product import pipelines, item audit workflow, rich product detail persistence, multi-image persistence, category image persistence, and drag-and-drop reorder APIs.
  - Harness archival after merge; user requested active records remain for later unified archival.

## Related Artifacts
- Spec: none - GitHub issues contain bounded bug reports against existing pages and APIs.
- Plan: none - changes are localized bug fixes tracked in this harness record.

## Likely Files
- `hmall-admin/src/views/ItemList.vue`
- `hmall-admin/src/views/ItemEdit.vue`
- `hmall-admin/src/views/CategoryList.vue`
- `hmall-admin/src/views/BannerList.vue`
- `hmall-admin/src/api/item.js`
- `hmall-admin/src/utils/adminCatalogActions.js`
- `item-service/src/main/java/com/hmall/item/controller/CategoryController.java`
- `item-service/src/test/java/com/hmall/item/controller/CategoryControllerAdminTest.java`

## Runtime Evidence To Inspect First
- Backend contracts in `ItemController`, `CategoryController`, and `BannerController`.
- Frontend API wrappers in `hmall-admin/src/api/item.js` and upload wrapper in `hmall-admin/src/api/upload.js`.

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files.
- Do not change existing endpoint response envelopes without coordinated frontend migration.
- Keep `/categories` customer-facing behavior enabled-only; add separate admin read endpoint for disabled-category lifecycle management.

## Worktree Or Branch
- `task/2026-06-06-admin-catalog-media`

## Open Questions
- none
