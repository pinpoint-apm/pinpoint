# otlptrace-collector

The OTLP (OpenTelemetry Protocol) trace ingestion collector module. It receives the
`ExportTraceServiceRequest` sent by an OTLP exporter over gRPC/HTTP, converts it into Pinpoint's
SpanBo/SpanChunkBo, and stores it in HBase.

This document summarizes the analysis of the gRPC/HTTP ingestion layer and the worker/HBase storage
layer. (Analysis baseline: 2026-07 master branch)

---

## 1. Architecture Overview

```
OTLP exporter
  ├─ gRPC  :9998 (plaintext) / :9448 (TLS, opt-in)  ── GrpcOtlpTraceService
  └─ HTTP  :9997  POST /v1/traces (protobuf)        ── OtlpTraceController
                     │
                     ▼
             OtlpTraceMapper (OTLP span → SpanBo/SpanChunkBo/AgentInfoBo/ExceptionMetaDataBo)
                     │
                     ▼
             HbaseOtlpTraceService / HbaseOtlpAgentInfoService / HbaseOtlpApplicationIndexV2Service
                     │
                     ▼
             HBase (TraceV2, TRACE_INDEX, AGENTINFO, APPLICATION/AGENT_ID, server-map stats)
```

- Startup: the multi-app starter in `collector-starter` boots it as a child context when
  `CollectorType.OTLPTRACE` (`collector-starter/.../PinpointCollectorStarter.java:95-124`).
  servlet port 9997, gRPC 9998, TLS 9448.
- Module gate: `pinpoint.modules.collector.otlptrace.enabled=true` (`OtlpTraceCollectorModule.java:75`)
- Properties: `otlptrace/collector/pinpoint-otlptrace-grpc-root.properties` + per-profile overrides

---

## 2. gRPC Layer

### Server bootstrap

- Reuses the collector module's shared `GrpcReceiver`/`ServerFactory` (`OtlpTraceCollectorModule.java:120-142`).
  IP filter (`IgnoreAddressFilter`, skips L4 health-check IPs) and the TransportMetadata / StreamCount
  interceptors are installed by default.
- Service registration: a single service — `GrpcOtlpTraceService` + `MetricCollectingServerInterceptor`
  (tagged `service=otlptrace`).
- The SSL module (`OtlpTraceCollectorGrpcSslModule`) is opt-in (off by default). It binds a separate
  `GrpcReceiver` on 9448 that shares the same serviceList / executor / ServerOption as the plaintext
  receiver. Certificate config reuses the collector's shared prefix `collector.receiver.grpc.ssl.*`.

### Key settings (pinpoint-otlptrace-grpc-root.properties)

| Item | Value | Notes |
|---|---|---|
| inbound_message_size_max | 1MB | More conservative than the main collector's span receiver (4MB) |
| HTTP/2 flow-control window | 512KB | |
| Concurrent calls per connection | 128 | Conservative vs the main collector's 1024 |
| keepalive | time 30s / timeout 60s, permit 10s + without_calls | Handles OTLP exporter idle PINGs (avoids GOAWAY too_many_pings) |
| max header | 8KB | Accounts for the JWT Authorization header |
| server executor | 4 threads / queue 256 | Serves both the gRPC handler and HBase async callbacks |
| worker executor | 16 threads / queue 128 | Runs mapping + insert, AbortPolicy |
| in-flight byte admission | 256MB (Semaphore) | The code `@Value` default is 64MB — note the mismatch |

### Request handling flow (`GrpcOtlpTraceService.java:86-153`)

1. **Byte admission**: `tryAcquire` on the Semaphore for `request.getSerializedSize()`; on failure,
   return `UNAVAILABLE` immediately. The reason for using `UNAVAILABLE` instead of `RESOURCE_EXHAUSTED`
   (OTLP exporters retry it unconditionally) is documented in a comment.
2. **Worker offload**: submitted to the worker executor via `Context.current().wrap()` — non-blocking
   for the gRPC handler thread. On queue saturation (`RejectedExecutionException`), release the permit
   and return `UNAVAILABLE`.
3. **Cancellation check**: if the client has cancelled by execution time, skip mapping; the permit is
   released in a finally block.
4. **Response semantics**:
   - Server-side error (insert failure) → `UNAVAILABLE` for the whole batch (drives retry, prevents loss)
   - Client data fault (mapping reject) → OTLP-spec `ExportTracePartialSuccess` (rejectedSpans + errorMessage)
   - Unexpected exception in the worker → `INTERNAL` (prevents a poison-data retry storm)

---

## 3. HTTP Layer

- Endpoint: a single mapping `POST /v1/traces`, `consumes = application/x-protobuf`
  (`OtlpTraceController.java:67-68`). **OTLP/HTTP JSON encoding is not supported** (415),
  **Content-Encoding: gzip is not supported** (parse failure 400 — beware exporters that default to gzip).
- Body parsing: `ProtobufHttpMessageConverter` (`OtlpTraceCollectorHttpModule.java:36-40`)
- Executed synchronously on the Tomcat request thread (no worker offload)

### Admission Filter (`OtlpTraceHttpAdmissionFilter`)

Registered only for `/v1/traces` with `Ordered.HIGHEST_PRECEDENCE`, so it runs **before** protobuf
deserialization. A 3-stage gate:

1. Content-Length > 4MB → **413**. If the length is unknown (chunked), pessimistically reserve 4MB and
   block on overflow via `LimitedRequestWrapper`.
2. Concurrent-request Semaphore(64) failure → **503 + Retry-After (1s)**
3. in-flight byte Semaphore (256MB)

Permits are released in a finally block after the entire request (parse + insert) completes.
Config keys: `pinpoint.collector.otlptrace.http.*`

### gRPC vs HTTP comparison

| Item | gRPC | HTTP |
|---|---|---|
| Mapping/storage deps | Shares the same beans | Same |
| export logic | Shared `OtlpTraceExportService` (single implementation) | Same |
| agentId dedup cache | Shared Caffeine bean (`otlpAgentIdCache`, thread-safe, cross-transport) | Same |
| Execution thread | Worker executor offload | Synchronous on the Tomcat request thread |
| Backpressure | UNAVAILABLE | 503 + Retry-After |
| partial success | Responds per spec | **Not emitted — always 200 (bug)** |
| Server-side error | Drives retry via UNAVAILABLE | **Always 200 → silent loss (bug)** |

---

## 4. Worker / HBase Storage Layer

### Thread-pool chain (per request)

```
gRPC server executor (4 threads / queue 256)
  → [admission Semaphore: in-flight bytes 256MB]
  → worker executor (16 threads / queue 128, AbortPolicy)   ← runs mapping + insert
  → AsyncPollerThread[] (span: at least 4, queue 10000 each) ← actual HBase batch put (100 rows / 100ms)
  → (completion callback) returns to the server executor
```

- HBase writes: `hbase.client.put-writer=asyncPoller` — `AsyncPollingPutWriter` distributes to pollers
  by rowkey hash. On poller-queue saturation it returns a **failed future** carrying
  `RequestNotPermittedException(OVERFLOW)` (not a synchronous exception).
- `span-put-writer.concurrency-limit=0` on the span path (no rate limiter).
- Server-map stats: `BulkWriter` aggregates in memory and flushes every 5s (`SchedulerConfiguration`).

### Storage paths (per SpanBo)

| Path | DAO | Table/CF | Write mode |
|---|---|---|---|
| span body | `HbaseTraceDaoV2.asyncInsert` | `TraceV2` / CF `S`, rowkey=salted transactionId | async (returns future, observed via callback) |
| scatter index | `HbaseTraceIndexDao` (v2) | `TRACE_INDEX` / `TRACE_INDEX_META` | async — **future discarded** |
| server-map stats | `BulkWriter.increment` | link/response-stats tables | in-memory aggregation + 5s flush |
| store event | `SpanStorePublisher` | (Spring event) | published from the async callback |

spanChunk uses only `TraceV2`/CF `S` (no scatter index). AgentInfo (`AGENTINFO`/CF `Info`,
rowkey=`agentId+startTime`) and ApplicationIndex v2 (`APPLICATION`, `AGENT_ID`) are synchronous put +
Caffeine dedup (`otlpAgentIdCache`, maxSize 10000, shared across transports). serviceUid is always
`ServiceUid.DEFAULT` (TODO).

### Loss analysis

There is no internal retry; it relies entirely on the OTLP exporter's retries. The key question is
whether a failure **surfaces before the response**:

| Failure point | Outcome |
|---|---|
| admission / worker queue saturation | UNAVAILABLE → client retries, **no loss** |
| synchronous exception during mapping | UNAVAILABLE → retry, **no loss** |
| span put failure (incl. poller-queue overflow) | **Loss after a success response** — metric + throttled WARN only (Phase 1.5 constraint, documented at `HbaseOtlpTraceService.java:51-53`) |
| spanChunk put failure | **Fully ignored** — future discarded + success event always published |
| scatter index failure | future discarded, no metric — loss that is invisible in scatter |
| server-map stats | Up to 5s of increments lost on process death (acceptable by design) |
