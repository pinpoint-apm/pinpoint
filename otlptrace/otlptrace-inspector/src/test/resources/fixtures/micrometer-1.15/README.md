# Micrometer OTLP fixtures

Raw `ExportMetricsServiceRequest` bodies captured from a Spring Boot 3.5.16 application
(`spring-boot-starter-actuator` + `micrometer-registry-otlp`, Micrometer 1.15.12, no OTel javaagent)
on 2026-11-02, Windows 11, OpenLogic OpenJDK 17.0.14, G1. `step=10s`. The application served a few
`GET /ok`, `/sleep`, `/throw` requests between exports, so `http.server.requests` has observations.

| File | Export settings |
|---|---|
| `cumulative-1.pb`, `cumulative-2.pb` | defaults (CUMULATIVE), two consecutive exports 10 s apart |
| `delta.pb` | `management.otlp.metrics.export.aggregation-temporality=delta` |
| `cumulative-histogram-1.pb`, `-2.pb` | CUMULATIVE + `management.metrics.distribution.percentiles-histogram.http.server.requests=true` |

Captured with a plain HTTP server that stores the request body (no gzip was sent). The resource
attributes are Micrometer's defaults plus `service.name` (= `spring.application.name`) and a
`service.instance.id`; `process.files.open` is absent because the JVM ran on Windows. Micrometer's
`disk.*` metrics are left out (their `path` attribute is a local path). The metrics themselves are as
sent.
