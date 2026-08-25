## quarkus-opentelemetry-testweb

Quarkus app instrumented with **`quarkus-opentelemetry`** (OTel SDK embedded in the framework). Spans go to Pinpoint
through the framework's own OTLP exporter (`quarkus.otel.exporter.otlp.*`); a second exporter is not needed for the
sample, fan-out to another backend is a Collector concern (guide section D).

- Do **not** attach the Pinpoint agent or the OpenTelemetry Java Agent (duplicate instrumentation).
- The module is in the **`otel-framework-testweb`** profile (off by default): it pulls `quarkus-maven-plugin` into the reactor.
- `quarkus.otel.exporter.otlp.protocol` defaults to `grpc` already.

### Run (JDK 17)

```
./mvnw -Potel-framework-testweb -pl agent-module/agent-testweb/quarkus-opentelemetry-testweb package -Dmaven.test.skip=true -Dspring-boot-build-skip=false
java -jar agent-module/agent-testweb/quarkus-opentelemetry-testweb/target/quarkus-app/quarkus-run.jar \
  -Dquarkus.otel.exporter.otlp.endpoint=http://<collector>:9998
```

`-Dspring-boot-build-skip=false` (the same switch as the other testwebs) enables the Quarkus build; without it the
module only compiles.

### Endpoints (port 18084)

| path | spans |
| --- | --- |
| `/helloworld` | server span |
| `/user/{id}` | server span (`http.route` template) |
| `/observed` | server span + `@WithSpan` INTERNAL span |
| `/remote` | server span -> REST client span -> server span (`/user/{id}`) |
| `/throw` | server span with error |

### What the collector sees (verified 2026-08-25, Quarkus 3.38.3)

- scope `io.quarkus.opentelemetry`; **stable HTTP semconv** on server and REST client spans (`http.route`, `url.path`,
  `http.request.method`, `http.response.status_code` as int, `server.address/port`, `client.address`, `error.type`,
  `url.full` on the client). rpc template / endPoint / remoteAddr / status / method all map with no key mapping.
  Fixture: `otlptrace-collector` `quarkus-3.38-opentelemetry.pb`.
- Unmatched routes are reported with `http.route=/`.
- The resource has `host.name`, `service.name`, `service.version`, `webengine.*` but **no `service.instance.id`
  and no `process.*`**: the collector falls back to `host.name` as the agentId, which collides when several
  instances run on one host. Set `service.instance.id` in `quarkus.otel.resource.attributes` (done above with
  `${quarkus.uuid}`).
