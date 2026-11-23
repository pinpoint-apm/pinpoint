# Pulsar plugin testweb

Spring Boot 4 app that exercises the Pulsar plugin through the shaded `pulsar-client`, the artifact applications
normally depend on. From 5.0 that artifact relocates the client implementation to
`org.apache.pulsar.shade.org.apache.pulsar.*`.

- `org.apache.pulsar:pulsar-client` 5.0.1 (`testweb.pulsar-client.version` in the module pom, newer than the Spring Boot BOM)
- `org.apache.pulsar:pulsar-client-admin-api` 5.0.1: `pulsar-client` 5.0.x needs
  `org.apache.pulsar.common.naming.TopicDomain` but no longer depends on the artifact that has it. Without it the
  client fails with `NoClassDefFoundError` when it creates a consumer or a producer, with or without the agent.

## Prerequirements
- JDK 17 (pulsar-client 4.1+ and Spring Boot 4).
- A Pulsar broker on `pulsar://localhost:6650`:
```
$ docker run -d --rm --name pulsar-testweb -p 6650:6650 apachepulsar/pulsar:4.0.10 bin/pulsar standalone
```
Wait until the broker logs `messaging service is ready` before starting the app.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/pulsar-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/pulsar-plugin-testweb spring-boot:start
```
- `Producer.send` to `pinpoint-test`, consumed by a `MessageListener`: http://localhost:9000/send

## Run with another pulsar-client version
Override `testweb.pulsar-client.version` and rebuild the module in the same command:
```
$ ./mvnw -pl agent-module/agent-testweb/pulsar-plugin-testweb clean package spring-boot:start -Dmaven.test.skip=true -Dtestweb.pulsar-client.version=4.0.14
```
- Range: 4.0.0 to 5.0.1, the range of the plugin. The same broker can be used for all of them.
- The property also sets the version of `pulsar-client-api` and `pulsar-client-admin-api`.
- To check the version in use, look for `pulsar-client-<version>.jar` in the classpath the agent prints at startup.

## Verify
The app has no automated test. Call the endpoint and check the console of the terminal that ran `spring-boot:start`;
both the app log and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app log)
The listener logs the `Pinpoint-*` properties of the message it received:
```
c.n.p.PulsarPluginController : Received message: 9:0:-1, key = test-key, value = send, headers={Pinpoint-Flags=0, Pinpoint-SpanID=447310175267578918, Pinpoint-TraceID=...^1795422475654^1, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=-1019953192096780482}
```
`headers={}` means the producer side did not inject them. `Pulsar service init failed` at startup means the broker is
not reachable or `pulsar-client-admin-api` is missing.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/pulsar-plugin-testweb spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
One call of `/send` flushes (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-1019953192096780482 ... SpanEvent{... serviceType=8670, endPoint='pulsar://localhost:6650' ... nextSpanId=447310175267578918, destinationId='pulsar://localhost:6650' ...
BufferedStorage -- Flush DefaultAsyncSpanChunk{...} SpanChunk{... spanId=-1019953192096780482 ... serviceType=8671 ...
BufferedStorage -- Flush Span{... parentSpanId=-1019953192096780482, spanId=447310175267578918 ... remoteAddr='pulsar://localhost:6650' ... serviceType=8671 ...
```
Check that
- the request span has a producer `SpanEvent` with `serviceType=8670` (PULSAR_CLIENT) and the service URL in
  `endPoint` and `destinationId`;
- an asynchronous `SpanChunk` of the request span records the send callback (`serviceType=8671`,
  PULSAR_CLIENT_INTERNAL);
- a consumer `Span` exists with the service URL in `remoteAddr`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/pulsar-plugin-testweb spring-boot:stop
$ docker stop pulsar-testweb
```
