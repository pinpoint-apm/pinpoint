# RabbitMQ plugin testweb

Spring Boot 4 app that exercises the RabbitMQ plugin through both `spring-rabbit` and the plain `amqp-client` API.

- `com.rabbitmq:amqp-client` 5.38.0 (`rabbitmq.amqp-client.version` in the module pom, newer than the Spring Boot BOM)
- `org.springframework.amqp:spring-rabbit` 4.2.0 (`rabbitmq.spring-amqp.version` in the module pom, newer than the Spring Boot BOM)

## Prerequirements
- JDK 17 (Spring Boot 4).
- A RabbitMQ broker on `localhost:5672` with the default `guest` / `guest` account
  (`spring.rabbitmq.*` in `application.yml`):
```
$ docker run -d --rm --name rabbitmq-testweb -p 5672:5672 rabbitmq:3-management
```
The exchange and the queues are declared by the app on startup and are deleted automatically.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/rabbitmq-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rabbitmq-plugin-testweb spring-boot:start
```

### spring-rabbit
- `RabbitTemplate.convertAndSend`, consumed by a `SimpleMessageListenerContainer`: http://localhost:18080/spring/send
- `RabbitTemplate.convertAndSend`, consumed by a `DirectMessageListenerContainer`: http://localhost:18080/spring/send-direct
- `RabbitTemplate.convertAndSend`, then `RabbitTemplate.receive`: http://localhost:18080/spring/send-and-receive

### amqp-client
- `basicPublish` with a `byte[]` body, consumed by a `Consumer` registered with `basicConsume`: http://localhost:18080/client/publish
- `basicPublish` with a `ByteBuffer` body (amqp-client 5.31.0+), same consumer: http://localhost:18080/client/publish-buffer
- `basicPublish`, then `basicGet`: http://localhost:18080/client/publish-and-get

`RabbitClientConsumer` overrides `handleDelivery` without calling `super`, so it is registered with
`profiler.rabbitmq.client.consumer.classes` in the module pom.

## Verify
The app has no automated test. Call an endpoint and check the console of the terminal that ran `spring-boot:start`;
both the app output and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app output)
Every consumer prints the headers of the message it received, and the two pull endpoints return them in the response:
```
Received by the simple container: Hello, simple container!, headers={Pinpoint-Flags=0, Pinpoint-pSpanID=-5369959188901543636, Pinpoint-TraceID=...^1795413653477^2, Pinpoint-pServiceName=DEFAULT, Pinpoint-SpanID=7809814539539225115, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210}
```
`headers=null` (or no `Pinpoint-*` entry) means the producer side did not inject them.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/rabbitmq-plugin-testweb spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
One call of `/spring/send` flushes two spans (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-5369959188901543636 ... SpanEvent{... serviceType=8300, endPoint='127.0.0.1:5672' ... nextSpanId=7809814539539225115, destinationId='exchange-rabbitmq-testweb-exchange' ...
BufferedStorage -- Flush Span{... parentSpanId=-5369959188901543636, spanId=7809814539539225115 ... remoteAddr='127.0.0.1:5672' ... acceptorHost='exchange-rabbitmq-testweb-exchange' ...
```
Check that
- the request span has a producer `SpanEvent` with `serviceType=8300` (RABBITMQ_CLIENT), the broker address in
  `endPoint` (not `null`), and the exchange in `destinationId`;
- a consumer `Span` exists with the broker address in `remoteAddr` and the exchange in `acceptorHost`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

### Known gaps
- The `ByteBuffer` overload of `basicPublish` is not instrumented when it is called directly:
  `/client/publish-buffer` shows `headers=null`, the request span has no producer event, and the consumer span
  starts a new trace (`parentSpanId=-1`).
- `DirectMessageListenerContainer` (`/spring/send-direct`): the consumer span is recorded and linked, but the
  listener invocation is not continued from it (the plugin has no hook for the container's consumer).

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rabbitmq-plugin-testweb spring-boot:stop
$ docker stop rabbitmq-testweb
```
