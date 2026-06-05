# Verification: Issue 113 Address Zip Tag

## Test Plan
1. Frontend build passes: `cd hmall-web && npm run build`
2. No zipCode or tag references remain in AddressList.vue
3. Address form displays correctly without zipCode/tag fields
4. Backend tests still pass: `mvn -q -pl user-service -am test`

## Test Results
- [ ] Frontend build passes
- [ ] No zipCode/tag references in AddressList.vue
- [ ] Backend tests pass
