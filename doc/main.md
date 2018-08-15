---
title: "Pinpoint 1.8.0"
keywords: pinpoint release, 1.8.0
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 1.8.0

 - **Started to support Java 9/10 application monitoring.**
 - Installation Guide has been update. Build Requirement has been chaged(Default Java to JDK 8).
 - Cleaned up plug-in dependency

### Pinpoint Plug-in
 - Started to support Kafka
 - Started to support akka-http
 - Started to support Spring asynchronous communication
 - [Started to support WebLogic](https://github.com/naver/pinpoint/pull/2570)
 - Enhance Jetty plug-in
 - Enhance JBoss plug-in
 - [Enhance okhttp plug-in](https://github.com/naver/pinpoint/issues/3761)
 - Fix Dubbo plug-in bugs
 - Fix undertow plug-in bugs
   
### Pinpoint Agent 
 - Started to collect Open File Descriptor Metric
 - Started to collect Direct Buffer Metric
 
### Pinpoint Collector
 
### Pinpoint Web
 - [Fix alarm bug](https://github.com/naver/pinpoint/pull/4079)

## Upgrade consideration

 - **If your application is based on Java 9/10, you should definitely consider updating Pinpoint-Agent to 1.8.**

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

## Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss), [Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere), [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx)
* Spring, Spring Boot (Embedded Tomcat, Jetty)
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER
* ActiveMQ, RabbitMQ
* MySQL, Oracle, MSSQL, CUBRID,POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback

