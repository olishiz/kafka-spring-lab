# Kafka fundamentals (interview depth, not a book)

Kafka is a distributed commit log. Producers append records. Consumers read them at their own pace. It is not a queue that deletes a message when someone takes it.

That one sentence is the difference between Kafka and RabbitMQ. In a bank, many systems need the same fact ("this transfer was posted"): the ledger, notifications, fraud, statements. A queue gives the message to one worker. A log lets every interested system read it.

## The five nouns

**Broker.** A Kafka server. In this lab there is one. Production has 3+ so a disk or machine can die.

**Topic.** A named stream, like a table name. This lab uses `payments`. Core banking typically has topics such as `account-opened`, `payment-posted`, `balance-changed`.

**Partition.** A topic is split into ordered, append-only logs. Partition 0 is independent of partition 1. Ordering is guaranteed only inside one partition, not across the topic. This is the fact interviewers want.

**Offset.** The position of a record inside a partition. First record is offset 0, next is 1. The consumer remembers "I have read up to offset 41". Kafka does not track that for you unless the consumer commits it.

**Consumer group.** A named set of consumers sharing the work. Kafka assigns each partition to one consumer in the group. If you have 3 partitions and 2 consumers in group `payment-core`, one consumer gets 2 partitions and the other gets 1. A second group (`fraud-check`) gets its own copy of every message. Same topic, two independent readers.

```
Topic: payments   (3 partitions)

partition 0:  [m0][m1][m2][m3]  -> consumer A in group payment-core
partition 1:  [m0][m1]          -> consumer B in group payment-core
partition 2:  [m0][m1][m2]      -> consumer A in group payment-core

group fraud-check reads the same partitions from its own offsets
```

## Key = ordering key

The producer picks a key. Kafka hashes the key to a partition. Same key, same partition, so order is kept for that key.

In banking you key by `accountId` (or `customerId`). All transfers for ACC-1001 stay in order. Transfers for ACC-2002 can be on another partition and processed in parallel. You do not key by a random UUID if you care about per-account order.

## What a record looks like

- key (bytes) — account id
- value (bytes) — usually JSON or Avro
- timestamp
- headers (optional metadata, correlation id)
- partition + offset, assigned by the broker

Kafka does not understand your JSON. Serializers do. In the lab, Spring's `JsonSerializer` writes the value. Interview line: "the broker stores bytes; schema lives in the app or in a schema registry."

## Delivery semantics (say these cleanly)

**At-most-once.** Send or read, do not retry. Fast, can lose messages. Wrong for payments.

**At-least-once.** Retry until the broker acknowledges. A crash between processing and offset commit can deliver the same record twice. This is the default you should claim. The consumer must be idempotent: processing the same `paymentId` twice does not post the transfer twice. Store the payment id and ignore duplicates.

**Exactly-once.** Kafka can do this inside Kafka (idempotent producer + transactions) so a consume-transform-produce does not double-write. It does not magically make your database exactly-once. For a bank, the honest answer is: at-least-once on the topic, idempotent handler in the service, unique business key in the database.

Producer settings that match that story:

- `acks=all` — wait until the leader and in-sync replicas have the record
- `enable.idempotence=true` — retries do not create duplicate records on the broker
- `min.insync.replicas=2` in production with replication factor 3

This lab runs one broker, so replication factor is 1. Say that out loud if asked: "locally RF=1, in production I would use RF=3 and min ISR 2."

## Consumer offset commit

Spring Kafka commits the offset after the listener method returns successfully (default). If the method throws, the offset is not committed and the record is retried. After N failures, send it to a dead-letter topic (`payments.DLT`) and commit, so one poison message does not block the partition.

Blocking the partition is the production incident. Partition 0 is stuck on a bad message, every later payment for accounts on that partition waits. DLT plus alert is the answer.

## Retention, not deletion-on-read

Messages stay for a configured time (often 7 days) or size, even after every consumer has read them. A new service can be deployed next week and replay history, if retention still covers it. That is also why you do not put 20 MB documents in Kafka. Put the blob in storage, put the id in the event.

## How this maps to the Luxoft role

The posting is core banking, microservices, Kafka event-driven, REST, Azure.

A reasonable shape you can describe:

1. API receives `POST /payments` (synchronous, returns 202 after the command is accepted).
2. Service writes the intent to its own DB and publishes `PaymentRequested` keyed by account id.
3. A posting service consumes, applies ledger rules, publishes `PaymentPosted` or `PaymentRejected`.
4. Notification and statement services consume `PaymentPosted` in their own consumer groups.
5. Failures go to a DLT. Ops replay after the bug is fixed.

You do not need to have built this. You need to walk it without hand-waving the partition key and the duplicate.

## 12 questions to answer out loud

1. Why a log, not a queue, for a payment posted event?
2. What does a consumer group give you that a single consumer does not?
3. Why is ordering only per partition?
4. What key would you use for a funds transfer, and why not a random UUID?
5. What happens if you have more consumers in the group than partitions?
6. What is an offset, and who stores the consumer's progress?
7. What is at-least-once, and how do you stop a double post?
8. What does `acks=all` wait for?
9. What is a dead-letter topic for?
10. Can two consumer groups read the same topic? What does each remember?
11. Why not put the full statement PDF on the topic?
12. What would you change between this one-broker lab and a production cluster?
