## Prerequirements
- JDK 17 (Spring Boot 4).
- No external ActiveMQ is needed: each app starts its own broker and connects to it over tcp.
  To use an external broker instead, set `activemq.broker.embedded=false` and point the broker url at it.
- ActiveMQ 5.x speaks `javax.jms` and 6.x speaks `jakarta.jms`, so each line has its own app: `activemq-5` and `activemq-6`.
- Both apps call the client through the JMS API (`send`, `sendAsync`, `sendAndReceive`), so the two lines can be
  compared. `activemq-6` adds Spring JMS, which Spring Boot 4 offers for `jakarta.jms` only.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5,agent-module/agent-testweb/activemq-plugin-testweb/activemq-6 -am -Dmaven.test.skip=true
```

## ActiveMQ 5.x (`activemq-5`)
- ActiveMQ 5.19.12 client and broker (`activemq5.version` in the module pom), `tcp://localhost:61616` (`activemq.broker.url`)

### Run
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5 spring-boot:start
```
- Send, consumed by a MessageListener: http://localhost:18080/activemq5/send
- Send with an AsyncCallback, consumed by a MessageListener: http://localhost:18080/activemq5/sendAsync
- Send, then MessageConsumer.receive: http://localhost:18080/activemq5/sendAndReceive

### Stop
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5 spring-boot:stop
```

## ActiveMQ 6.x (`activemq-6`)
- ActiveMQ 6.4.0 client and broker (`activemq6.version` in the module pom, newer than the Spring Boot BOM),
  `tcp://localhost:61617` (`spring.activemq.broker-url`)

### Run
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-6 spring-boot:start
```
JMS API, the same scenarios as `activemq-5`:
- Send, consumed by a MessageListener: http://localhost:18080/activemq6/client/send
- Send with an AsyncCallback, consumed by a MessageListener: http://localhost:18080/activemq6/client/sendAsync
- Send, then MessageConsumer.receive: http://localhost:18080/activemq6/client/sendAndReceive
- JMSContext send, then JMSConsumer.receiveBody (ActiveMQ 6.4.0+): http://localhost:18080/activemq6/client/sendAndReceiveBody

Spring JMS:
- JmsTemplate send, consumed by the default `@JmsListener` container (MessageConsumer.receive): http://localhost:18080/activemq6/spring/send
- JmsTemplate send, consumed by a simple `@JmsListener` container (MessageListener): http://localhost:18080/activemq6/spring/sendToSimpleListener
- JmsTemplate send, then JmsTemplate.receive: http://localhost:18080/activemq6/spring/sendAndReceive

### Stop
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-6 spring-boot:stop
```

## Verify
The apps have no automated test. Call an endpoint and check the console of the terminal that ran `spring-boot:start`;
both the app log and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app log)
Every listener logs the `Pinpoint-*` headers of the message it received, and `sendAndReceive` returns them:
```
c.p.test.plugin.ActiveMQ6SpringListener  : Received spring/send from pinpoint.activemq6.spring.listener headers={Pinpoint-Flags=0, Pinpoint-SpanID=4580048072955591402, Pinpoint-TraceID=...^1795411333839^2, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=-6314997172741667704}
```
`headers={}` means the producer side did not inject them.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-6 spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
One call of `/activemq6/spring/send` flushes two spans (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-6314997172741667704 ... spanEventList=[SpanEvent{... serviceType=8310, endPoint='tcp://127.0.0.1:61617', annotations=[StringAnnotation{100=queue://pinpoint.activemq6.spring.listener}, StringAnnotation{101=tcp://127.0.0.1:61617}], nextSpanId=4580048072955591402, destinationId='pinpoint.activemq6.spring.listener' ...
BufferedStorage -- Flush Span{... parentSpanId=-6314997172741667704, spanId=4580048072955591402 ... serviceType=8310, remoteAddr='tcp://127.0.0.1:61617' ... parentApplicationName='PluginTest', parentApplicationType=1210, acceptorHost='pinpoint.activemq6.spring.listener' ...
```
Check that
- the request span has a producer `SpanEvent` with `serviceType=8310` (ACTIVEMQ_CLIENT), the broker address in
  `endPoint`, and the queue in `destinationId`;
- a consumer `Span` exists with `serviceType=8310`, the broker address in `remoteAddr`, and the queue in `acceptorHost`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

For the pull endpoints (`sendAndReceive`), the request span also has a `SpanEvent` with `serviceType=8311`
(ACTIVEMQ_CLIENT_INTERNAL) for `MessageConsumer.receive`.

### Known gap
`JMSConsumer.receiveBody` (ActiveMQ 6.4.0+) is not instrumented: `/activemq6/client/sendAndReceiveBody` records the producer
event and the consumer span, but the request span has no `serviceType=8311` event for the receive call.
