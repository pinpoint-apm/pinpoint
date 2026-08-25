## micronaut-opentelemetry-testweb

Micronaut app instrumented with **`micronaut-tracing-opentelemetry-http`** (OTel SDK autoconfigure driven by
`otel.*` properties, HTTP server/client filters, `@NewSpan`). Spans go to Pinpoint through the SDK's own OTLP
exporter (`otel.exporter.otlp.*`).

- Do **not** attach the Pinpoint agent or the OpenTelemetry Java Agent (duplicate instrumentation).
- The module is in the **`otel-framework-testweb`** profile (off by default).
- **Micronaut 5.x is compiled for Java 25**: the app, the compiler and the Maven JVM itself must run on JDK 25
  (`micronaut-maven-plugin` / `micronaut-core` are class file version 69.0). This module therefore uses
  `jdk.home=${java.home}` and must be built with `JAVA_HOME=<jdk25>`.
- `application.yml` is silently ignored (snakeyaml is not on the classpath); use `application.properties`.

### Run (JDK 25, also for Maven)

```
JAVA_HOME=<jdk25> ./mvnw -Potel-framework-testweb -pl agent-module/agent-testweb/micronaut-opentelemetry-testweb package -Dmaven.test.skip=true -Dspring-boot-build-skip=false
<jdk25>/bin/java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow \
  -jar agent-module/agent-testweb/micronaut-opentelemetry-testweb/target/pinpoint-micronaut-opentelemetry-testweb-*-exec.jar \
  -Dotel.exporter.otlp.endpoint=http://<collector>:9998
```

`-Dspring-boot-build-skip=false` (the same switch as the other testwebs) builds the shaded exec jar; without it the
module only compiles.

### Endpoints (port 18085)

| path | spans |
| --- | --- |
| `/helloworld` | server span |
| `/user/{id}` | server span (`http.route` template) |
| `/observed` | server span + `@NewSpan` INTERNAL span |
| `/remote` | server span -> HttpClient client span -> server span (`/user/{id}`); needs `@ExecuteOn(BLOCKING)` |
| `/throw` | server span with error |

### What the collector sees (verified 2026-08-25, Micronaut 5.1.2 / tracing 8.2.0 / SDK 1.64.0)

- scopes `io.micronaut.http.server`, `io.micronaut.http.client`, `io.micronaut.code` (no version); **stable HTTP
  semconv** (`http.route`, `url.path`, `http.request.method`, `http.response.status_code` as int, `server.address/port`,
  `error.type`; `url.full` on the client). rpc template / status / method map with no key mapping.
  Fixture: `otlptrace-collector` `micronaut-5.1-tracing-opentelemetry.pb`.
- No `client.address`, so remoteAddr stays empty. Unmatched routes have no `http.route`: rpc falls back to `url.path`.
- Every exception is recorded twice as a span event; the collector still stores one exception record per span.
- **The default resource is `service.name` + `telemetry.sdk.*` only** (no host/process resource providers, no
  auto-generated `service.instance.id`). Without `service.instance.id` the collector rejects every span
  ("no per-instance identifier"), so it is mandatory here. `otel.resource.attributes` is ONE comma-separated string,
  a nested map is ignored. Fixture of the rejected case: `micronaut-5.1-default-resource-rejected.pb`.
