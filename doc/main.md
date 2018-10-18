---
title: "Pinpoint 1.8.1"
keywords: pinpoint release, 1.8.1
permalink: main.html
sidebar: mydoc_sidebar
---

[Check out updates on lastest stable release](https://naver.github.io/pinpoint/1.8.0/main.html)

## What's Next in 1.8.1?


### Pinpoint Plugin

- Started to support MongoDB

### Pinpoint Agent


### Pinpoint Collector


### Pinpoint Web


### Pinpoint Flink



### Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss),
[Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere),
WebLogic
- asynchronous communication supported
* [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx), Undertow
* Spring, Spring Boot (Embedded Tomcat, Jetty), Spring asynchronous communication
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient, Akka-http
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER, Kafka
* ActiveMQ, RabbitMQ
* MySQL, Oracle, MSSQL, CUBRID,POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback

