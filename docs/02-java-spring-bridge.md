# Java and Spring Boot, from Kotlin

You do not need a Java course. The interviewer will still probe Java-the-language, because the req says 8-10 years of Java. These are the differences that show up in a senior screen.

## Language

- A Kotlin `data class` is a Java `record` (Java 16+). Both are immutable carriers. Use a record for the Kafka event.
- Kotlin null-safety is not in Java. Interview answer: validate at the boundary, use `Optional` for return values that may be missing, do not return null from new code.
- Checked exceptions exist. `IOException` must be caught or declared. Spring usually wraps them.
- `==` on objects is reference equality. Use `.equals`, or `Objects.equals`. Strings are interned sometimes, so `==` looks like it works and then fails. Say you always use `.equals` for value equality.
- Streams are pull-based pipelines (`list.stream().filter(...).map(...).toList()`). Fine for in-memory. Not how Kafka consumers work.

## Spring, same as you know

- `@SpringBootApplication` starts the context.
- `@RestController` + `@PostMapping` is the HTTP edge.
- `@Service` is a bean. Constructor injection, not field `@Autowired`.
- `@KafkaListener(topics = "payments", groupId = "payment-core")` is the consumer. The container manages the poll loop. You do not write a `while (true) poll()` in business code.
- `KafkaTemplate.send(topic, key, value)` is the producer. It returns a `CompletableFuture`. In the lab we block with `.get()` so the HTTP call fails if the broker did not ack. In production you would handle the future and not tie up the servlet thread, or use a transactional outbox.

## Two phrases worth having

**Transactional outbox.** The service writes the business row and the outbound event in the same database transaction. A relay publishes the event to Kafka. You never have "DB committed, Kafka publish failed" or the reverse. This is the senior answer when they ask how you keep the ledger and the topic in sync.

**Idempotent consumer.** The handler checks `paymentId` in the DB unique key before posting. A redelivered Kafka record is a no-op. This is how at-least-once becomes safe.

## What they may ask that is not Kafka

- Spring Boot vs Spring. Boot is the opinionated starter and embedded server. Spring is the container and the programming model.
- `@Transactional` on a public method, same-class calls do not go through the proxy, so the annotation is skipped. Rollback default is runtime exceptions.
- Microservices: one deployable per business capability, sync REST for queries and commands that need an immediate answer, Kafka for facts other services must react to.
- Azure: you have real Azure AI Search / OpenAI / migration stories. If they ask cloud, talk containers, CI/CD, Key Vault, and managed identity. Do not invent AKS production ownership you did not have. Kafka on Azure is often HDInsight, Confluent Cloud, or Event Hubs Kafka endpoint. You can say you have not run that control plane, and describe the app side.
- React: they listed it. A senior full-stack answer is component boundaries, data fetching, and how the UI calls the REST API that produces the event. Do not pretend to be a React specialist if the screen goes deep; offer the API contract instead.

## Honest gap to state if asked

"My production depth is full-stack delivery, Spring-style services, and Azure. Kafka I have learned from the model: topic, key, partition, consumer group, at-least-once plus idempotent handler. I have a local lab, not a production cluster."

That is stronger than bluffing a cluster you have not operated.
