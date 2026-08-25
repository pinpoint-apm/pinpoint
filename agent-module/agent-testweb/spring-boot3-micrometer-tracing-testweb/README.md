## spring-boot3-micrometer-tracing-testweb

Spring Boot 3 app that is **already instrumented with micrometer-tracing (`micrometer-tracing-bridge-otel`)**
and sends its spans to Pinpoint by adding a second `SpanExporter` bean.

- Do **not** attach the Pinpoint agent or the OpenTelemetry Java Agent (duplicate instrumentation).
- Spring Boot collects every `SpanExporter` bean into one `BatchSpanProcessor`, so the existing backend
  (`management.otlp.tracing.endpoint`) and Pinpoint (`pinpoint.otel.trace.endpoint`) receive the same spans.
- The Pinpoint bean is declared as `SpanExporter`, not `OtlpGrpcSpanExporter`; otherwise Boot's
  auto-configured OTLP exporter (`@ConditionalOnMissingBean(OtlpGrpcSpanExporter, OtlpHttpSpanExporter)`) backs off.
- To turn off Boot's own OTLP export while keeping tracing: `management.otlp.tracing.export.enabled=false` (Boot 3.4+).

### Run (JDK 17)

```
./mvnw -Pjdk17 -pl agent-module/agent-testweb/spring-boot3-micrometer-tracing-testweb package -Dmaven.test.skip=true -Dspring-boot-build-skip=false
java -jar agent-module/agent-testweb/spring-boot3-micrometer-tracing-testweb/target/pinpoint-spring-boot3-micrometer-tracing-testweb-*-exec.jar \
  --pinpoint.otel.trace.endpoint=http://<collector>:9998 \
  --management.otlp.tracing.endpoint=http://<existing-backend>:4317   # optional: simulates the app's existing OTLP backend
```

or run `MicrometerTracingTestApplication` in the IDE. Unlike the other testwebs the exec jar does not attach the pinpoint agent.

Verified combinations (Boot 3.5.14):

| flags | SpanExporter beans | export targets |
| --- | --- | --- |
| (default) | `pinpointSpanExporter` | 9998 |
| `--management.otlp.tracing.endpoint=http://localhost:4317` | `otlpGrpcSpanExporter`, `pinpointSpanExporter` | 4317, 9998 |
| `--management.otlp.tracing.endpoint=... --management.otlp.tracing.export.enabled=false` | `pinpointSpanExporter` | 9998 |
| `--pinpoint.otel.trace.enabled=false --management.otlp.tracing.endpoint=http://localhost:9998` (property only, no code) | `otlpGrpcSpanExporter` | 9998 |

### Endpoints (port 18080)

| path | spans |
| --- | --- |
| `/helloworld` | server span |
| `/observed` | server span + `@Observed` span |
| `/remote` | server span -> RestClient client span -> server span (`/helloworld`) |
| `/sleep` | server span (1s) |
| `/throw` | server span + `@Observed` span with error (not `/error`: that path is Boot's BasicErrorController) |
| `/actuator/beans` | check that both `otlpGrpcSpanExporter` (when an endpoint is set) and `pinpointSpanExporter` exist |

If the collector is unreachable the app logs `Failed to export spans`; no such log means export succeeded.

`--pinpoint.otel.trace.debug=true` additionally logs every exported span as OTLP JSON (`OtlpJsonLoggingSpanExporter`) — exactly what the collector receives.
