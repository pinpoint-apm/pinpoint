# Kafka 4.x plugin testweb

Spring Boot 4 app that exercises the Kafka plugin through the plain `kafka-clients` API.

- `org.apache.kafka:kafka-clients` 4.4.0 (`testweb.kafka-clients.version` in the module pom, newer than the Spring Boot BOM)

## Prerequirements
- JDK 17 (kafka-clients 4.x needs Java 11+, Spring Boot 4 needs 17).
- A Kafka broker on `localhost:9092` (override with `-Dkafka.broker.url=host:port`):
```
$ docker run -d --rm --name kafka-testweb -p 9092:9092 apache/kafka:4.2.2
```

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/kafka-4-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-4-plugin-testweb spring-boot:start
```
Call them in this order:
- `KafkaProducer.send` to `kafka-it-topic`: http://localhost:18080/producer
- `KafkaConsumer.poll`, then the consumer entry point for each record: http://localhost:18080/consumer
  (returns `consumed N`; it waits up to 15 seconds for the first batch)

`TestConsumerRecordEntryPoint.consumeRecord` is the consumer entry point, registered with
`profiler.kafka.consumer.entryPoint` in the module pom.

## Run with another kafka-clients version
Override `testweb.kafka-clients.version` and rebuild the module in the same command:
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-4-plugin-testweb clean package spring-boot:start -Dmaven.test.skip=true -Dtestweb.kafka-clients.version=4.0.0
```
- Range: 4.0.0 to 4.4.0. For 0.11 - 3.x use [kafka-plugin-testweb](../kafka-plugin-testweb).
- The startup log prints `Kafka version: <version>` when the first producer or consumer is created.

## Verify
The app has no automated test. Call the endpoints and check the console of the terminal that ran `spring-boot:start`;
both the app output and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app output)
The entry point prints the `Pinpoint-*` headers of each record it receives:
```
Received record: message-00d82456-..., headers={Pinpoint-Flags=0, Pinpoint-SpanID=1548747705246206700, Pinpoint-TraceID=...^1795416217237^1, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=-8147061892561712626}
```
`headers={}` means the producer side did not inject them.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-4-plugin-testweb spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
`/producer` followed by `/consumer` flushes (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-8147061892561712626 ... SpanEvent{... serviceType=8660, endPoint='localhost:9092' ... nextSpanId=1548747705246206700, destinationId='localhost:9092' ...
BufferedStorage -- Flush Span{... parentSpanId=-8147061892561712626, spanId=1548747705246206700 ... remoteAddr='localhost:9092' ... acceptorHost='localhost:9092' ...
```
Check that
- the `/producer` request span has a `SpanEvent` with `serviceType=8660` (KAFKA_CLIENT) and the broker address in
  `endPoint` and `destinationId`;
- a consumer `Span` exists with the broker address in `remoteAddr` and `acceptorHost`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-4-plugin-testweb spring-boot:stop
$ docker stop kafka-testweb
```
