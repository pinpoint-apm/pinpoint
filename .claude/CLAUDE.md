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

## Commit Message Convention

Format: `[#issue_number] Description` or `[#noissue] Description`

- Issue numbers are numeric only, or `noissue` (lowercase).
- Examples: `[#2314] Fix agent nbase-t plugin unknown db`, `[#noissue] Refactor AgentInfoService`


## Key Tech Stack

- **Java:** Java Agent (JDK 8+), Backend Services (JDK 17+)
- **Spring Boot:** 3.5.x (Spring 6.2.x)
- **gRPC:** 1.75.x with Protocol Buffers 3.25.x
- **Storage:** HBase 2.5.x, Apache Pinot 1.3.x
- **Frontend:** React/TypeScript, Node 22.x, Yarn 1.22.x
- **DTO Mapping:** MapStruct 1.6.x

## Frontend

Located at `web-frontend/src/main/v3/`. Built via Maven frontend plugin using Yarn. Can be skipped with `-Dbuild.frontend.skip=true`.
