

![Pinpoint](web/psd/logo.png)

[![Maven](https://img.shields.io/github/actions/workflow/status/pinpoint-apm/pinpoint/maven.yml?branch=master&label=build&logo=github)](https://github.com/pinpoint-apm/pinpoint/actions?query=workflow%3AMaven)
[![codecov](https://codecov.io/gh/pinpoint-apm/pinpoint/branch/master/graph/badge.svg)](https://codecov.io/gh/pinpoint-apm/pinpoint)

## Latest Release (2023/06/14)

We're happy to announce the release of Pinpoint v2.5.2.
Please check the release note at (https://github.com/pinpoint-apm/pinpoint/releases/tag/v2.5.2).

The current stable version is [v2.5.2](https://github.com/pinpoint-apm/pinpoint/releases/tag/v2.5.2).

## Live Demo

Take a quick look at Pinpoint with our [demo](http://125.209.240.10:10123/main/ApiGateway@SPRING_BOOT/5m?inbound=1&outbound=4&wasOnly=false&bidirectional=false)!

## PHP, PYTHON

Pinpoint also supports application written in PHP, Python. [Check-out our agent repository](https://github.com/pinpoint-apm/pinpoint-c-agent).

## About Pinpoint

**Pinpoint** is an APM (Application Performance Management) tool for large-scale distributed systems written in Java / [PHP](https://github.com/pinpoint-apm/pinpoint-c-agent)/[PYTHON]((https://github.com/pinpoint-apm/pinpoint-c-agent)).
Inspired by [Dapper](http://research.google.com/pubs/pub36356.html "Google Dapper"),
Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

You should definitely check **Pinpoint** out If you want to

* understand your *[application topology](https://pinpoint-apm.gitbook.io/pinpoint/want-a-quick-tour/overview)* at a glance
* monitor your application in *Real-Time*
* gain *code-level visibility* to every transaction
* install APM Agents *without changing a single line of code*
* have minimal impact on the performance (approximately 3% increase in resource usage)

## Getting Started
 * [Quick-start guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/quickstart) for simple test run of Pinpoint
 * [Installation guide](https://pinpoint-apm.gitbook.io/pinpoint/getting-started/installation) for further instructions.
 
## Overview
Services nowadays often consist of many different components, communicating amongst themselves as well as making API calls to external services. How each and every transaction gets executed is often left as a blackbox. Pinpoint traces transaction flows between these components and provides a clear view to identify problem areas and potential bottlenecks.<br/>
For a more intimate guide, please check out our *[Introduction to Pinpoint](https://pinpoint-apm.gitbook.io/pinpoint/#want-a-quick-tour)* video clip.

* **ServerMap** - Understand the topology of any distributed systems by visualizing how their components are interconnected. Clicking on a node reveals details about the component, such as its current status, and transaction count.
* **Realtime Active Thread Chart** - Monitor active threads inside applications in real-time.
* **Request/Response Scatter Chart** - Visualize request count and response patterns over time to identify potential problems. Transactions can be selected for additional detail by **dragging over the chart**.

  ![Server Map](doc/images/ss_server-map.png)

* **CallStack** - Gain code-level visibility to every transaction in a distributed environment, identifying bottlenecks and points of failure in a single view.

  ![Call Stack](doc/images/ss_call-stack.png)

* **Inspector** - View additional details on the application such as CPU usage, Memory/Garbage Collection, TPS, and JVM arguments.

  ![Inspector](doc/images/ss_inspector.png)

* **URI-metric**
  ![URI-Metric](doc/images/ss-uri-metric.png)

* **Infrastructure**
  ![Infrastructure](doc/images/ss-Infrastructure-metric.png)

## Supported Modules
* JDK 8+
* [Tomcat](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/tomcat), [Jetty](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jetty), [JBoss EAP](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jboss), [Resin](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/resin), [Websphere](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/websphere), [Vertx](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/vertx), [Weblogic](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/weblogic), [Undertow](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/undertow), [Akka HTTP](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/akka-http)
* [Spring](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/spring), [Spring Boot](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/spring-boot) ([Embedded Tomcat](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/tomcat), [Jetty](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jetty), [Undertow](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/undertow), [Reactor Netty](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/reactor-netty)), [Spring WebFlux](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/spring-webflux)
* Apache HttpClient [3](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/httpclient3) / [4](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/httpclient4) / [5](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/httpclient5), [JDK HttpConnector](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jdk-http), [GoogleHttpClient](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/google-httpclient), [OkHttpClient](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/okhttp), [NingAsyncHttpClient](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/ning-asynchttpclient)
* [Thrift](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/thrift), [DUBBO](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/dubbo), [GRPC](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/grpc), [Apache CXF](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/cxf)
* [ActiveMQ](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/activemq-client), [RabbitMQ](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/rabbitmq), [Kafka](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/kafka), [RocketMQ](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/rabbitmq), [Paho MQTT](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/paho-mqtt)
* [MySQL](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/mysql-jdbc), [Oracle](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/oracle-jdbc), [MSSQL](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/mssql-jdbc), [JTDS](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jtds), [CUBRID](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/cubrid-jdbc), [POSTGRESQL](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/postgresql-jdbc), [MARIA](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/mariadb-jdbc), [Informix](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/informix-jdbc), [Spring Data R2DBC](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/spring-data-r2dbc)
* [Arcus](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/arcus), [Memcached](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/arcus), Redis([Jedis](https://github.com/pinpoint-apm/pinpoint/blob/master/plugins/redis), [Lettuce](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/redis-lettuce), [Redisson](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/redis-redisson)), [CASSANDRA](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/cassandra), [MongoDB](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/mongodb), [Hbase](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/hbase), [Elasticsearch](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/elasticsearch)
* [iBATIS](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/ibatis), [MyBatis](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/mybatis)
* [DBCP](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/dbcp), [DBCP2](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/dbcp2), [HIKARICP](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/hikaricp), [DRUID](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/druid)
* [Gson](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/gson), [Jackson](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/jackson), [Json Lib](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/json-lib), [Fastjson](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/fastjson)
* [log4j](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/log4j), [Logback](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/logback), [log4j2](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/log4j2)
* [OpenWhisk](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/openwhisk), [Kotlin Coroutines](https://github.com/pinpoint-apm/pinpoint/tree/master/plugins/kotlin-coroutines)

## Compatibility

Java version required to run Pinpoint:
<!-- <compatibilityJava.md> -->
| Pinpoint Version | Agent | Collector | Web | Flink |
|------------------|-------|-----------|-----|-------|
| 2.0.x            | 6-13  | 8         | 8   | 8     |
| 2.1.x            | 6-14  | 8         | 8   | 8     |
| 2.2.x            | 7-14  | 8         | 8   | 8     |
| 2.3.x            | 7-17  | 8         | 8   | 8     |
| 2.4.x            | 7-18  | 11        | 11  | 11    |
| 2.5.x            | 8-19  | 11        | 11  | 11    |

<!-- </compatibilityJava.md> -->
HBase compatibility table:
<!-- <compatibilityHbase.md> -->
| Pinpoint Version | HBase 1.x | HBase 2.x                                                                                                             |
|-----------------|-----------|-----------------------------------------------------------------------------------------------------------------------|
| 2.0.x           | yes       | [optional](https://pinpoint-apm.gitbook.io/pinpoint/documents/hbase-upgrade#do-you-like-to-use-hbase-2x-for-pinpoint) |
| 2.1.x           | yes       | [optional](https://pinpoint-apm.gitbook.io/pinpoint/documents/hbase-upgrade#do-you-like-to-use-hbase-2x-for-pinpoint) |
| 2.2.x           | yes       | [optional](https://pinpoint-apm.gitbook.io/pinpoint/documents/hbase-upgrade#do-you-like-to-use-hbase-2x-for-pinpoint) |
| 2.3.x           | yes       | [hbase2-module](https://github.com/pinpoint-apm/pinpoint/tree/master/hbase2-module)                                   |
| 2.4.x           | yes       | [hbase2-module](https://github.com/pinpoint-apm/pinpoint/tree/master/hbase2-module)                                   |
| 2.5.x           | yes       | [hbase2-module](https://github.com/pinpoint-apm/pinpoint/tree/master/hbase2-module)                                   |
| 2.6.x           | no         | yes |                                                                                                                  |

<!-- </compatibilityHbase.md> -->
Agent - Collector compatibility table:
<!-- <compatibilityPinpoint.md> -->
| Agent Version | Collector 2.0.x | Collector 2.1.x | Collector 2.2.x | Collector 2.3.x | Collector 2.4.x | Collector 2.5.x |
|---------------|-----------------|-----------------|-----------------|-----------------|-----------------|-----------------|
| 2.0.x         | yes             | yes             | yes             | yes             | yes             | yes             |
| 2.1.x         | no              | yes             | yes             | yes             | yes             | yes             |
| 2.2.x         | no              | no              | yes             | yes             | yes             | yes             |
| 2.3.x         | no              | no              | no              | yes             | yes             | yes             |
| 2.4.x         | no              | no              | no              | no              | yes             | yes             |
| 2.5.x         | no              | no              | no              | no              | no              | yes             |

<!-- </compatibilityPinpoint.md> -->
Flink compatibility table:
<!-- <compatibilityFlink.md> -->
| Pinpoint Version | Flink 1.3.X | Flink 1.4.X | Flink 1.5.X | Flink 1.6.X | Flink 1.7.X | Flink 1.14.X |
|------------------|-------------|-------------|-------------|-------------|-------------|--------------|
| 2.0.x            | yes         | yes         | yes         | yes         | yes         | no           |
| 2.1.x            | yes         | yes         | yes         | yes         | yes         | no           |
| 2.2.x            | yes         | yes         | yes         | yes         | yes         | no           |
| 2.3.x            | yes         | yes         | yes         | yes         | yes         | no           |
| 2.4.x            | yes         | yes         | yes         | yes         | yes         | yes          |
| 2.5.x            | yes         | yes         | yes         | yes         | yes         | yes          |
<!-- </compatibilityFlink.md> -->
Pinot compatibility table:
<!-- <compatibilityPinot.md> -->
| Pinpoint Version | Pinot 0.11.0 | Pinot 0.12.0 |
|------------------|--------------|--------------|
| 2.5.x            | yes          | yes          | 
<!-- </compatibilityPinot.md> -->

## Community

[Github issues](https://github.com/pinpoint-apm/pinpoint/issues)  
[Google group](https://groups.google.com/forum/#!forum/pinpoint_user)  
[Gitter](https://gitter.im/naver/pinpoint)  

We have Chinese community now, welcome to join!

<details>
  <summary> 👆 click me
</summary>

QQ Group1: 897594820 | QQ Group2: 812507584 | QQ Group3: 882020485| DING Group : 21981598
:----------------: |:----------------: | :-----------: | :-----------: 
![QQ Group1](doc/images/NAVERPinpoint.png) | ![QQ Group2](doc/images/NAVERPinpoint2.png)| ![QQ Group3](doc/images/NAVERPinpoint3.png)| ![DING Group](doc/images/NaverPinpoint交流群-DING.jpg)

</details>

## License
Pinpoint is licensed under the Apache License, Version 2.0.
See [LICENSE](LICENSE) for full license text.

```
Copyright 2018 NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

