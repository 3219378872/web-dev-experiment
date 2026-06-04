# Handoff: Phase 4 — notify-service

## Status

Implementation complete. Pending: test execution + lint verification + commit.

## Deliverables

1. `GET /faqs` endpoint returning `List<FaqVO>` (active FAQs sorted by sort ASC, createTime DESC)
2. Full PO → Mapper → Service → Controller chain
3. SQL migration script at `docs/sql/notify-service-faq.sql`
4. Unit tests (6 test cases) targeting FaqServiceImpl
5. Knowledge base update for notify-service module
6. Customer message endpoints confirmed existing (no changes needed)

## Verification Steps

```bash
mvn -B -ntp -pl notify-service -am test
python3 scripts/engineering-lint.py
```

## Notes

- No existing endpoints were modified
- No new external dependencies
- Gateway routing for `/faqs` uses existing rules (no changes needed)
- FAQ table must be created in MySQL before deploying: `mysql -u root -p hmall < docs/sql/notify-service-faq.sql`
