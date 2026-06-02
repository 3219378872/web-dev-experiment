# Handoff: Rabbitmq Integration

## Status
PR follow-up in progress. Branch `task/2026-06-02-rabbitmq-integration` contains RabbitMQ implementation, focused unit tests, focused RabbitMQ integration test, compose/schema changes, KB updates, and codex-review follow-up fixes for producer transaction boundaries, consumer retries, and payment-success idempotency. Local final verification after the latest edits is green; PR CI/review remains.

## Files Changed
- `docs/superpowers/plans/2026-06-02-rabbitmq-integration.md`
- `docs/agent-harness/tasks/active/2026-06-02-rabbitmq-integration/*`
- `hm-common/src/main/java/com/hmall/common/mq/**`
- `hm-common/src/test/java/com/hmall/common/mq/**`
- `trade-service/src/main/java/com/hmall/service/impl/OrderServiceImpl.java`
- `pay-service/src/main/java/com/hmall/service/impl/PayOrderServiceImpl.java`
- `cart-service/src/main/java/com/hmall/cart/mq/OrderCreatedListener.java`
- `notify-service/src/main/java/com/hmall/notify/mq/OrderEventListener.java`
- `docker-compose.yml`
- `docs/sql/init-all-tables.sql`
- RabbitMQ-related tests and KB pages.
- Second codex-review follow-up:
  - producer publish is deferred to transaction `afterCommit`;
  - consumer failures retry through dedicated retry queues before dead-letter;
  - `pay.success` only updates orders still in pending-payment status.

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
- `mvn -B -ntp -q -pl hm-common,trade-service,cart-service,notify-service -am -Dtest=RabbitMqMessagePublisherTest,MqConsumerSupportTest,OrderServiceImplTest,OrderCreatedListenerTest,OrderEventListenerTest -DfailIfNoTests=false test` — pass after second codex-review fixes.
- `DOCKER_HOST=unix:///var/run/docker.sock SPRING_DATASOURCE_URL='jdbc:mysql://172.17.0.1:3306/hm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 SPRING_REDIS_HOST=172.17.0.1 SPRING_REDIS_PORT=6379 mvn -B -ntp -q -pl pay-service -am -Dtest=PayOrderServiceImplIT -DfailIfNoTests=false test` — pass after second codex-review fixes.
- `mvn -B -ntp -q verify` — pass after second codex-review fixes.
- `DOCKER_HOST=unix:///var/run/docker.sock SPRING_DATASOURCE_URL='jdbc:mysql://172.17.0.1:3306/hm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 SPRING_REDIS_HOST=172.17.0.1 SPRING_REDIS_PORT=6379 mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` — pass after second codex-review fixes.
- `python3 scripts/agent_harness.py check` — pass after second codex-review documentation updates.
- `python3 scripts/knowledge_base.py check --base origin/main` — pass after second codex-review documentation updates.
- `python3 scripts/engineering-lint.py` — pass after second codex-review documentation updates.

## Known Risks
- Reliable producer fallback is a lightweight `mq_outbox_message` insert for synchronous publish exceptions, publisher confirm nacks, and returned unroutable messages; no replay scheduler is included in this slice.
- Producer publish now waits for transaction commit. If broker publish then fails, fallback outbox insert happens after commit and does not roll back the already-committed business transaction.
- Consumer retry topology is bounded: failed messages are delayed in per-consumer retry queues and moved to `hmall.mq.dead.queue` after max retries.
- RabbitMQ IT logs include Testcontainers/Docker connection reset/refused during shutdown, but Maven exited 0.
- Local Docker port publishing timed out for JVM MySQL connections to `127.0.0.1`; local full integration used Docker bridge host `172.17.0.1`. The GitHub Actions service-container workflow still uses `127.0.0.1`.

## Next Action
Commit, push, open PR, and follow CI/codex-review to green.

## Worktree Or Branch
- `task/2026-06-02-rabbitmq-integration`
