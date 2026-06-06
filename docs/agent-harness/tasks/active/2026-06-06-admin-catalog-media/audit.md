# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| #136 removes unsupported import and pending-review item actions | done locally | `ItemList.vue` no longer renders batch import; tabs come from `catalogItemTabs` and contain only all/on/off/low. |
| #137 limits item edit payload to persisted backend fields | done locally | `ItemEdit.vue` uses `buildItemPayload`; helper test asserts unsupported fields are excluded and price/stock/image/spec are normalized. |
| #138 supports disabled category lifecycle management without fake affordances | done locally | Added `GET /admin/categories` returning all categories while preserving `/categories` enabled-only behavior; UI uses the admin endpoint and removed image/drag/expand-only controls. |
| #142 wires banner media and sort to real APIs | done locally | `BannerList.vue` uploads through `uploadImage`, previews `imageUrl`, and calls `/admin/banners/{id}/sort` through up/down controls. |
| Avoid backend contract overreach | done locally | Did not add import/audit/rich-detail/multi-image/category-image APIs outside the issue scope. |
| Harness archival | intentionally deferred | User requested no archival now; task remains in `active` with `status: active` for later unified archival. |
