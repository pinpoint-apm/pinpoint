---
title: Roadmap
keywords: roadmap, future
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: roadmap.html
disqus: false
---

## 2017 Roadmap
* Server Map Enhancement
  * Performance
    * Improve query speed through parallelism via asynchronous I/O operation, and code optimization
    * Change/introduce a new data structure more suitable for dealing with a large number of agents
  * Realtime
    * Improve realtime update/rendering
  * Support grouping of multiple applications
* Scatter Chart Enhancement
  * Introduce grouping by type of errors (db access fail, rpc fail, cache access fail, etc)
* Statistics/Aggregation
  * Introduce realtime data pipeline (Apache Flink) for statistics and data aggregation
  * Application-level min/max statistics and response time histograms
  * Statistics by request URLs
* Agent
  * Active thread dump
  * Collect DataSource information
  * Improve asynchronous trace support
    * Add vert.x support
  * Introduce agent trace data format v2
    * Type optimization & compressed format
    * Protocol buffer 3
  * Improve interceptor and thread local lookup performance
  * Introduce adapters for different interceptor types/patterns
  * Adaptive sampling
  * Adaptive callstack tracing
    * Discard relatively insignificant method invocations
    * Combine multiple highly similar/identical callstacks when sending them over the wire
  * Ability to store stack traces
  * Introduce log-level histograms
* UI/Usability
  * Improve performance
  * Migrate to AngularJS 2
  * Improve personalized configuration for users
* HBase
  * Data store optimization - Reduce rowkey sizes