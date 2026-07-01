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
| `otlptrace-collector` | Spring Boot collector. Listens for OTLP trace exports over gRPC (`:9998`) and HTTP (`POST /v1/traces`, `application/x-protobuf`), maps `ResourceSpans` into Pinpoint `SpanBo` / `SpanChunkBo` / `AgentInfoBo`, and inserts them into HBase. |
| `otlptrace-otel-extension` | OTel Java SDK extension that emits a `pp=...` entry into the W3C `tracestate` header so the collector can identify the upstream Pinpoint service / application on cross-process spans. See [its README](otlptrace-otel-extension/README.md). |

The collector is enabled by setting `pinpoint.modules.collector.otlptrace.enabled=true`.

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

1. `pinpoint.serviceName`
2. `service.namespace`
3. Defaults to Pinpoint's built-in default service when neither is set.

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
| `service.name` + `service.namespace` + `service.instance.id` | Pure OTel semconv. All identifiers resolve through the fallback chain. Good fit for OTel-native deployments. |
| `pinpoint.applicationName` + `pinpoint.serviceName` + `pinpoint.agentName` + `service.instance.id` | **Recommended.** Three explicit Pinpoint attributes plus a per-instance identifier (`uuidgen` on a VM, auto-populated by OTel SDK in K8s). |

Mixing is fine — set `pinpoint.*` only for the identifiers you want to pin and
let the rest fall through to the OTel semconv keys.

### Validation errors

When an identifier fails validation, the **span is rejected**: the OTLP export
call fails (gRPC `INVALID_ARGUMENT` / partial-success response on HTTP), the
rejected-span count is incremented, and the collector logs a message like one
of the following — the offending value is appended after `=`:

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

All of these share the same root cause: the value exceeds the **length cap**
or violates the **`[a-zA-Z0-9._-]+` pattern** in the limits table above. Fix
the offending attribute on the OTel agent side (no spaces, no special
characters, within the length budget) and the error goes away.

## otlptrace-otel-extension

A small **sender-side** OTel SDK extension that adds a `pp=...` entry to the
W3C `tracestate` header on every outgoing span:

```
tracestate: pp=svc:order-team;app:order-api;type:1010
```

When the next service in the call chain exports its spans to the Pinpoint OTLP
collector, the collector parses that entry and stamps the receiving span with
`parentApplicationName` / `parentServiceName` / `parentApplicationServiceType`,
which is what lets ApplicationMap render the upstream node and the edge
between the two services.

The extension and the collector share the same Resource-attribute lookup chain
(`pinpoint.applicationName` → `service.name`, etc.), so a single
`OTEL_RESOURCE_ATTRIBUTES` configuration drives both ends.

See [`otlptrace-otel-extension/README.md`](otlptrace-otel-extension/README.md)
for installation, format details, and the `-Dpinpoint.X` override caveat.

## Build

```sh
./mvnw install -pl otlptrace -am -Dmaven.test.skip=true -Dbuild.frontend.skip=true
```
