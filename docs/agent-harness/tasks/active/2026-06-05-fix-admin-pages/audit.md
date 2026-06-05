---
status: active
slug: fix-admin-pages
title: Fix hmall-admin page issues
---

# Audit Checklist

## Code Quality
- [x] Immutable patterns used (no mutation of objects)
- [x] Error handling with try-catch and user-facing messages
- [x] Loading states for async operations
- [x] No hardcoded values (replaced with API data)
- [x] Functions are small and focused

## Frontend Standards
- [x] Component uses `<script setup>` syntax
- [x] API calls are in dedicated modules under `src/api/`
- [x] Consistent axios interceptor usage
- [x] el-pagination used for pagination
- [x] el-dialog for modals
- [x] el-empty for empty states

## Security
- [x] No hardcoded secrets
- [x] Token from localStorage used in axios interceptor
- [x] 401/403 handling in interceptor

## Backward Compatibility
- [x] No changes to API endpoint signatures
- [x] New API files don't break existing imports
- [x] Dashboard no longer shows demo data when API fails
