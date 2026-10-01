# Monday cheat sheet

Interview: Monday 5 Oct 2026, 12:00 SGT, Teams, interviewer Hari Babu Dammala.
Join 10 minutes early on a personal laptop.

## 60-second intro

I am Oliver, a senior full-stack engineer based in KL, 7 years, currently at Maybank. I have shipped enterprise portals and an internal RAG chatbot on Azure OpenAI and AI Search, and I have done backend work in Spring Boot and Kotlin. I am looking at this role because it is core banking, Singapore, and event-driven. My strongest match is API and service delivery, Azure, and working with a business stream. The stack I would ramp fastest on here is Kafka on top of Spring Boot, which I have been building a local lab for.

## Stack they wrote down

Must: Java, Spring Boot, microservices, Kafka, React, REST, SQL/NoSQL, Azure, Docker, Kubernetes, CI/CD, Agile.
Nice: using an AI coding assistant. You can mention you use one, and that you still review the diff.

## Kafka in one minute

Kafka is a distributed log. We publish payment facts to a topic, keyed by account id so one account stays ordered on one partition. Other services read the same topic in their own consumer group. Delivery is at-least-once, so the consumer is idempotent on payment id. Failures go to a dead-letter topic so one bad message does not stall the partition. Production would be 3 brokers, replication factor 3, acks=all.

## If they go practical

- Walk `POST /payments` in this repo: controller, template send with key, listener, log partition and offset.
- Why the key is accountId.
- What changes if the listener throws.
- How you would add a fraud consumer without stealing messages from the posting consumer (second group).

## Questions to ask them

- Is this staffed on a named bank programme, and is the team product-aligned or a rotating bench?
- Is Kafka Confluent Cloud, self-managed, or Azure Event Hubs Kafka endpoint?
- What does the first 90 days look like, and is there a pairing engineer on the core stream?
- How are people staffed if the account winds down?
