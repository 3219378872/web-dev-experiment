# Handoff: Rabbitmq Integration

## Status
Done. Branch `task/2026-06-02-rabbitmq-integration` contains RabbitMQ implementation, focused unit tests, focused RabbitMQ integration test, compose/schema changes, and KB updates. Local verification is green; PR loop remains.

## Files Changed
- `docs/superpowers/plans/2026-06-02-rabbitmq-integration.md`
- `docs/agent-harness/tasks/active/2026-06-02-rabbitmq-integration/*`
- `hm-common/src/main/java/com/hmall/common/mq/**`
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- `cart-service/src/main/java/com/hmall/cart/mq/OrderCreatedListener.java`
- `notify-service/src/main/java/com/hmall/notify/mq/OrderEventListener.java`
- `docker-compose.yml`
- `docs/sql/init-all-tables.sql`
- RabbitMQ-related tests and KB pages.

## Commands Run
- `git switch -c task/2026-06-02-rabbitmq-integration`
- `python3 scripts/agent_harness.py new rabbitmq-integration`
- `mvn -B -ntp -q -pl trade-service,pay-service,cart-service,notify-service -am test -DskipITs` — pass after implementation and cleanup.
- `mvn -B -ntp -q -pl trade-service -am -Pintegration verify -DskipUnitTests=true -Dit.test=RabbitMqOrderEventIT -DfailIfNoTests=false` — pass with Testcontainers RabbitMQ.
- `python3 scripts/knowledge_base.py check` — pass, K005 skipped.
- `mvn -B -ntp -q test` — pass before final notify IT datasource hardening.
- `mvn -B -ntp -q -pl notify-service -Pintegration verify -DskipUnitTests=true` with CI-style global datasource env — pass after `NotificationServiceImplIT` pins its H2 datasource.
- `DOCKER_HOST=unix:///var/run/docker.sock SPRING_DATASOURCE_URL='jdbc:mysql://172.17.0.1:3306/hm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 SPRING_REDIS_HOST=172.17.0.1 SPRING_REDIS_PORT=6379 mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` — pass.
- `python3 scripts/agent_harness.py check` — pass.
- `python3 scripts/knowledge_base.py check` — pass, K005 skipped.
- `python3 scripts/engineering-lint.py` — pass.
- `docker compose config -q` — pass.
- `mvn -B -ntp -q test` — pass after final notify IT datasource hardening.

## Known Risks
- Reliable producer fallback is a lightweight `mq_outbox_message` insert for synchronous publish exceptions, publisher confirm nacks, and returned unroutable messages; no replay scheduler is included in this slice.
- RabbitMQ IT logs include Testcontainers/Docker connection reset/refused during shutdown, but Maven exited 0.
- Local Docker port publishing timed out for JVM MySQL connections to `127.0.0.1`; local full integration used Docker bridge host `172.17.0.1`. The GitHub Actions service-container workflow still uses `127.0.0.1`.

## Next Action
Commit, push, open PR, and follow CI/codex-review to green.

## Worktree Or Branch
- `task/2026-06-02-rabbitmq-integration`
