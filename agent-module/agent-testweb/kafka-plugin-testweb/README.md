# Kafka 0.11 - 3.x plugin testweb

Spring Boot 2 app that exercises the Kafka plugin through the plain `kafka-clients` API.
For kafka-clients 4.x see [kafka-4-plugin-testweb](../kafka-4-plugin-testweb).

- `org.apache.kafka:kafka-clients` 3.8.1 (`plugins-bom.kafka3.version`)
- Runs on any kafka-clients from 0.11.0.0 to 3.9.x, see [Run with another kafka-clients version](#run-with-another-kafka-clients-version).

## Prerequirements
- A Kafka broker on `localhost:9092` (override with `-Dkafka.broker.url=host:port`):
```
$ docker run -d --rm --name kafka-testweb -p 9092:9092 apache/kafka:3.8.1
```

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/kafka-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-plugin-testweb spring-boot:start
```
Call them in this order:
- `KafkaProducer.send` to `kafka-it-topic`: http://localhost:18080/producer
- `KafkaConsumer.poll`, then the consumer entry point for each record: http://localhost:18080/consumer
  (returns `consumed N`; it waits up to 15 seconds for the first batch)

`TestConsumerRecordEntryPoint.consumeRecord` is the consumer entry point, registered with
`profiler.kafka.consumer.entryPoint` in the module pom.

## Run with another kafka-clients version
Override `plugins-bom.kafka3.version` and rebuild the module in the same command:
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-plugin-testweb clean package spring-boot:start -Dmaven.test.skip=true -Dplugins-bom.kafka3.version=2.0.1
```
- Range: 0.11.0.0 to 3.9.x. The app uses `KafkaConsumer.poll(long)`, which 4.x removed.
- Keep `-pl` on this module only: the plugin ITs compile against the same property.
- Use a 3.x broker. Kafka 4.x brokers no longer accept clients older than 2.1.
- To check the version in use, look for `kafka-clients-<version>.jar` in the classpath the agent prints at startup.

## Verify
The app has no automated test. Call the endpoints and check the console of the terminal that ran `spring-boot:start`;
both the app output and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app output)
The entry point prints the `Pinpoint-*` headers of each record it receives:
```
Received record: message-bb173715-..., headers={Pinpoint-Flags=0, Pinpoint-SpanID=-1665141085243794626, Pinpoint-TraceID=...^1795417924701^2, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=5062451714032440552}
```
`headers={}` means the producer side did not inject them.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-plugin-testweb spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
`/producer` followed by `/consumer` flushes (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=5062451714032440552 ... SpanEvent{... serviceType=8660, endPoint='localhost:9092' ... nextSpanId=-1665141085243794626, destinationId='localhost:9092' ...
BufferedStorage -- Flush Span{... parentSpanId=5062451714032440552, spanId=-1665141085243794626 ... remoteAddr='localhost:9092' ... acceptorHost='localhost:9092' ...
```
Check that
- the `/producer` request span has a `SpanEvent` with `serviceType=8660` (KAFKA_CLIENT) and the broker address in
  `endPoint` and `destinationId`;
- a consumer `Span` exists with the broker address in `remoteAddr` and `acceptorHost`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/kafka-plugin-testweb spring-boot:stop
$ docker stop kafka-testweb
```
