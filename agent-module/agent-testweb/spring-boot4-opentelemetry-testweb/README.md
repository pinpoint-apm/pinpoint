## spring-boot4-opentelemetry-testweb

Spring Boot 4 app that is **already instrumented with `spring-boot-starter-opentelemetry`** (Micrometer Observation ->
OTel SDK; the Boot 4 successor of `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp`) and sends its
spans to Pinpoint by adding a second `SpanExporter` bean. Boot 3 counterpart: `spring-boot3-micrometer-tracing-testweb`.

- Do **not** attach the Pinpoint agent or the OpenTelemetry Java Agent (duplicate instrumentation).
- Boot 4 splits the starters: `spring-boot-starter-webmvc` / `-aspectj` (`@Observed`) / `-restclient` (`RestClient.Builder` bean).
- OTLP keys moved to `management.opentelemetry.tracing.export.otlp.*` (Boot 3: `management.otlp.tracing.*`).
- The Pinpoint bean is declared as `SpanExporter`, not `OtlpGrpcSpanExporter`; otherwise the OTLP exporter
  auto-configured by Boot backs off (`@ConditionalOnMissingBean`).

### Run (JDK 17)

```
./mvnw -Pjdk17 -pl agent-module/agent-testweb/spring-boot4-opentelemetry-testweb package -Dmaven.test.skip=true -Dspring-boot-build-skip=false
java -jar agent-module/agent-testweb/spring-boot4-opentelemetry-testweb/target/pinpoint-spring-boot4-opentelemetry-testweb-*-exec.jar \
  --pinpoint.otel.trace.endpoint=http://<collector>:9998 \
  --management.opentelemetry.tracing.export.otlp.endpoint=http://<existing-backend>:4317
```

Unlike the other testwebs the exec jar does not attach the pinpoint agent.

### Endpoints (port 18083)

| path | spans |
| --- | --- |
| `/helloworld` | server span |
| `/user/{id}` | server span (`uri` template) |
| `/observed` | server span + `@Observed` span |
| `/remote` | server span -> RestClient client span -> server span (`/user/{id}`) |
| `/throw` | server span + `@Observed` span with error |
| `/actuator/beans` | check the `SpanExporter` beans |

`--pinpoint.otel.trace.debug=true` additionally logs every exported span as OTLP JSON, exactly what the collector receives.

### What the collector sees (verified 2026-08-25, Boot 4.1.0)

- scope `org.springframework.boot` (same name as Boot 3.5) and the same **Micrometer keys** instead of OTel semconv:
  `uri` (template), `http.url` (raw path), `method`, `status` (string), `outcome`, `exception`, client `client.name`.
- The collector maps those keys (`OtlpMicrometerAttributes`, gated on that scope) to rpc = `uri` template and the
  HTTP status / method annotations. Fixture: `otlptrace-collector` `spring-boot-4.1-starter-opentelemetry.pb`.
- The resource has no `service.instance.id` / `process.*`: set `service.instance.id` yourself (`${random.uuid}` above)
  or the collector rejects the spans.
