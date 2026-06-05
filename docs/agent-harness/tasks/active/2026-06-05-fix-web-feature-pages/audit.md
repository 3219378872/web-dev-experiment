# Audit Checklist

## Code Quality
- [x] No hardcoded secrets or credentials
- [x] All API calls follow existing axios pattern in `request.js`
- [x] Error handling with user-friendly messages
- [x] No console.log in production code (only `console.error` in catch blocks)
- [x] Consistent API naming: `uploadImage`, `getFaqs`, `getActiveSeckills`
- [x] CSS changes scoped with `scoped` attribute

## API Compatibility
- [x] New APIs (`uploadImage`, `getFaqs`, `getActiveSeckills`) match existing pattern
- [x] Existing endpoint signatures preserved (no breaking changes to existing API calls)
- [x] Image upload uses `multipart/form-data` content type
- [x] Sort parameters (`sortBy`, `isAsc`) use standard query params

## Frontend Patterns
- [x] No mutation of reactive state in loops (uses `.splice()`, `.push()` following existing patterns)
- [x] Template uses `v-for` with `:key` bindings
- [x] Async functions use try-catch with proper error handling
- [x] All CSS is scoped (no global style pollution)

## Security
- [x] No exposed API keys or tokens
- [x] File upload size validated (5MB limit)
- [x] File upload type restricted to `image/*`
