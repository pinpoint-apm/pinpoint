# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Pinpoint is a distributed APM (Application Performance Management) system. It monitors large-scale Java applications via bytecode instrumentation.

## Architecture

**Data flow:** Agent (bytecode instrumentation on target app) → Collector (gRPC) → HBase/Pinot storage → Web API → React Frontend

Core components:
- **agent-module/** — Java agent with profiler, bootstrap, SDK, plugin loader, and 90+ instrumentation plugins (Spring, Kafka, Redis, HTTP clients, databases, etc.)
- **collector/** / **collector-starter/** — gRPC endpoint that receives trace/metric data from agents and writes to storage
- **web/** / **web-starter/** — Spring Boot REST API backend
- **web-frontend/** — React/TypeScript frontend (v3, built with Yarn)
- **batch/** / **batch-alarmsender/** — Spring Batch scheduled jobs for aggregation, cleanup, alarms
- **hbase/** — HBase schema management
- **hbase-uid-module/** / **service-module/** — Service layer implementations
- **pinot/** — Apache Pinot analytics integration
- **grpc/** — Protocol Buffers definitions and gRPC infrastructure
- **metric-module/**, **uristat/**, **realtime/**, **otlpmetric/**, **otlptrace/**, **exceptiontrace/**, **inspector-module/** — Feature modules
- **commons/**, **commons-buffer/**, **commons-config/**, **commons-profiler/**, **commons-hbase/**, **commons-server/**, **commons-mybatis/**, **commons-timeseries/** — Shared libraries

## Build Commands

Maven wrapper (`./mvnw`) is the build tool. This is a multi-module Maven project with 30+ top-level modules.

```bash
# Build everything (skip tests)
./mvnw install -Dmaven.test.skip=true

# Build specific modules with dependencies
./mvnw install -pl agent -am -Dmaven.test.skip=true
./mvnw install -pl collector -am -Dmaven.test.skip=true
./mvnw install -pl web -am -Dmaven.test.skip=true

# Build web without frontend
./mvnw install -pl web -am -Dbuild.frontend.skip=true -Dmaven.test.skip=true
```

## Testing

```bash
# Run all unit tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl commons

# Run a single test class
./mvnw test -pl commons -Dtest=ClassName

# Run a single test method
./mvnw test -pl commons -Dtest=ClassName#methodName

# Integration tests
./mvnw verify

# With coverage (JaCoCo)
./mvnw -Pcode.coverage package

# Plugin integration tests
./mvnw clean install -Pit-module -pl agent-module/plugins-it
```

Test stack: JUnit 5, Mockito 4, Spring Test, TestContainers, AssertJ.

## Commit Message Convention

Format: `[#issue-number] message`

```
[#1001] Refactor servermap
[#noissue] Update README.md
```

## Key Tech Stack

- **Java:** Agent module (JDK 8+), Server modules (JDK 17+)
- **Spring Boot:** 3.5.x (Spring 6.2.x)
- **gRPC:** 1.75.x with Protocol Buffers 3.25.x
- **Storage:** HBase 2.5.x, Apache Pinot 1.3.x
- **Frontend:** React/TypeScript, Node 22.x, Yarn 1.22.x
- **DTO Mapping:** MapStruct 1.6.x

## Frontend

Located at `web-frontend/src/main/v3/`. Built via Maven frontend plugin using Yarn. Can be skipped with `-Dbuild.frontend.skip=true`.
