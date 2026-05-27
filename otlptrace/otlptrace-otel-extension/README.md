# pinpoint-otlptrace-otel-extension

OpenTelemetry Java agent extension that injects a Pinpoint-aware `pp=...` entry
into the W3C `tracestate` header, so a Pinpoint OTLP trace collector receiving
spans from a downstream service can identify the upstream service / application /
service type.

This is the **sender-side** companion to the receiver-side parsing implemented in
`otlptrace-collector` (`OtlpTraceMapperUtils.getId()` and
`PinpointTraceStateParser`). Both ends consume the **same attribute keys**, so a
single `OTEL_RESOURCE_ATTRIBUTES` entry drives both.

## How it works

At SDK startup, the extension's `AutoConfigurationCustomizerProvider`
(`PinpointAutoConfig`) wraps the configured `Sampler` with
`PinpointTraceStateSampler`. The wrapper preserves the delegate's sampling
decision and adds a `pp=svc:<svc>;app:<app>[;type:<code>]` entry to the trace
state of every span. The W3C `tracestate` propagator then emits that entry on
every outgoing HTTP / gRPC carrier.

## Recommended deployment (single source of truth)

Set the Pinpoint identifiers **once** on the standard
`OTEL_RESOURCE_ATTRIBUTES`:

```sh
java \
  -javaagent:opentelemetry-javaagent.jar \
  -Dotel.javaagent.extensions=/path/to/pinpoint-otlptrace-otel-extension-4.0.0-SNAPSHOT.jar \
  -Dotel.service.name=order-api \
  -Dotel.resource.attributes=pinpoint.applicationName=order-api,pinpoint.serviceName=order-team,pinpoint.applicationType=1010 \
  -jar app.jar
```

Pure OTel semantic-convention deployments also work — if you only set the
standard `service.name` / `service.namespace` resource attributes (no
`pinpoint.*` keys at all), both the extension and the collector pick those up
via the same fallback chain. See [Lookup order](#lookup-order) below.

This one setting feeds both ends:

- **Local OTLP spans** — the collector's `OtlpTraceMapperUtils.getId()` reads
  `pinpoint.applicationName` / `pinpoint.serviceName` straight off the incoming
  Resource block and uses them as the span's `applicationName` / `serviceName`.
- **Outgoing tracestate** — the extension reads the same keys via OTel
  `ConfigProperties.getMap("otel.resource.attributes")` and emits
  `tracestate: pp=svc:order-team;app:order-api;type:1010`. The downstream
  collector parses this into `parentApplicationName` / `parentServiceName` /
  `parentApplicationServiceType` on the receiving span.

## Configuration keys

| Pinpoint key (primary) | OTel semconv fallback | Collector counterpart | Notes |
|---|---|---|---|
| `pinpoint.applicationName` | `otel.service.name`, then `service.name` (resource attr) | `OtlpTraceMapperUtils.KEY_APPLICATION_NAME` | Required (at least one of the four resolves) |
| `pinpoint.serviceName` | `service.namespace` (resource attr) | `OtlpTraceMapperUtils.KEY_PINPOINT_SERVICE_NAME` | Pinpoint multi-tenant service grouping |
| `pinpoint.applicationType` | (none) | (sender-only) | Numeric Pinpoint ServiceType code; e.g. `1010` for WAS. When absent, the collector defaults the parent to `OPENTELEMETRY_SERVER` (1220). |

Pinpoint-specific keys follow the camelCase convention used in the collector's
`OtlpTraceMapperUtils` constants so the two ends literally share strings. The
fallback chain mirrors the collector's own
`getApplicationName()` / `getServiceName()` exactly, so a deployment that uses
*only* the standard OTel semconv keys (`service.name`, `service.namespace`)
still produces identical identifiers on both sides.

## Lookup order

For each value the extension walks the priority list below. For every key in
the list, it tries the dedicated config property first (`-Dservice.namespace=...`),
then the `OTEL_RESOURCE_ATTRIBUTES` entry with the same name; only when both
miss does it move on to the next key.

For `applicationName`:

1. `pinpoint.applicationName`
2. `otel.service.name`
3. `service.name`

For `serviceName`:

1. `pinpoint.serviceName`
2. `service.namespace`

For `applicationType`:

1. `pinpoint.applicationType` (only — sender-only field)

## ⚠ Warning — `-Dpinpoint.X` overrides cause node divergence

The `-D` override on this extension affects **only** what gets written into the
outgoing `tracestate`. It does **not** modify the Resource attributes the OTel
SDK builds for *this* process's spans — those still come exclusively from
`OTEL_RESOURCE_ATTRIBUTES`.

Concrete example:

```sh
-Dotel.resource.attributes=pinpoint.applicationName=order-api,pinpoint.serviceName=order-team
-Dpinpoint.applicationName=order-api-canary    # override
```

Effect:

```
                  ┌──────────────── Java process ────────────────┐
                  │                                              │
own spans ────────┼──► Resource (OTEL_RESOURCE_ATTRIBUTES only)  │
                  │       pinpoint.applicationName=order-api     │
                  │                                              │
outgoing requests ┼──► tracestate (override wins)                │
                  │       pp=...;app:order-api-canary            │
                  └──────────────────────────────────────────────┘
                          │                            │
                          ▼                            ▼
                  ┌───────────────────┐      ┌───────────────────┐
                  │ Pinpoint collector│      │ Downstream service│
                  │ → node "order-api"│      │ → spans see parent│
                  │   for own spans   │      │   "order-api-canary"
                  └───────────────────┘      └───────────────────┘
```

ApplicationMap shows the same physical process as **two separate nodes**, and
the outgoing call edge is attributed to the override name. Prefer editing
`OTEL_RESOURCE_ATTRIBUTES` itself for any production-visible change; reserve
`-Dpinpoint.X` for tests and one-off diagnostics.

To change both ends consistently, edit `OTEL_RESOURCE_ATTRIBUTES`:

```sh
-Dotel.resource.attributes=pinpoint.applicationName=order-api-canary,pinpoint.serviceName=order-team
```

## Disabled mode (no-op)

If none of `pinpoint.serviceName`, `pinpoint.applicationName`, or
`otel.service.name` resolves, the extension wraps nothing and returns the SDK's
sampler untouched. Agent startup is unaffected. A single INFO log entry records
the disabled state for diagnostics.

## Format on the wire

```
tracestate: pp=svc:<serviceName>;app:<applicationName>;type:<serviceTypeCode>
```

- Key `pp` — chosen to align with other vendors' 2-letter convention
  (`dd`, `nr`, `dt`, `ot`).
- Sub-keys separated by `;`, sub-key / value separated by `:`, mirroring the
  OTel-defined `ot=th:...` and Datadog `dd=s:1;t.dm:-4` formats.
- Any sub-key may be omitted; unknown sub-keys are ignored by the collector
  parser, so the format can grow without breaking existing services.

## Build

The extension targets **JDK 8** so it can run inside any OTel-instrumented
application. It depends only on `opentelemetry-sdk` / `-trace` /
`-extension-autoconfigure-spi` — all marked `provided`, since the OTel Java
agent supplies them at runtime.

```sh
./mvnw install -pl otlptrace/otlptrace-otel-extension -am -Dmaven.test.skip=true -Dbuild.frontend.skip=true
```

The resulting jar at `otlptrace/otlptrace-otel-extension/target/pinpoint-otlptrace-otel-extension-4.0.0-SNAPSHOT.jar`
is what you point `-Dotel.javaagent.extensions` at.

## Verifying

After deploy, capture an outgoing request and look for the header:

```
tracestate: pp=svc:order-team;app:order-api;type:1010
```

On the Pinpoint OTel collector, the incoming span's mapped `SpanBo` will then
have:

- `parentApplicationName = "order-api"`
- `parentApplicationServiceType = 1010`
- `parentServiceName = "order-team"`

ApplicationMap renders the upstream node and the edge accordingly.
