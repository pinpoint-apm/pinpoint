# pinpoint-otlptrace

Pinpoint OTLP trace ingestion. Receives OpenTelemetry trace data (OTLP/gRPC and
OTLP/HTTP) from any OTel-compatible source — typically the
[OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
running on a target application — and writes the spans into Pinpoint's storage
so they show up in ApplicationMap, ServerMap, and the Inspector UI alongside
spans produced by the native Pinpoint agent.

## Modules

| Module | Role |
|---|---|
| `otlptrace-collector` | Spring Boot collector. Listens for OTLP trace exports over gRPC (`:9998`) and HTTP (`POST /v1/traces`, `application/x-protobuf` or OTLP/JSON `application/json`), maps `ResourceSpans` into Pinpoint `SpanBo` / `SpanChunkBo` / `AgentInfoBo`, and inserts them into HBase. |

The collector is enabled by setting `pinpoint.modules.collector.otlptrace.enabled=true`.

The OTLP **logs** receiver ([`otlplog`](../otlplog/README.md)) is co-hosted in this process when
`pinpoint.modules.collector.otlplog.enabled=true`: `LogsService` on the same gRPC servers and
`POST /v1/logs` on the same HTTP port, feeding exception log records into Error Analysis.

## Using the OpenTelemetry Java agent

Point an OTel-instrumented application at the Pinpoint OTLP collector via the
standard OTel agent environment variables:

```sh
java \
  -javaagent:opentelemetry-javaagent.jar \
  -Dotel.resource.attributes=pinpoint.applicationName=order-api,pinpoint.serviceName=order-team,pinpoint.agentName=order-api-seoul-001,service.instance.id=$(uuidgen) \
  -Dotel.exporter.otlp.endpoint=http://pinpoint-otlptrace-collector:9998 \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.traces.exporter=otlp \
  -jar app.jar
```

The example above sets the three Pinpoint identifiers explicitly via
`pinpoint.*` resource attributes plus `service.instance.id` for per-instance
`agentId` derivation. `otel.service.name` is omitted because
`pinpoint.applicationName` takes priority and `service.name` is never
consulted. In Kubernetes the OTel SDK auto-populates `k8s.pod.uid` /
`service.instance.id`, so `service.instance.id` can be dropped from the
command line. If you instead want to drive the collector from pure OTel
semantic-convention keys, set `-Dotel.service.name=order-api` (and optionally
`service.namespace`, `service.instance.id`) and drop the `pinpoint.*` overrides;
see [Configuring identifiers](#configuring-identifiers) for the full fallback
order.

OTLP/HTTP also works — point `otel.exporter.otlp.protocol=http/protobuf` at the
collector's HTTP port and the agent will `POST` to `/v1/traces`.

The collector reads Pinpoint identifiers off the OTLP **Resource** block (the
attributes the OTel SDK builds from `OTEL_RESOURCE_ATTRIBUTES` and the
`service.*` semantic-convention keys). No Pinpoint-specific exporter or plugin
is required on the application side.

## Trace time precision

The OTLP trace collector preserves OpenTelemetry span timestamps with nano
precision by mapping incoming OTel data to Pinpoint trace V3.

- OTLP span start/end timestamps and span event start/end timestamps are stored
  as epoch-nanos in V3.
- This V3 path is currently used for OTLP collector ingestion. The native
  Pinpoint agent gRPC span path keeps the existing V2/millis behavior.
- Backend and UI APIs may still expose millis for compatibility, but V3 keeps
  nanos internally for ordering and Call Tree timeline positioning.

## Configuring identifiers

The collector resolves each identifier from the incoming OTLP Resource
attributes by walking a priority list. The first key that resolves wins; later
keys are only consulted when earlier ones are absent.

For most deployments you set three Pinpoint attributes:
`pinpoint.applicationName`, `pinpoint.serviceName`, `pinpoint.agentName` (display
name) — plus a per-instance identifier (`service.instance.id` on a VM via
`uuidgen`; auto-populated by the OTel SDK in K8s).

### ⚠ Limits and allowed characters

| Identifier | Max length | Pattern |
|---|---:|---|
| `agentId` | **24** | `[a-zA-Z0-9._-]+` |
| `agentName` | **254** | `[a-zA-Z0-9._-]+` |
| `applicationName` | **254** | `[a-zA-Z0-9._-]+` |
| `serviceName` | **254** | `[a-zA-Z0-9._-]+` |

Allowed: ASCII letters, digits, dot (`.`), underscore (`_`), hyphen (`-`).
Anything outside this set — spaces, slashes, colons, non-ASCII — causes the
span to be **rejected at the collector**.

`agentId`'s 24-char ceiling is the most restrictive because it's part of the
HBase row key. **You normally don't set this directly** — provide
`pinpoint.agentName` (or one of the standard OTel identifiers like
`service.instance.id`) and the collector hashes/encodes it down to fit. See
[agentId — internal derivation](#agentid--internal-derivation) below.

### `applicationName`

The logical application shown on ApplicationMap (e.g. `order-api`).

1. `pinpoint.applicationName`
2. `service.name`

If neither resolves, the span is rejected.

### `agentName` (display name)

The per-instance display name shown alongside `agentId` in the UI. Set this
to a human-readable label; **`agentName` is not used as a fallback source for
`agentId`** — provide a dedicated per-instance identifier (see
[`agentId` — internal derivation](#agentid--internal-derivation)) such as
`service.instance.id` (uuidgen on a VM, OTel SDK auto-populated in K8s) or
`k8s.pod.uid` (auto-populated in K8s).

Resolution:
1. `pinpoint.agentName` — used verbatim when present.
2. Otherwise inherits from whichever `agentId` derivation step fired:
   - `service.instance.id` / `k8s.pod.uid` UUID → **original UUID string** preserved.
   - `container.id` full 64-hex → **original 64-hex string** preserved.
   - Other sources → same as the derived `agentId`.

### `serviceName`

Pinpoint's multi-tenant service grouping (the level above `applicationName`).

1. `pinpoint.serviceName` — explicit only. The name is resolved to its serviceUid
   through the collector's service lookup (`pinpoint.collector.service.lookup.enabled`,
   default `true`), and the span, its agent/application registration and the
   statistics keyed by `serviceName` (heatmap, URI stat, Error Analysis) all land
   under that service. **The service must already be registered on the Pinpoint
   side** (`POST /api/v2/services`); otherwise every span of the `ResourceSpans` is
   rejected with `reason=service_not_found` (see *Validation failures* below).
2. Defaults to Pinpoint's built-in default service when not set.

`service.namespace` is deliberately **not** a fallback: OTel-native deployments set
it for their own grouping, and promoting it would turn an unregistered namespace
into a reject. The same rule applies to the `pp=svc:...` tracestate entry written
by `pinpoint-otel-extension`, so a sender's own service and the parent service its
callees record are always the same name.

### `agentId` — internal derivation

The per-process identifier (≤ 24 chars, part of the HBase row key). **You
rarely need to set this directly.** Rely on standard OTel identifiers
(`service.instance.id`, `k8s.pod.uid`, etc.) populated by the SDK, or set
`service.instance.id` explicitly (e.g. via `uuidgen` on a VM).

The collector walks this fallback chain (first match wins):

1. `pinpoint.agentId` — used verbatim.
2. `service.instance.id` — if it parses as a UUID, the 16 raw bytes are
   URL-safe Base64-encoded to a 22-char `agentId`; otherwise used verbatim.
3. `k8s.pod.uid` — same UUID handling as `service.instance.id`.
4. `container.id` — Docker/containerd 64-char hex SHA256 IDs are truncated
   to the first 16 bytes and Base64-encoded (22 chars). Shorter values are
   used verbatim.
5. `host.name` — used verbatim.
6. `applicationName` — **gated test/dev fallback** (used by the OTel demo apps).
   Disabled by default in production; enable with the configuration property
   below. All instances of the same application collapse to the same
   `agentId`, so it's only suitable for test/dev environments. In production,
   provide a per-instance identifier higher in the chain.

For Kubernetes workloads the OTel SDK usually populates `k8s.pod.uid` or
`service.instance.id` automatically — the resulting Base64 form fits the
24-char budget while remaining stable per pod, so you get a sensible `agentId`
without any configuration.

#### `applicationName` fallback — configuration

The `applicationName` fallback (step 6) is controlled by:

| Property | Default | Notes |
|---|---|---|
| `pinpoint.collector.otlptrace.application-name-fallback.enabled` | `false` | Set `true` on the `dev` Spring profile only. |

When disabled, a request with no per-instance identifier is rejected with:

```
no per-instance identifier — set service.instance.id (e.g. via uuidgen), k8s.pod.uid, container.id, or host.name. applicationName='<value>'
```

Recommended per Spring profile (collector profile, set in
`profiles/<profile>/pinpoint-collector.properties`):

| Profile | Value |
|---|---|
| `local` | `true` (OTel demo / sandbox compatibility) |
| `release` (production) | `false` (default — forces explicit per-instance identifier) |

### Minimal vs. recommended

| Setup | Result |
|---|---|
| `service.name` only | Works. `applicationName` = `service.name`, `agentId` derived from `host.name` / `service.instance.id` / `applicationName`, `serviceName` defaults. |
| `service.name` + `service.instance.id` | Pure OTel semconv. `applicationName` and `agentId` resolve through the fallback chain; `serviceName` defaults (`service.namespace` is ignored). Good fit for OTel-native deployments. |
| `pinpoint.applicationName` + `pinpoint.agentName` + `service.instance.id` | **Recommended.** Two explicit Pinpoint attributes plus a per-instance identifier (`uuidgen` on a VM, auto-populated by OTel SDK in K8s). Add `pinpoint.serviceName` only when that service is registered on the Pinpoint side. |

Mixing is fine — set `pinpoint.*` only for the identifiers you want to pin and
let the rest fall through to the OTel semconv keys.

### Validation errors

When an identifier fails validation, the **span is rejected**: the OTLP export
call fails (gRPC `INVALID_ARGUMENT` / partial-success response on HTTP), the
rejected-span count is incremented — every span of the offending `ResourceSpans`
is counted, and the collector metric `collector.otlptrace.span.rejected` records
them under `reason=invalid_resource` (see the collector README, *Ingest metrics*,
for the other reason codes) — and the collector logs a message like one of the
following — the offending value is appended after `=`:

| Failure | Message |
|---|---|
| `pinpoint.agentId` length/pattern | `invalid pinpoint.agentId=<value>` |
| `pinpoint.agentName` length/pattern | `invalid pinpoint.agentName=<value>` |
| `service.instance.id` length/pattern (after UUID fallback) | `invalid service.instance.id=<value>` |
| `k8s.pod.uid` length/pattern | `invalid k8s.pod.uid=<value>` |
| `container.id` length/pattern (after 64-hex fallback) | `invalid container.id=<value>` |
| `host.name` length/pattern | `invalid host.name=<value>` |
| `applicationName` fallback exceeds 24 chars (used as `agentId`) | `invalid agentId(derived from applicationName)=<value>` |
| `applicationName` length/pattern | `invalid applicationName=<value>` |
| Neither `pinpoint.applicationName` nor `service.name` present | `not found applicationName` |
| No per-instance identifier and `applicationName` fallback disabled | `no per-instance identifier — set service.instance.id ...` |
| `serviceName` length/pattern | `invalid serviceName=<value>` |
| `pinpoint.serviceName` not registered on the Pinpoint side (`reason=service_not_found`) | `service not found. serviceName=<value>` |

All of these share the same root cause: the value exceeds the **length cap**
or violates the **`[a-zA-Z0-9._-]+` pattern** in the limits table above. Fix
the offending attribute on the OTel agent side (no spaces, no special
characters, within the length budget) and the error goes away.

## URI stat

The collector can also derive **URI stat** (per-endpoint response-time histograms, the data behind
the URI Stat charts) from OTLP traces. It aggregates the entry-point spans of each export request by
`(serviceName, applicationName, agentId, uri, 30s window)` and writes them to the same Kafka/Pinot
store the native agent's URI stat uses.

Only spans carrying an OTel `http.route` template (e.g. `/users/{id}`) contribute — the raw
`url.path` (`/users/12345`) is deliberately excluded so the stat stays low-cardinality, mirroring the
native agent recording URI stat solely from its resolved URI template.

| Property | Default | Notes |
|---|---|---|
| `pinpoint.collector.otlptrace.uristat.enabled` | `false` | Enables OTLP → URI stat feeding. |
| `pinpoint.modules.collector.uristat.enabled` | — | **Prerequisite.** Provides the shared URI stat storage module (Kafka/Pinot). Both flags must be `true`. |

Off by default for two reasons: it requires the URI stat storage module, and OTLP spans are
**sampled** — so counts/apdex reflect the sampled population rather than the native agent's
pre-sampling totals. Keep that distinction in mind when reading charts that mix both sources.

## Error Analysis

The collector maps every OTel `exception` span event to an Error Analysis record
(`ExceptionMetaDataBo`, see `OtlpExceptionMapper`) and writes it through the same Kafka/Pinot
exception-trace store the native agent uses. The store is a separate collector module, so it has to
be enabled explicitly; without it the OTLPTRACE app has no `ExceptionMetaDataService` and the mapped
exceptions are silently dropped (traces and URI stat are unaffected).

| Property | Default | Notes |
|---|---|---|
| `pinpoint.modules.collector.exceptiontrace.enabled` | `false` | **Prerequisite.** Provides the exception-trace storage beans (`ExceptionTraceCollectorConfig`: Pinot + Kafka). The BASIC collector reads the same flag for agent exceptions. The `local` profile turns it on. |
| `pinpoint.modules.web.exceptiontrace.enabled` | `false` | Web side: shows the Error Analysis menu that reads the store. |

## Inspector metrics (OTLP metrics)

The OTLPTRACE collector can also receive **OTLP metrics** and store the OTel JVM runtime metrics of
each Resource as the native agent's Inspector stat (`AgentStatBo` → `AgentStatService`, the Inspector
module's Kafka/Pinot store), so an OTel application shows the Inspector charts (Heap/Non-Heap, GC,
CPU, Thread, Class, Buffer, Open File Descriptor, Response Time, Data Source). The receiver is served on the same
endpoints as traces, as the logs receiver is: `MetricsService/Export` on the gRPC servers and
`POST /v1/metrics` on the servlet port, protobuf, gzip accepted. The exporter needs no per-signal
endpoint.

The mapping (`pinpoint-otlptrace-inspector`, `OtlpMetricInspectorMapper`) is by **metric name**; it
never stores arbitrary metrics. Six naming dialects are understood and may be mixed in one Resource
(`pinpoint.collector.otlptrace.inspector.dialects`, default `otel,micrometer,go,nodejs,python,dotnet`; a shared name such as
`jvm.memory.used` is told apart by its attribute keys):

- `otel` — the OpenTelemetry semantic conventions, i.e. the OTel Java agent's `runtime-telemetry`
  (durations in seconds), and the process/host metrics any SDK's host instrumentation sends
  (`process.cpu.time`, `system.cpu.time` — mode in `cpu.mode` or the older `system.cpu.state` / `state`
  — and `process.thread.count`; see "Go", "Node.js", "Python" and ".NET" below).
- `micrometer` — Micrometer's own binder names as exported by `micrometer-registry-otlp`, i.e.
  Spring Boot Actuator's OTLP metrics (durations in milliseconds, unit `milliseconds`).
- `go` — the OTel Go runtime instrumentation (`go.opentelemetry.io/contrib/instrumentation/runtime`,
  `go.*` semantic conventions), mapped onto the closest JVM charts (see "Go" below).
- `nodejs` — the OTel JS runtime instrumentation (`@opentelemetry/instrumentation-runtime-node`,
  `v8js.*` semantic conventions), mapped onto the closest JVM charts (see "Node.js" below).
- `python` — the CPython metrics of `opentelemetry-instrumentation-system-metrics`, mapped onto the
  closest JVM charts (see "Python" below).
- `dotnet` — the .NET runtime metrics: the runtime's own `System.Runtime` meter on .NET 9+ (`dotnet.*`)
  and `OpenTelemetry.Instrumentation.Runtime`'s `process.runtime.dotnet.*` on .NET 8 and earlier,
  mapped onto the closest JVM charts (see ".NET" below).

| Inspector chart | `otel` metric | `micrometer` metric |
|---|---|---|
| Heap / Non-Heap used, max | `jvm.memory.used`, `jvm.memory.limit` summed per `jvm.memory.type`. Heap max is the sum of the heap pools that report a limit (under G1 only the old gen does, and that is the heap max); the two generational ZGC pools each report the whole heap, so it counts once. Non-heap max follows `MemoryMXBean` like the agent: -1 unless every non-heap pool in use reports a limit (Metaspace normally does not) | `jvm.memory.used{area=heap\|nonheap, id=pool}`, `jvm.memory.max` (a pool's `-1` means "no limit"), same rules |
| GC old count / time | `jvm.gc.duration` count/sum of the **old/major collector only** (`jvm.gc.name`, the agent's table: `MarkSweepCompact`, `PS MarkSweep`, `ConcurrentMarkSweep`, `G1 Old Generation`, `ZGC Major Pauses`). The young collector, `G1 Concurrent GC` and the ZGC `* Cycles` beans are not charted by the agent either and count as `ignored_series` | `jvm.gc.pause` per `gc`, same rule (`jvm.gc.concurrent.phase.time` is `ignored_series`) |
| JVM CPU / System CPU | `jvm.cpu.recent_utilization` / `jvm.system.cpu.utilization` (experimental: `otel.instrumentation.runtime-telemetry.emit-experimental-metrics=true`; the older `emit-experimental-telemetry` still works in 2.31.1 but is deprecated) | `process.cpu.usage` / `system.cpu.usage` (default binders) |
| Total Thread, Loaded/Unloaded Class, Direct/Mapped Buffer, Open File Descriptor | `jvm.thread.count`, `jvm.class.count`, `jvm.class.unloaded`, `jvm.buffer.count`, `jvm.buffer.memory.used`, `jvm.file_descriptor.count` (experimental) | `jvm.threads.live` (`jvm.threads.states` is ignored, it is a split of the same number), `jvm.classes.loaded`, `jvm.classes.unloaded`, `jvm.buffer.count{id}`, `jvm.buffer.memory.used{id}`, `process.files.open` |
| Response Time avg / max | `http.server.request.duration`, all attribute series folded: avg = Δsum/Δcount, max = the `max` field (DELTA) or the upper bound of the highest bucket that grew (CUMULATIVE; when only the overflow bucket grew, the last bound or the interval mean, whichever is larger) | `http.server.requests` (a Timer): same rule. Micrometer sends `max` only under DELTA and buckets only with `management.metrics.distribution.percentiles-histogram.http.server.requests=true`; with neither, max falls back to each series' interval mean (the point keeps the largest) |
| Data Source active / max connections, per pool | the agent's pool instrumentations (HikariCP, DBCP2, Tomcat JDBC, c3p0, Vibur, Oracle UCP, Druid; on by default): `db.client.connections.usage{pool.name, state=used}` and `db.client.connections.max` (2.x default names), or `db.client.connection.count{db.client.connection.pool.name, db.client.connection.state=used}` and `db.client.connection.max` (`otel.semconv-stability.opt-in=database`). `state=idle` is `ignored_series` | `hikaricp.connections.active{pool}`, `hikaricp.connections.max` (HikariCP only, see below) |
| Agent start time (session) | `process.creation.time` resource attribute | `process.start.time` (the JVM start, `RuntimeMXBean.getStartTime()`) |
| Transactions Per Second, Active Request, Deadlock | no counterpart — not produced, the charts stay empty | same |

`jvmGcDetailed` (young GC count/time, per-pool usage ratios) is not produced: the web has no chart for
it, and leaving it out saves 8 of the 26 fields a point would otherwise write.

Data Source: neither OTel nor Micrometer reports the JDBC URL, so the **pool name** stands in for the
chart's `jdbcUrl` and `databaseName` (the series is shown as `<pool name>-<id>`, the id a stable hash of
the name). The OTel Java agent names a HikariCP pool left at its generated name (`HikariPool-1`) after
the JDBC URL, `host:port/db`, so those pools show their database; a pool named by the application
(`spring.datasource.hikari.pool-name`) shows that name. The service type is the pool library, read
from the instrumentation scope (`HIKARICP`, `DBCP2`, `DRUID`; other libraries `UNKNOWN_DB`). On the
Micrometer side only HikariCP's own `hikaricp.connections.*` is read: Spring Boot's
`jdbc.connections.*` is bound when the context starts, before HikariCP opens its pool, so for HikariCP
it never carries the active count (only `max`/`min`), and it would count the same pool a second time.
Other pools sent through Micrometer are not charted. `hikaricp.*` appears once the pool has made its
first connection. A pool is its library (the instrumentation scope, without its version) and its name: two
libraries' pools of the same name are two entries, each with its own id and service type. The agent
chart finds a pool by its `jdbcUrl` alone, so for a name two libraries share, `jdbcUrl` carries the
library as well (`orders (hikaricp-3.0)`, `orders (apache-dbcp-2.0)`); a name used once stays as it is.
The application chart groups agents by `jdbcUrl`.

Sources checked against real output:
the OTel Java agent 2.x (JMX or JFR runtime metrics; G1, Parallel, Serial, ZGC, Shenandoah), the OTel
Java SDK used directly (Spring Boot starter: the experimental buffer and system CPU metrics are off by
default, so those charts stay empty) and Micrometer. Known differences from the Pinpoint agent: under
Parallel GC the heap max is the sum of the pool limits, a little above the agent's (Eden is resized);
old collections that ran before the OTel agent installed its GC listener (JVM startup) are not in the
count, which the delta chart does not show; with the runtime metrics taken from JFR the old count is
higher than the agent's (JFR reports `G1 Old Generation` collections the MXBean does not count);
Shenandoah has no GC type, as with the agent. The OTel Java agent 1.x (`process.runtime.jvm.*`) is not
recognised.

**Go** (`go` dialect; checked with Go 1.26, OTel Go 1.47, `instrumentation/runtime` 0.72). The Go
runtime metrics (semantic conventions in Development) are shown on the closest JVM charts; the
charts keep their JVM titles:

| Inspector chart | Go metric |
|---|---|
| Heap used / max | `go.memory.used{go.memory.type=other}` — memory the Go runtime uses outside goroutine stacks (heap objects, free and unused heap spans, runtime metadata), not only live objects. Max = `go.memory.limit`, sent only when `GOMEMLIMIT` is set, else -1 |
| Non-Heap used | `go.memory.used{go.memory.type=stack}` (goroutine stacks); max -1 |
| GC old count / time | Count = `go.memory.gc.cycles`, time = the sum of `go.memory.gc.pause.duration` (its count is the stop-the-world pauses, two per cycle, and is not used): opt-in, listed in `OTEL_GO_X_RUNTIME_METRICS_OPTIN` (contrib releases after 0.72). Up to contrib 0.72: `process.runtime.go.gc.pause_ns` (one point per cycle), sent with `OTEL_GO_X_DEPRECATED_RUNTIME_METRICS=true`; later releases remove it, and when both come the cycles win (`duplicate_source`). Every cycle counts (the Go collector is not generational), GC type unknown. The default runtime metrics have no GC |
| JVM CPU / System CPU | From running totals, differenced between exports (the first export of a process gives none, `no_previous_point`). The increases add up only over one interval: an export that leaves out a series the previous one had (or whose point is dropped as invalid) gives no CPU, nor does the next, whose increase of that series spans two intervals. With the contrib host metrics (`go.opentelemetry.io/contrib/instrumentation/host`; `otel` dialect names): process CPU = increase of `process.cpu.time{cpu.mode}` over that of `system.cpu.time{cpu.mode}` (every CPU of the machine, idle included), like the JVM's process CPU load; system CPU = 1 - idle share of `system.cpu.time`. Without them, the opt-in `go.cpu.time{go.cpu.state}`: process CPU = 1 - idle share of the runtime's capacity (GOMAXPROCS), a runtime estimate (slightly high), refreshed every ~15 s, so an export within that window gives no CPU point; system CPU -1. The host's system CPU is the whole machine (in a container, possibly the host's) |
| Total Thread | `go.goroutine.count` — live goroutines, not OS threads |
| Response Time | `http.server.request.duration` (`otelhttp` 0.61+), as for every SDK |

Not mapped (`unknown_metric`): `go.memory.allocated`/`.allocations`, `go.memory.gc.goal`,
`go.processor.limit`, `go.config.gogc`, `go.schedule.duration` (no Inspector chart), the host's
`system.memory.*` and `system.network.io`, and the other deprecated `process.runtime.go.*` metrics
(the current names are sent next to them). The Go SDK sets no `process.creation.time`, so the agent
start time is the SDK start (the points' `start_time`).

**Node.js** (`nodejs` dialect; checked with Node.js 24, `sdk-node` 0.300, `instrumentation-runtime-node`
0.37 — on by default in `auto-instrumentations-node` — and `instrumentation-host-metrics` 0.7):

| Inspector chart | Node.js metric |
|---|---|
| Heap used | `v8js.memory.heap.used`, summed over the V8 heap spaces (`v8js.heap.space.name`). V8 reports no heap limit: max -1. No non-heap (-1) |
| GC old count / time | `v8js.gc.duration{v8js.gc.type=major}` (Mark-Sweep-Compact); `minor` (Scavenge), `incremental` and `weakcb` are `ignored_series`. GC type unknown |
| JVM CPU / System CPU | Only with `@opentelemetry/instrumentation-host-metrics` (off by default in `auto-instrumentations-node`): its `process.cpu.time{cpu.mode}` / `system.cpu.time{cpu.logical_number, cpu.mode}`, the same rule as for Go (`otel` dialect); the result equals the package's own `process.cpu.utilization` |
| Response Time | `http.server.request.duration` (`instrumentation-http` 0.300 emits it by default) |

Not mapped (`unknown_metric`): `nodejs.eventloop.*`, `v8js.resource.active`, the other
`v8js.memory.heap.space.*` sizes, and the host's `process.cpu.utilization`, `process.memory.usage`,
`system.memory.*`, `system.network.*`. There is no thread chart. The JS SDK sets no
`process.creation.time` either.

**Python** (`python` dialect; checked with Python 3.14, `opentelemetry-instrument` 0.66b2 with the
Flask and `opentelemetry-instrumentation-system-metrics` instrumentations, psutil 7.2). The runtime and
host metrics come from system-metrics only: `opentelemetry-bootstrap` installs it when psutil is
present, and `opentelemetry-instrument` then enables it. Without it a Python application shows the
Response Time chart only.

| Inspector chart | Python metric |
|---|---|
| Heap used | `process.runtime.cpython.memory{type=rss}` — the **process's resident set size**, not a heap (CPython has no heap size API); `type=vms` is `ignored_series`. Max and non-heap -1 |
| GC old count | `cpython.gc.collections{cpython.gc.generation=2}` (full collections); generations 0 and 1 are `ignored_series`. CPython reports **no GC time**: 0 |
| JVM CPU / System CPU | `process.cpu.time{type}` / `system.cpu.time{cpu, state}` (`otel` dialect, the older attribute names), the same rule as for Go; the result equals the instrumentation's own `process.cpu.utilization`. Its default configuration reports only the `idle`, `user`, `system` and `irq` states, so on Linux (`iowait`, `nice`, `softirq`, `steal` left out) the machine's total is a little low and the CPU a little high |
| Total Thread | `process.thread.count` (`otel` dialect): the process's OS threads, which Python threads are |
| Response Time | `http.server.request.duration`, only with `OTEL_SEMCONV_STABILITY_OPT_IN=http`: the Python instrumentations emit the old `http.server.duration` (milliseconds, not recognised) by default |

Not mapped (`unknown_metric`): `process.memory.usage` / `.virtual` (the RSS is read from the
CPython-specific name: the semantic-convention name is also sent by other runtimes' host
instrumentations, where it would replace their heap), `cpython.gc.collected_objects` /
`.uncollectable_objects`, the other `process.runtime.cpython.*`, `process.context_switches`,
`process.disk.io`, and the `system.*` memory, swap, disk and network metrics. The Python SDK sets no
`process.*` resource attributes, so the agent start time is the SDK start.

**.NET** (`dotnet` dialect; checked with ASP.NET Core 8 + OpenTelemetry .NET 1.20.1
`AddRuntimeInstrumentation()`, and ASP.NET Core 9 + `AddRuntimeInstrumentation()` (on .NET 9 it subscribes
to the runtime's `System.Runtime` meter) + `OpenTelemetry.Instrumentation.Process` 1.20.1-rc.1):

| Inspector chart | .NET 9+ (`System.Runtime`) | .NET 8 and earlier (runtime instrumentation) |
|---|---|---|
| Heap used | `dotnet.gc.last_collection.heap.size`, summed over the generations: the heap **after the latest collection** (fragmentation included) | `process.runtime.dotnet.gc.objects.size`: the live objects now |
| Heap max / Non-Heap | -1 (the committed size is not a limit) | same |
| GC old count | `dotnet.gc.collections{gc.heap.generation=gen2}` (the runtime sends `gc.heap.generation`; the semantic conventions say `dotnet.gc.heap.generation`, also read). Counted exclusively per generation; gen0 and gen1 are `ignored_series` | `process.runtime.dotnet.gc.collections.count{generation=gen2}`, same |
| GC old time | `dotnet.gc.pause.time`: the pause time of **every generation** (.NET does not split it by generation) | `process.runtime.dotnet.gc.duration` (ns, .NET 7+), same |
| JVM CPU | increase of `dotnet.process.cpu.time{cpu.mode}` over the elapsed time times `dotnet.process.cpu.count`; with the process instrumentation, its `process.cpu.time{cpu.mode}` (`otel` dialect) in the same way. The first export gives none. No System CPU (-1) | none (the process instrumentation sends no CPU count) |
| Total Thread | `process.thread.count` of the process instrumentation (`otel` dialect). The thread pool's `dotnet.thread_pool.thread.count` is not the process's threads and is not mapped | same |
| Response Time | `http.server.request.duration` (ASP.NET Core 8+ emits it by default) | same |

Not mapped (`unknown_metric`): JIT, lock contention, timer, assembly, exception and thread pool counts,
allocated bytes, fragmentation, committed size, working set, `process.memory.*`, Kestrel and routing
metrics. The .NET SDK sets no `process.*` resource attributes, so the agent start time is the SDK
start.

Spring Boot Actuator setup (Boot 3.x, `micrometer-registry-otlp` on the classpath). Boot sets
`service.name` from `spring.application.name` but **no instance id**, so one of `service.instance.id`
/ `pinpoint.agentId` must be given or the Resource is rejected as `invalid_resource`:

```yaml
management:
  opentelemetry:
    resource-attributes:
      "[service.instance.id]": ${random.uuid}      # -> agentId  (or "[pinpoint.agentId]": my-agent-01)
      "[pinpoint.serviceName]": my-service         # optional, DEFAULT when absent (service.namespace is not used)
  otlp:
    metrics:
      export:
        url: http://<otlptrace-collector>:9997/v1/metrics
        step: 60s
        aggregation-temporality: delta             # exact Response Time max; needs one collector per agent (see Collector instances)
```

Applications that run the Pinpoint agent must **not** send their Micrometer metrics here: the agent
already reports the native Inspector stat, and its own `profiler-micrometer` exporter identifies the
service through `service.namespace`, which this path does not read, so the metrics would register a
second agent under DEFAULT.

Temporality: the SDK default (CUMULATIVE) works. Fields the Inspector stores as running totals
(`gcOldCount`, `unLoaded`) are taken as is; `responseTime` is differenced against the
previous point of the series, so the **first export of a series produces no response time**
(`point.dropped{reason=no_previous_point}`), and a process restart (`start_time` change) starts a
new baseline. DELTA input is used directly for `responseTime` and re-accumulated for the totals (a
DELTA point's `start_time` is the start of its interval and advances every export; it is not read as a
restart — a restart is a new agent start time). All series of such a field add into one agent-level total, and an export that carries no point of the field (the OTel SDK sends none under DELTA when nothing was recorded) keeps the total as it stood at that collection cycle. The stored totals of an agent never fall in time order (the web reads a fall as a restart and would draw the whole total as one interval): an increment that arrives after a later export was already stored (an upstream queue retrying or reordering) is carried by the next collection cycle instead of its own. Only a repeat of a point already seen (same series, same time: a retried export) is left out; a late interval is added. The GC type comes from the collector names, or from the heap pool names in a cycle without a collector point (under DELTA the OTel SDK leaves a collector that collected nothing out of the export).
Collector instances: this state (the CUMULATIVE baselines, the DELTA totals) lives in the memory of one collector process. With several OTLPTRACE collectors behind a load balancer that spreads the requests of one agent, CUMULATIVE still works (a collector that missed the previous point of a series skips one point, and the response time of a point may span more than one interval), but **DELTA does not**: every collector adds only the increments it received, the stored totals jump between partial sums, and the GC charts show phantom collections. Use CUMULATIVE (the SDK default) in that setup, or route each agent to one collector, or convert upstream (the OTel Collector's `deltatocumulative` processor).
Export interval: the OTel default (60 s) gives one Inspector point per minute.
Duration units: the metric's `unit` is honoured (`s`, `ms`, `us`, `ns`, `min`, `h` and the spelled-out
names such as `milliseconds`); an empty unit means seconds (the semantic-convention default) and an
unknown unit drops the point as `invalid_point` rather than guessing a scale. A point stamped more
than 5 minutes ahead of the collector's clock is `invalid_point` as well: a series' state only moves
forward in time, so a point from a client clock that jumped ahead would otherwise make every later
point of that series look already covered. Negative or non-finite
values are `invalid_point` too, except a negative `jvm.memory.limit`, which means "no limit". A
histogram without the optional `sum` is `invalid_point` (its mean and total time are unknown).
Points that carry no value — the `FLAG_NO_RECORDED_VALUE` flag (an upstream staleness marker) or a
number point without a value field — are `no_recorded_value`, not zeros. A dropped point never
moves a series' state, so the next point is still measured from the last usable one.
Instrumentation scope: the scope (name and version) is part of a series' identity, as in the OTLP
data model. One source per Inspector value: when an agent's Resource carries the same value (heap,
GC, threads, classes, buffers, CPU, file descriptors) from more than one source — a source is a
scope with its dialect — only one source is used and the other points are `duplicate_source`,
whatever their names and attributes. This keeps the OTel javaagent's Micrometer bridge
(`otel.instrumentation.micrometer.enabled=true`, Micrometer names) and its JMX Insight JVM rules
(`otel.jmx.target.system=jvm`, the OTel names, `jvm.thread.count` without attributes) from counting
the JVM twice next to the runtime-telemetry metrics. Ranking: the `io.opentelemetry.runtime-telemetry*`
scopes, then the `otel` dialect, then `micrometer`, then the scope name. The choice is remembered per
agent and value for the state retention period, so a source that leaves a series out of one export
(DELTA without recordings) does not hand the value to another source; a source not seen for that
period gives way. Server request durations and connection pools of different scopes of one dialect
are different requests or pools and add up; of the two dialects (the bridge's `http.server.requests`
or `hikaricp.*` next to the agent's own instrumentation) they are the same ones, and only the `otel`
one is used.

A Resource that only sends metrics is registered in AgentInfo / the application index like the first
span would be, under the OTel application type, so it appears in the Inspector agent list. Its start
time is, best source first: the `process.creation.time` resource attribute, a start-time metric
(Micrometer `process.start.time`), the earliest `start_time_unix_nano` of its points (the SDK start),
else 0 (`agent-start-time{source=creation-time|start-time-metric|point-start-time|none}`). Only CUMULATIVE Sum and Histogram points count for the third source: a DELTA point's `start_time` is its interval start and would move every export, a gauge's may be the previous collection, and a `start_time` before 2000 or ahead of the collector's clock is not a time.
Each of these start times is fixed for the life of the process, so it is part of the metric path's
registration key: a process restarted under the same agentId registers its new session. With no
start time (0) the agent is registered once. The trace path keeps registering once per agent (its
span start time fallback changes from span to span and cannot tell a restart apart), in its own key
space: an application that sends spans and metrics gets a row from each path on its first run (the
first span, the SDK start; the same row when both resolve `process.creation.time`), and from the
metric path alone on a restart. The web shows the latest row. Metrics alone give two rows in one
case: with the OTel javaagent's Micrometer bridge on (`otel.instrumentation.micrometer.enabled=true`), the bridged `process.start.time`
is missing from the first export, so that export registers the SDK start and the next ones the JVM
start (a few seconds earlier). The agent list then shows two sessions for one process, and each series
starts over once (`no_previous_point`). Nothing is lost after the second export; setting
`process.creation.time` (or leaving the bridge off) avoids it.

| Property | Default | Notes |
|---|---|---|
| `pinpoint.collector.otlptrace.inspector.enabled` | `false` | Registers the receiver. Lives in `pinpoint-otlptrace-grpc-root.properties` / the profile files. |
| `pinpoint.collector.otlptrace.inspector.dialects` | `otel,micrometer,go,nodejs,python,dotnet` | Naming dialects understood, in precedence order. Drop one to ignore its names. |
| `pinpoint.modules.collector.inspector.enabled` | `true` (application.yml) | **Prerequisite**: the Inspector storage module (`InspectorCollectorConfig`: Kafka + Pinot, the same beans the BASIC collector uses for agent stat). When it is off the receiver is not registered and the collector logs a WARN at startup (`OtlpMetricInspectorGateReporter`). |
| `pinpoint.collector.otlptrace.inspector.delta-cache.max-size` / `.expire-after-access` | `100000` / `10m` | Per-series state for the temporality conversion above (one entry per agent × metric × attribute set). |
| `pinpoint.collector.otlptrace.inspector.admission.max-in-flight-bytes` | 32 MB | gRPC in-flight byte budget for metrics alone (UNAVAILABLE when exhausted). |
| `pinpoint.collector.otlptrace.inspector.http.*` | see root properties | `/v1/metrics` admission: request size (413), concurrency and in-flight bytes (503 + Retry-After), decompression cap. |
| `collector.receiver.grpc.otlp.metric.worker.executor.*` | 4 / 4 / 1024 | Worker pool of the gRPC metrics service (profile files). |

Metrics (`collector.otlptrace.inspector.*`, tag `transport=grpc|http`): `point.received`,
`point.accepted{dialect=otel|micrometer}`, `point.mapped{type=jvmGc|cpuLoad|...}`,
`point.dropped{reason=unknown_metric|unsupported_type|no_previous_point|ignored_series|duplicate_source|no_recorded_value|invalid_point}`,
`agent-start-time{source}`,
`resource.rejected{reason=invalid_resource|service_not_found}` (reported back to the exporter as
`rejected_data_points`), `stat.stored`, `insert.error{op=agentStat|agentInfo}`, plus the admission
slice (`request.rejected`, `request.bytes`, `admission.*`) and `grpc.server.*{service=otlpmetric}`.

## Sender side: the `pp` tracestate entry

The caller → callee edge on the server map comes from a `pp=...` entry the **sender** writes
into the W3C `tracestate` header of its outgoing spans:

```
tracestate: pp=svc:order-team;app:order-api;type:1010
```

When the next service exports its spans to this collector, `PinpointTraceStateParser` reads
that entry off the receiving span and stamps `parentApplicationName` /
`parentServiceName` / `parentApplicationServiceType`, which is what lets ApplicationMap render
the upstream node and the edge. The sender side lives in its own repository,
[pinpoint-apm/pinpoint-otel-extension](https://github.com/pinpoint-apm/pinpoint-otel-extension):
the OpenTelemetry Java agent extension, the wire contract
([`docs/contract.md`](https://github.com/pinpoint-apm/pinpoint-otel-extension/blob/main/docs/contract.md)),
and the guides and reference samplers for applications without the agent (Java SDK, Python,
Go, Node.js). The collector asserts the parsing direction of the same contract file,
`src/test/resources/pp-tracestate-contract.txt` of `otlptrace-collector`
(`TraceStateSpecConsistencyTest`); a change to the format updates the file in both
repositories and bumps its `contract-version`.

## Link index: slot quota

When the link index is on (`pinpoint.collector.otlptrace.link-index.enabled`), each collector writes at most
`max-entries-per-slot` entries per one-minute slot, and one CLIENT identifier (application + destination) at most
`max-entries-per-identifier-percent` of that (default 20 %; 100 = no per-identifier limit). The SERVER entries of one
application can be capped the same way (`max-server-entries-per-application-percent`, off by default).

Both limits are first come first served within the minute, not a uniform sample: once a limit is reached, the rest
of the minute is not indexed, so the server-map counts of that slot under-represent its later part. The identifier
share applies even while the slot quota has room, so a single legitimate identifier above the share (200,000 calls per
minute and collector with the defaults) is cut. Operators can tell when a slot's counts are incomplete from
`collector.otlptrace.linkindex.dropped{reason=identifier_limit|slot_limit}`, the `quota.limited.identifiers`,
`slot.limit.reached.second` and `quota.summary.slot` gauges, and the INFO summary the collector logs when a slot ends
(the identifiers that reached their share, and the largest identifiers when the slot quota was reached).

## Build

```sh
./mvnw install -pl otlptrace -am -Dmaven.test.skip=true -Dbuild.frontend.skip=true
```
