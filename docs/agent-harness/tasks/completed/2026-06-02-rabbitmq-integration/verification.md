# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -B -ntp -q -pl hm-common -Dtest=RabbitMqMessagePublisherTest test` | pass | Exit 0 on 2026-06-02 after codex-review reliability fix; tests cover synchronous publish exception, confirm nack, and return fallback outbox writes. |
| `mvn -B -ntp -q -pl hm-common,trade-service,cart-service,notify-service -am -Dtest=RabbitMqMessagePublisherTest,MqConsumerSupportTest,OrderServiceImplTest,OrderCreatedListenerTest,OrderEventListenerTest -DfailIfNoTests=false test` | pass | Exit 0 on 2026-06-02 after second codex-review fixes; covers after-commit publish, bounded consumer retry, payment-success idempotency, and listener wiring. |
| `DOCKER_HOST=unix:///var/run/docker.sock SPRING_DATASOURCE_URL='jdbc:mysql://172.17.0.1:3306/hm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 SPRING_REDIS_HOST=172.17.0.1 SPRING_REDIS_PORT=6379 mvn -B -ntp -q -pl pay-service -am -Dtest=PayOrderServiceImplIT -DfailIfNoTests=false test` | pass | Exit 0 on 2026-06-02 after second codex-review fixes; verifies pay-service publishes after transaction commit in the existing transactional IT. |
| `mvn -B -ntp -q verify` | pass | Exit 0 on 2026-06-02 after second codex-review fixes and KB/harness updates. Existing Nacos connection noise appeared in some tests but Maven exited 0. |
| `mvn -B -ntp -q -pl trade-service,pay-service,cart-service,notify-service -am test -DskipITs` | pass | Exit 0 on 2026-06-02 after manual ack and cleanup edits; unit tests use mocked `RabbitTemplate` and no live broker. |
| `mvn -B -ntp -q -pl trade-service -am -Pintegration verify -DskipUnitTests=true -Dit.test=RabbitMqOrderEventIT -DfailIfNoTests=false` | pass | Exit 0 on 2026-06-02; Testcontainers RabbitMQ delivered `pay.success` and trade listener updated order status. |
| `python3 scripts/knowledge_base.py check` | pass | Exit 0 on 2026-06-02 after KB updates; K005 skipped because no `--base` argument. |
| `mvn -B -ntp -q test` | pass | Exit 0 on 2026-06-02 after final notify IT datasource hardening. Nacos connection noise appeared in existing gateway/notify tests but Maven exited 0. |
| `DOCKER_HOST=unix:///var/run/docker.sock SPRING_DATASOURCE_URL='jdbc:mysql://172.17.0.1:3306/hm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai' SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 SPRING_REDIS_HOST=172.17.0.1 SPRING_REDIS_PORT=6379 mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | pass | Exit 0 on 2026-06-02 after second codex-review fixes. Local Docker port publishing timed out from the JVM on `127.0.0.1`, so the local run used the Docker bridge host. CI still uses `127.0.0.1`. |
| `mvn -B -ntp -q -pl notify-service -Pintegration verify -DskipUnitTests=true` with CI-style datasource env | pass | Exit 0 on 2026-06-02 after `NotificationServiceImplIT` pinned its H2 datasource in test properties. |
| `python3 scripts/agent_harness.py check` | pass | Exit 0 on 2026-06-02 after second codex-review documentation updates. |
| `python3 scripts/knowledge_base.py check --base origin/main` | pass | Exit 0 on 2026-06-02 after KB pages were updated for changed tracks. |
| `python3 scripts/engineering-lint.py` | pass | Exit 0 on 2026-06-02 after second codex-review documentation updates; also ran harness and KB checks internally. |
| `docker compose config -q` | pass | Exit 0 on 2026-06-02 after RabbitMQ service changes. |
