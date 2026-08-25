## spring-boot3-otel-starter-testweb

Spring Boot 3 app instrumented with the **OpenTelemetry Spring Boot Starter** (`opentelemetry-spring-boot-starter`,
OTel SDK autoconfigure, no javaagent). Pinpoint is added as a second exporter through an
`AutoConfigurationCustomizerProvider` bean (guide section B).

- Do **not** attach the Pinpoint agent or the OpenTelemetry Java Agent (duplicate instrumentation).
- The starter's own exporter is configured by `otel.exporter.otlp.*`; its **default protocol is `http/protobuf`**, so
  `otel.exporter.otlp.protocol=grpc` must be set explicitly when it points at a gRPC-only collector.
- `RestClient.create()` is not instrumented; inject `RestClient.Builder` and build the client from it.

### Run (JDK 17)

```
./mvnw -Pjdk17 -pl agent-module/agent-testweb/spring-boot3-otel-starter-testweb package -Dmaven.test.skip=true -Dspring-boot-build-skip=false
java -jar agent-module/agent-testweb/spring-boot3-otel-starter-testweb/target/pinpoint-spring-boot3-otel-starter-testweb-*-exec.jar \
  --pinpoint.otel.trace.endpoint=http://<collector>:9998 \
  --otel.exporter.otlp.endpoint=http://<existing-backend>:4317
```

Unlike the other testwebs the exec jar does not attach the pinpoint agent.

### Endpoints (port 18081)

| path | spans |
| --- | --- |
| `/helloworld` | server span |
| `/user/{id}` | server span (`http.route` template) |
| `/observed` | server span + `@WithSpan` INTERNAL span |
| `/remote` | server span -> RestClient client span -> server span (`/user/{id}`) |
| `/throw` | server span + `@WithSpan` span with error |

### What the collector sees (verified 2026-08-25, starter 2.28.1 / SDK 1.62 / Boot 3.5.14)

- spans carry stable HTTP semconv (`http.route`, `url.path`, `http.request.method`, `http.response.status_code`),
  so rpc template / status / method map without any key mapping.
- the resource includes `host.*`, `os.*`, `process.*` and an auto-generated `service.instance.id` (UUID); unlike
  Spring Boot Actuator's micrometer-tracing nothing has to be set for the agentId.
