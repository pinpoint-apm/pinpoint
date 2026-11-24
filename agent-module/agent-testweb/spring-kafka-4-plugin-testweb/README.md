# Spring Kafka 4.x plugin testweb

Spring Boot 4 app that exercises the Kafka plugin through `spring-kafka` listener containers.

- `org.springframework.kafka:spring-kafka` 4.2.0 (`testweb.spring-kafka.version` in the module pom, newer than the Spring Boot BOM)
- `org.apache.kafka:kafka-clients` 4.4.0 (`testweb.kafka-clients.version` in the module pom, newer than the Spring Boot BOM)

## Prerequirements
- JDK 17 (Spring Boot 4).
- A Kafka broker on `localhost:9092` (`spring.kafka.bootstrap-servers` in `application.yml`):
```
$ docker run -d --rm --name kafka-testweb -p 9092:9092 apache/kafka:4.2.2
```
The topics are created by the app on startup.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/spring-kafka-4-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/spring-kafka-4-plugin-testweb spring-boot:start
```
- `KafkaTemplate.send`, consumed by a record listener: http://localhost:18080/producer/record
- 3 sends, consumed by a batch listener taking a `List`: http://localhost:18080/producer/batch-list
- 3 sends, consumed by a batch listener taking `ConsumerRecords`: http://localhost:18080/producer/batch-records
- 3 sends, consumed by a batch listener that always fails: http://localhost:18080/producer/batch-error

## Verify
The app has no automated test. Call an endpoint and check the console of the terminal that ran `spring-boot:start`;
both the app output and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app output)
Every listener prints the `Pinpoint-*` headers of the records it receives:
```
Received record: Hello, World!, headers={Pinpoint-Flags=0, Pinpoint-SpanID=-5036356141666620364, Pinpoint-TraceID=...^1795415532398^2, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=-1358210158718211618}
```
`headers={}` means the producer side did not inject them. The records of a batch carry the same `Pinpoint-TraceID` and
one `Pinpoint-SpanID` each.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/spring-kafka-4-plugin-testweb spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
One call of `/producer/record` flushes two spans (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-1358210158718211618 ... SpanEvent{... serviceType=8660, endPoint='localhost:9092' ... nextSpanId=-5036356141666620364, destinationId='localhost:9092' ...
BufferedStorage -- Flush Span{... parentSpanId=-1358210158718211618, spanId=-5036356141666620364 ... remoteAddr='localhost:9092' ... acceptorHost='localhost:9092' ...
```
Check that
- the request span has one `SpanEvent` per send with `serviceType=8660` (KAFKA_CLIENT) and the broker address in
  `endPoint` and `destinationId`;
- a consumer `Span` exists with the broker address in `remoteAddr` and `acceptorHost`;
- for the record listener they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the
  producer `nextSpanId` (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

A batch listener receives records of several traces at once, so its consumer `Span` starts a new trace
(`parentSpanId=-1`) by design. For `/producer/batch-error` the consumer span also carries the listener exception.

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/spring-kafka-4-plugin-testweb spring-boot:stop
$ docker stop kafka-testweb
```
