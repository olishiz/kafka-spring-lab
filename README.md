# Kafka + Spring Boot banking lab

Weekend lab for the Luxoft Senior Full Stack (Java + React) interview.
Role context: core-banking stream, Java, Spring Boot, microservices, Kafka, REST, Azure.

You already know Java and Spring Boot via Kotlin. This repo is for the piece you have not used: Kafka, and how a Spring service publishes and consumes events.

## What you will have running

- One Kafka broker in KRaft mode (no ZooKeeper). Docker image `apache/kafka-native`.
- Kafka UI at http://localhost:8085 so you can see topics, partitions, and messages.
- A small Spring Boot app on http://localhost:8088 that publishes a payment event and consumes it.

## Prerequisites

- Docker Desktop running
- Java 17+
- Maven 3.9+ (`mvn -v`)

## Start Kafka

```bash
docker compose up -d
docker compose ps
```

Wait until `kafka` is healthy (about 15-30 seconds on first pull). Open http://localhost:8085.

## Start the Spring app

```bash
cd lab
mvn spring-boot:run
```

Publish a payment:

```bash
curl -s -X POST http://localhost:8088/payments \
  -H 'Content-Type: application/json' \
  -d '{"accountId":"ACC-1001","amount":250.00,"currency":"SGD","type":"TRANSFER"}'
```

You should see the consumer log a line like `consumed partition=0 offset=0 key=ACC-1001`.
Post the same account a few times, then a different account. In Kafka UI, open topic `payments` and notice:

- messages with the same `accountId` land on the same partition (the key decides the partition)
- offset increases by 1 each message on that partition

## Console producer and consumer (no Spring)

This is the fastest way to see Kafka itself, before the framework.

```bash
# terminal 1: consume from the beginning
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic payments \
  --from-beginning

# terminal 2: produce a line
docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic payments
```

Type a line and press enter. The consumer prints it. Ctrl+C to stop.

## Exercises (do these in order)

1. Post 5 payments for `ACC-1001` and 5 for `ACC-2002`. In Kafka UI, confirm they are split across partitions, and that one account stays on one partition.
2. Stop the Spring app, post 3 more payments with curl (they will fail if the app is down... so instead produce with the console producer). Restart the app. Did the consumer replay old messages or only new ones? Change `auto-offset-reset` only matters when the group has no committed offset. After the first run, the group `payment-core` has a committed offset, so restart continues from the last commit.
3. Change `group-id` in `application.yml` to `payment-core-v2` and restart. This is a new consumer group, so it has no offset. With `earliest`, it replays the topic. That is the interview answer for "how do I reprocess?".
4. Read `docs/01-kafka-fundamentals.md` and answer the 12 questions at the bottom out loud.

## Stop

```bash
docker compose down
```

Guides:

- `docs/01-kafka-fundamentals.md`
- `docs/02-java-spring-bridge.md`
- `docs/03-interview-cheat-sheet.md`
