# Pinpoint Agent Plugins

This document lists the plugins bundled with the Pinpoint agent and the range of
library/framework versions each one supports.

_Last updated: 2026-07-13_

> **Note:** Version compatibility for the plugins below is still being reviewed.
> Some ranges may be outdated or incomplete and are subject to change.

- **Since** — the Pinpoint version in which the plugin was first introduced.
- **Supported Version** — the version range of the target library that the plugin
  supports. The range follows Maven notation: `[` / `]` are inclusive and
  `(` / `)` are exclusive. `x.max` means "any release of that major/minor line".
- The ranges below reflect the versions Pinpoint has verified. Newer library
  releases often work as well but are not guaranteed.

> Each range is sourced from the individual plugin's `README.md` (`* Range:` /
> `* Since:` lines). When you change a plugin's supported version, update both
> the plugin README and this table.

## Application / Web Server

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [tomcat](tomcat) | Apache Tomcat | `[8, 10]` | 1.5.0 |
| [jetty](jetty) | Eclipse Jetty | `[9, 11]` | 1.5.0 |
| [jboss](jboss) | JBoss / WildFly | `[6, 7]` | 1.6.0 |
| [weblogic](weblogic) | Oracle WebLogic | `[10, 12]` | 1.8.0 |
| [websphere](websphere) | IBM WebSphere | `[6.1, 8]` | 1.7.0 |
| [undertow](undertow) | `io.undertow:undertow-core` | `[2.0.0.Final, 2.0.16.Final]` | 1.8.0 |
| [undertow-servlet](undertow-servlet) | `io.undertow:undertow-servlet` | `[2.0.0.Final, 2.0.16.Final]` | 1.8.0 |
| [vertx](vertx) | Eclipse Vert.x | `[3.3, 4.max]` | 1.6.0 |
| [akka-http](akka-http) | `com.typesafe.akka:akka-http-core_2.12` | `[10.1.0, 10.4]` | 1.8.0 |
| [pekko-http](pekko-http) | `org.apache.pekko:pekko-http-core_2.13` | `[1.0.0, )` | — |

## HTTP Client

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [httpclient3](httpclient3) | `commons-httpclient:commons-httpclient` | `[3.0, 3.1]` | 1.5.0 |
| [httpclient4](httpclient4) | `org.apache.httpcomponents:httpclient` | `[4.0, 4.5]` | 1.5.0 |
| [httpclient5](httpclient5) | `org.apache.httpcomponents.client5:httpclient5` | `[5.0, 5.2]` | 2.5.0 |
| [okhttp](okhttp) | `com.squareup.okhttp:okhttp` | `[2.0, 2.7]` | 1.5.0 |
| [okhttp](okhttp) | `com.squareup.okhttp3:okhttp` | `[3.0, 3.14]` | 1.5.0 |
| [ning-asynchttpclient](ning-asynchttpclient) | `com.ning:async-http-client` | `[1.7, 1.9]` | 1.5.0 |
| [ning-asynchttpclient](ning-asynchttpclient) | `org.asynchttpclient:async-http-client` | `[2.0, 2.12]` | 1.5.0 |
| [google-httpclient](google-httpclient) | `com.google.http-client:google-http-client` | `[1.40, 1.42]` | 1.5.0 |
| [reactor-netty](reactor-netty) | `io.projectreactor.netty:reactor-netty` | `[0.8.0.RELEASE, 0.9.2.RELEASE]` | 2.0.0 |
| [cxf](cxf) | `org.apache.cxf` (client) | `[3.0, 3.5]` | 1.5.0 |
| [jdk-http](jdk-http) | JDK `HttpURLConnection` | `java [1.6, )` | 1.5.0 |
| [jdk-httpclient](jdk-httpclient) | JDK `java.net.http.HttpClient` | `java [11, )` | 3.0.0 |

## RPC / Web Service

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [apache-dubbo](apache-dubbo) | `org.apache.dubbo:dubbo` | `[2.7, 3.1]` | 2.0.0 |
| [grpc](grpc) | `io.grpc:grpc-core` | `[1.2, 1.50]` | 1.8.0 |
| [thrift](thrift) | `org.apache.thrift` | `[0.6, 0.17]` | 1.5.0 |

## Database — JDBC Driver

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [mysql-jdbc](mysql-jdbc) | `mysql:mysql-connector-java` | `[2.0, 8.0]` | 1.5.0 |
| [mariadb-jdbc](mariadb-jdbc) | `org.mariadb.jdbc:mariadb-java-client` | `[1.1, 3.1]` | 1.5.0 |
| [postgresql-jdbc](postgresql-jdbc) | `org.postgresql:postgresql` | `[9.2, 42.5]` | 1.6.0 |
| [oracle-jdbc](oracle-jdbc) | `com.oracle.database.jdbc:ojdbc8` | `[12, 27]` | 1.5.0 |
| [mssql-jdbc](mssql-jdbc) | `com.microsoft.sqlserver:mssql-jdbc` | `[6.1, 12.1]` | 2.0.0 |
| [db2-jdbc](db2-jdbc) | `com.ibm.db2:jcc` | `[11.1.4.4, 12.1.4.0]` | 3.1.0 |
| [informix-jdbc](informix-jdbc) | `com.ibm.informix:jdbc` | `[4.10, 4.50]` | 1.5.0 |
| [cubrid-jdbc](cubrid-jdbc) | `cubrid:cubrid-jdbc` | `[8.2.0, 11.1.1]` | 1.6.0 |
| [dameng-jdbc](dameng-jdbc) | `com.dameng:DmJdbcDriver18` | `[8.1.1.193, 8.max)` | 3.1.0 |
| [jtds](jtds) | `net.sourceforge.jtds:jtds` | `[1.2, 1.3]` | 1.5.0 |
| [spring-data-r2dbc](spring-data-r2dbc) | `org.springframework.data:spring-data-r2dbc` (+ R2DBC drivers) | `[1.0, 1.5]` | 2.5.0 |

## Database — Connection Pool

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [dbcp](dbcp) | `commons-dbcp:commons-dbcp` | `[1.0, 1.4]` | 1.5.0 |
| [dbcp2](dbcp2) | `org.apache.commons:commons-dbcp2` | `[2.0, 2.9]` | 1.5.0 |
| [hikaricp](hikaricp) | `com.zaxxer:HikariCP` | `[2.3, 5.0]` | 1.8.0 |
| [druid](druid) | `com.alibaba:druid` | `[1.0, 1.2]` | 1.8.0 |

## ORM / SQL Mapper

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [mybatis](mybatis) | `org.mybatis:mybatis` | (all) | 1.5.0 |
| [ibatis](ibatis) | Apache iBATIS | (all) | 1.5.0 |

## NoSQL / Cache / Search

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [redis](redis) | `redis.clients:jedis` | `[2.4.x, 2.9.0]` | 1.0 |
| [redis-lettuce](redis-lettuce) | `io.lettuce:lettuce-core` | `[5.0.0.RELEASE, 5.1.2.RELEASE]` | 1.8.1 |
| [redis-redisson](redis-redisson) | `org.redisson:redisson` | `[3.10.0, 3.10.4]` | 1.9.0 |
| [arcus](arcus) | `com.navercorp.arcus:arcus-java-client` | `[1.7, 1.13]` | 1.6.0 |
| [mongodb](mongodb) | `org.mongodb:mongodb-driver-sync` | `[2.0, 3.12]` | 1.8.0 |
| [cassandra](cassandra) | `com.datastax.cassandra:cassandra-driver-core` | `[2.0, 3.11]` | 1.6.0 |
| [cassandra4](cassandra4) | `com.datastax.oss:java-driver-core` | `[4.1.0, 4.15]` | 2.5.0 |
| [hbase](hbase) | `org.apache.hbase:hbase-shaded-client` | `[1.0, 3.0]` | 1.8.0 |
| [elasticsearch](elasticsearch) | `org.elasticsearch.client:elasticsearch-rest-high-level-client` | `[6.0, 7.16]` | 2.0.0 |
| [elasticsearch8](elasticsearch8) | `co.elastic.clients:elasticsearch-java` | `[8.0, 8.5]` | 2.4.0 |

## Messaging / Streaming

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [kafka](kafka) | `org.apache.kafka:kafka-clients` | `[0.11, 3.max]` | 1.8.0 |
| [rabbitmq](rabbitmq) | `com.rabbitmq:amqp-client` | `[3.0.0, 5.x]` | 1.7.0 |
| [rocketmq](rocketmq) | `org.apache.rocketmq:rocketmq-client` | `[4.0, 5.0]` | 2.2.0 |
| [activemq-client](activemq-client) | `org.apache.activemq:activemq-client` | `[5.1.0, 5.16]` | 1.6.0 |
| [pulsar](pulsar) | `org.apache.pulsar:pulsar-client` | `[4.0.0, )` | 3.1.0 |
| [paho-mqtt](paho-mqtt) | `org.eclipse.paho:org.eclipse.paho.mqttv5.client` | `1.2.5` (tested) | — |

## Reactive / Async / Resilience

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [reactor](reactor) | `io.projectreactor:reactor-core` | `[3.0.0.RELEASE, 3.3.1.RELEASE]` | 2.0.0 |
| [kotlin-coroutines](kotlin-coroutines) | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `[1.0.1, )` | 2.4.0 |
| [resilience4j](resilience4j) | `io.github.resilience4j:resilience4j-reactor` | `1.7.1.RELEASE` | 2.6.0 |
| [hystrix](hystrix) | `com.netflix.hystrix:hystrix-core` | `[1.4, 1.5]` | 1.6.0 |
| [rxjava](rxjava) | RxJava (1.x, beta) | (all) | — |
| [ktor](ktor) | JetBrains Ktor server | (all) | 3.0.1 |

## Logging

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [log4j](log4j) | `log4j:log4j` | `[1.1, 1.2]` | 1.5.0 |
| [log4j2](log4j2) | `org.apache.logging.log4j:log4j-core` | `[2.1, 2.19]` | 1.8.0 |
| [logback](logback) | `ch.qos.logback:logback-core` | `[1.0, 1.4]` | 1.5.0 |

## JSON / Serialization

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [jackson](jackson) | `com.fasterxml.jackson.core:jackson-databind` | `[2.8, 2.14]` | 1.5.0 |
| [gson](gson) | `com.google.code.gson:gson` | `[1.1, 2.9]` | 1.5.0 |
| [fastjson](fastjson) | `com.alibaba:fastjson` | `[1.2, 2.0]` | 1.8.0 |
| [json-lib](json-lib) | `net.sf.json-lib:json-lib` | `[1.0, 2.4]` | 1.5.0 |

## Spring

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [spring](spring) | `org.springframework:spring-context` | `[3.0, 7.0.max]` | 1.5.0 |
| [spring-boot](spring-boot) | `org.springframework.boot:spring-boot` | `[1.2, 4.1.max]` | 1.5.0 |
| [spring-webflux](spring-webflux) | `org.springframework:spring-webflux` | `[5.0.0.RELEASE, 7.0.max]` | 2.0.0 |
| [spring-tx](spring-tx) | `org.springframework:spring-tx` | (all) | 2.5.1 |
| [spring-cloud-sleuth](spring-cloud-sleuth) | Spring Cloud Sleuth | (all) | 2.0.0 |

## Cloud / Serverless / Storage

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [aws-sdk-s3](aws-sdk-s3) | AWS SDK for Java — S3 | (all) | 3.1.0 |
| [openwhisk](openwhisk) | `internal.com.apache.openwhisk:openwhisk-common` | `[2.0, 2.7]` | 1.8.0 |

## Others

| Plugin | Library | Supported Version | Since |
| --- | --- | --- | --- |
| [user](user) | User-defined entry/exit points | — | 1.6.0 |
| [thread](thread) | `java.lang.Thread` async tracing | — | 2.0.2 |
| [process](process) | `java.lang.Process` / `ProcessBuilder` | — | 1.6.0 |

---

**Notes**

- `(all)` means the plugin has no declared version range in its README; it is not
  version-bound to a specific release line.
- `—` in the *Since* column means the plugin README does not declare a `Since`
  version.
- A few bundled plugins (e.g. `netty`, `resttemplate`, `clickhouse-jdbc`,
  `jetty12`, `spring-stub`) are not listed here because they have no `README.md`
  yet. Contributions to document them are welcome.
