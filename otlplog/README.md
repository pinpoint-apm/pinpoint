# pinpoint-otlplog

OTLP logs receiver for Pinpoint's Error Analysis. It accepts OpenTelemetry **log records** over
OTLP/gRPC and OTLP/HTTP, keeps the ones that describe an exception, and stores them as Error
Analysis records — the same `exceptionTrace` table and web views as the exceptions the OTLP trace
collector derives from `exception` span events.

## Why

OpenTelemetry is moving exception reporting from span events to log records: semantic conventions
1.44 mark the span-event conventions *deprecated* and define the log-record ones as stable, with an
`OTEL_SEMCONV_EXCEPTION_SIGNAL_OPT_IN=logs|logs/dup` switch for instrumentations. The Java agent
exposes it as `otel.semconv.exception.signal.preview=logs|logs/dup` (2.29+) and since 2.29–2.31
emits the domain exception events (`http.server.request.exception`, `db.client.operation.exception`,
`rpc.*.call.exception`, `messaging.*.exception`, `faas.invocation.exception`,
`gen_ai.client.operation.exception`) as log records. Once an application switches, its `exception`
span events disappear while traces keep flowing — the OTLP trace collector then has nothing to feed
Error Analysis. Log records also carry a class of exceptions that span events
never did: the ones an application catches and only *logs*. Java, Python, Go (`otelslog`) and Node
(`winston`) appenders all attach `exception.type` / `exception.message` / `exception.stacktrace` and,
inside an active span, `trace_id` / `span_id`.

## Module

| Module | Role |
|---|---|
| `otlplog-collector` | Spring configuration (`OtlpLogCollectorModule`) hosted **inside the OTLPTRACE collector process**: `LogsService/Export` on the trace collector's gRPC servers (`:9998`, TLS `:9448`), `POST /v1/logs` on its servlet port (`:9997`). |

There is no `CollectorType.OTLPLOG`. OTLP exporters send every signal to one endpoint (gRPC one port,
HTTP `<base>/v1/<signal>`) and gRPC routes by service name, so a separate port would force per-signal
endpoint configuration on every application. The module depends on `otlptrace-collector` (resource
resolution, attribute utilities, stack trace parsers, HTTP admission filters, signal-neutral metric
types), so the trace module cannot import it; `collector-starter` registers it next to
`OtlpTraceCollectorApp`.

## Enabling

```properties
# collector-starter application.yml (default false) or external config
pinpoint.modules.collector.otlplog.enabled=true
# Error Analysis storage (Kafka -> Pinot exceptionTrace); without it records are received and dropped
pinpoint.modules.collector.exceptiontrace.enabled=true
```

The gate is evaluated before the module's own property files are imported, so it must come from the
starter configuration or external config, not from `pinpoint-otlplog-root.properties`. With the gate
off the whole module is skipped and the OTLPTRACE collector behaves exactly as before. The module also
requires its host, `pinpoint.modules.collector.otlptrace.enabled=true` (the default): with the trace
module off there are no gRPC servers or exception mapper to wire into, so the logs receiver is skipped
as well rather than failing the OTLPTRACE context.

Point the OTel SDK / agent at the same endpoint it already uses for traces, turn on the logs
exporter, and route instrumentation exceptions to logs, e.g. for the Java agent (its default
protocol is `http/protobuf`, so the servlet port is the default target; `/v1/traces` and `/v1/logs`
are appended automatically):

```sh
-Dotel.logs.exporter=otlp \
-Dotel.exporter.otlp.endpoint=http://pinpoint-otlptrace-collector:9997 \
-Dotel.semconv.exception.signal.preview=logs
# logs/dup keeps the span events too during migration
# gRPC instead: -Dotel.exporter.otlp.endpoint=http://...:9998 -Dotel.exporter.otlp.protocol=grpc
```

Without the `signal.preview` switch the instrumentation keeps recording exceptions as span events
(handled by the trace path) and only the exceptions the application itself logs through an OTel
appender (`log.error(msg, e)`) arrive here; the appender records are sent regardless of the switch.
The application must also export traces to this collector: application and agent registration
(web application list, agent info) comes from the trace path, so a logs-only application is stored
in Error Analysis but is not listed in the web UI.

To send only exceptions, set the appender threshold to `ERROR` (Java:
`otel.instrumentation.logback-appender.*` / `log4j-appender.*`); the receiver drops everything else
anyway, but not shipping it saves bandwidth on both sides.

## Pipeline

```
LogsService/Export (gRPC)            POST /v1/logs (HTTP, protobuf, gzip)
  admission: in-flight bytes           filters: 413 size cap, 503 concurrency / in-flight bytes, gzip inflate
  worker pool                          servlet thread
        └──────────────► OtlpLogExportService.export ◄──────────────┘
             for each ResourceLogs
               resolve (application, agent) like the trace path      -> invalid_resource*
               for each LogRecord
                 any `exception.*` attribute key?                    -> no_exception
                 scope / application blacklist                       -> blacklisted
                 valid trace_id (16B) and span_id (8B)?              -> no_trace_context*
                 trace flags sampled bit clear, storing disabled?    -> unsampled_context
                 in-request de-dup on (traceId, spanId, type)         -> duplicate
                   (type = exception.type, else error.type; a record with neither is never a duplicate)
             for each surviving record
               OtlpLogExceptionMapper -> ExceptionMetaDataBo         -> no_exception_type*, mapping_error*
               ExceptionMetaDataService.save (Kafka -> Pinot)        -> storage_unavailable / store.error
```

Reasons marked `*` are client faults and are reported back in `ExportLogsPartialSuccess`
(`rejected_log_records`, `error_message`). The others are the receiver's own selection and are
accepted silently, so exporters do not warn about them. Note that the Java SDK exporters (agent
2.31.1 measured) treat any HTTP 2xx / gRPC OK as success and never read `partial_success`, so even
the client-visible rejections surface only in the OTel Collector's exporter logs (it does log partial
success) and in `record.dropped{reason}` here. In particular an exception logged outside any span
(a plain `new Thread`, a startup hook) has no trace context and is dropped as `no_trace_context`.
The selection rule — presence of any
`exception.*` key — was measured on 2,792 records from the OTel demo and four language probes:
precision and recall 100%, ~1% pass rate. Severity-based selection let infrastructure errors through
(or, ANDed, lost WARN-level client exceptions); event-name-based selection lost every appender record.

## Mapping (`OtlpLogExceptionMapper`)

| `ExceptionMetaDataBo` | Source | Notes |
|---|---|---|
| transactionId | `trace_id` | `OtelServerTraceId`, same as the trace path — the record joins the same transaction |
| **spanId** | **the record's own `span_id`** | The trace path stores the transaction *root* span id here so the web can link to the stored root span. A LogRecord carries no parent/root information, the ingest path must not read HBase to find it, and a span cache would rarely hit (logs are batched every 1s, spans every 5s). The detail view keys on the stored `(transactionId, spanId, exceptionId)`, so it works; only the call-tree link differs. |
| exceptionId | `span_id` | Same rule as the trace path (exception-bearing span id) |
| serviceType | `OPENTELEMETRY_SERVER` | |
| service / application / agent | resource attributes | Same resolver and `application-name-fallback` flag as the trace path |
| uriTemplate | `http.route` on the record, else `""` | Never a raw path and never null (Pinot would store its `"null"` sentinel and the UI would show it literally); sanitized like the trace path (query string / fragment dropped, control characters removed, capped at `uri-template-max-bytes`); neither appender records nor the semantic-convention exception events carry `http.route`, so this is empty in practice |
| exceptionClassName | `exception.type`, else `error.type` | Neither → `no_exception_type`; capped at `type-max-bytes` (deterministic, so equal types stay equal) |
| exceptionMessage | `exception.message` → string body → `""` | Capped at `exception.message-max-bytes` |
| startTime | `time_unix_nano` → `observed_time_unix_nano` → receive time | Both were seen empty in practice |
| stackTraceElements | `exception.stacktrace` through the trace path's language parsers | May be empty (Go emits type and message only). Input is bounded first by the trace path's `stacktrace.max-chars` / `line-max-chars` — the regex parsers are quadratic on a single hostile line, so one multi-megabyte line would otherwise pin a worker for hours |
| exceptionDepth | 0 | Chain decomposition is out of scope |

### Unsampled traces

The low 8 bits of `LogRecord.flags` are the W3C trace flags of the span the record was logged in. A
record with a valid trace context but a clear sampled bit belongs to a trace the SDK sampler dropped,
so no span will ever be stored and the transaction link is dead. By default such records are dropped
(`unsampled_context`); `pinpoint.collector.otlplog.exception.store-unsampled=true` stores them for the
Error Analysis aggregates and counts them in `record.unsampled_context`. The proto cannot distinguish
"flags not set" from "not sampled"; every SDK measured (Java, Python, Go, Node) sets `flags=1` inside a
sampled span.

### De-duplication

One exception is typically emitted twice by the same SDK — by the instrumentation
(`recordException` routed to logs) and by the appender the application's `log.error(msg, e)` goes
through — with the same `(trace_id, span_id, exception.type)`. Both pass the same
BatchLogRecordProcessor, so they almost always share an export request; the receiver keeps one per key
per request, preferring the record with the longer stack trace. There is deliberately **no cache across
requests or across signals**: the receiver's memory must not grow with log volume. A duplicate that
straddles a batch boundary, or an `exception` span event of the same span during the client's
migration, is stored twice.

## Configuration

`otlplog/collector/pinpoint-otlplog-root.properties`, overridden by
`profiles/<profile>/pinpoint-otlplog.properties`.

| Key | Default | Meaning |
|---|---|---|
| `pinpoint.modules.collector.otlplog.enabled` | `false` | Module gate (starter `application.yml` / external config) |
| `pinpoint.collector.otlplog.filter.blacklist.scopes` / `.applications` | empty | Comma-separated prefixes of instrumentation scope names / application names to drop |
| `pinpoint.collector.otlplog.exception.store-unsampled` | `false` | Store records whose trace was not sampled |
| `pinpoint.collector.otlplog.exception.message-max-bytes` | 2048 (same as the trace path; falls back to the trace key when unset) | Byte cap for the message |
| `pinpoint.collector.otlplog.exception.type-max-bytes` | 1024 (same as the trace path; falls back to the trace key when unset) | Byte cap for `exception.type` |
| `pinpoint.collector.otlplog.exception.uri-template-max-bytes` | 1024 (same as the trace path; falls back to the trace key when unset) | Byte cap for `http.route` (also sanitized) |
| `pinpoint.collector.otlplog.admission.max-in-flight-bytes` | 64MB | gRPC in-flight byte budget for logs (trace: 256MB) |
| `pinpoint.collector.otlplog.http.max-request-bytes` | 4MB | 413 above this Content-Length |
| `pinpoint.collector.otlplog.http.max-decompressed-request-bytes` | 16MB | gzip inflate cap (400 above) |
| `pinpoint.collector.otlplog.http.max-concurrent-requests` | 32 | 503 + Retry-After above this |
| `pinpoint.collector.otlplog.http.admission.max-in-flight-bytes` | 64MB | HTTP in-flight byte budget |
| `pinpoint.collector.otlplog.http.rejected.retry-after-seconds` | 1 | |
| `collector.receiver.grpc.otlp.log.worker.executor.*` | core 4 / max 4 / queue 256 | Log worker pool (profile file) |

Shared with the trace service and **not** configurable per signal, because both are served by one gRPC
server: `collector.receiver.grpc.otlp.trace.inbound_message_size_max` (4MB),
`concurrent-calls_per-connection_max`, flow control and keepalive settings. A log batch above the
inbound limit is refused with RESOURCE_EXHAUSTED like a trace batch.

The budgets are deliberately smaller than the trace path's: ~99% of records are dropped at the
attribute key scan before any mapping, and a parsed request lives only while its task runs. Re-size
from `collector.otlplog.request.bytes` (max) and `record.received` (rate) after rollout; the emergency
brake is the module gate (restart) or a blacklist entry.

A client that compresses (`otel.exporter.otlp.compression=gzip`) sends chunked HTTP bodies without
`Content-Length`; the admission filter then reserves `http.max-request-bytes` (4MB) per request, so
the 64MB HTTP in-flight budget admits about 16 concurrent compressed requests. Watch
`admission.inflight.bytes` and raise the budget when many gzip clients report to one collector.

## Metrics (`collector.otlplog.*`, tag `transport=grpc|http`)

| Meter | Tags | Meaning |
|---|---|---|
| `record.received` | transport | LogRecords in admitted requests, before selection |
| `record.stored` | transport | Records handed to Error Analysis storage |
| `record.dropped` | transport, `reason` | `no_exception`, `blacklisted`, `invalid_resource`, `no_trace_context`, `unsampled_context`, `duplicate`, `no_exception_type`, `mapping_error`, `storage_unavailable` |
| `record.unsampled_context` | transport | Stored records of unsampled traces (only with `store-unsampled=true`) |
| `request.rejected` | transport, `reason` | `inflight_bytes`, `executor_rejected` (gRPC); `inflight_bytes`, `concurrency`, `payload_too_large`, `unsupported_encoding`, `parse_error` (HTTP) |
| `request.bytes` | transport | Size of admitted requests — HTTP: wire bytes (`Content-Length`, or the bytes read for a chunked body; the compressed size when gzip); gRPC: serialized size after decompression |
| `admission.inflight.bytes` / `admission.limit.bytes` | transport | In-flight byte gate occupancy and budget |
| `admission.inflight.requests` / `admission.limit.requests` | http | Concurrency gate occupancy and cap |
| `store.error` | | Synchronous failures handing a record to the exceptiontrace store |
| `exception.truncated` | `field=message\|type\|uri_template` | Field cap / sanitizer applied. Stack trace input bounds are counted on the shared parser: `collector.otlptrace.exception.truncated{field=stacktrace_bytes\|stacktrace_line}` |

Reading them: `received - dropped{no_exception}` is the exception volume, `stored` is what reaches Error
Analysis, `duplicate` is the de-duplication effect, `request.bytes` max is the batch size to size the
budgets from. gRPC server metrics (`grpc.server.*`) for `LogsService/Export` carry `service=otlplog`.

## Input hardening

Every field a third-party exporter controls is bounded before it reaches a parser or the store, on both
signals: `exception.type` and `http.route` byte caps (route also loses any query string / fragment and
control characters), `exception.message` cap, stack trace whole-text and per-line caps in front of the
regex-based language parsers (their worst case is quadratic in the length of one line), frame count and
frame field caps. Values that fail the resource-id validators are echoed into logs and partial-success
messages only through `LogSafe` (control characters escaped, 256-character abbreviation), so a hostile
`service.name` cannot forge log lines or bloat the response. Admission (in-flight bytes, request size,
concurrency, gzip inflate cap) bounds memory; see the tables above.

## Out of scope

Non-exception (correlated) log storage, OTLP/JSON logs (the Java SDK refuses `http/json` for logs;
`application/json` is answered 415), exception chain decomposition, a call-tree link to log-path
exceptions (needs a transactionId reverse lookup), exceptions logged without a trace context, and
application/agent registration for logs-only applications (traces must be exported as well).
