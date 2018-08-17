---
title: "Pinpoint 1.7"
keywords: pinpoint release, 1.8.0
permalink: main173.html
sidebar: mydoc_sidebar
---

## What's New in 1.7.3

If you are tracing RabbitMQ or Dubbo, we highly suggest upgrading your agent to 1.7.3 as it includes a number of bug fixes for the plugins.
Added several Bug Fixes

### Pinpoint Plugin

 - Bug Fix RabbitMQ plugin
 - Bug Fix Dubbo plugin
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.3+is%3Aclosed+label%3Amodule%3Aplugin)
 
### Pinpoint Web

 - Bug Fix related to RabbitMQ plugin  
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.3+is%3Aclosed+label%3Abug+label%3Amodule%3Aweb)
    
### Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss), [Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere), [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx)
* Spring, Spring Boot (Embedded Tomcat, Jetty)
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER
* ActiveMQ, RabbitMQ
* MySQL, Oracle, MSSQL, CUBRID, POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback



## What's New in 1.7.2

RabbitMQ, ActiveMQ Plugins are added with several enhancement of other plugins 

### Pinpoint Plugin

 - Started to support RabbitMQ, ActiveMQ plugin
 - Bug Fix MariaDB plugin
 - Enhance stated to support vertx.io 3.5.0
 - Enhance JBoss plugin
 - Enhance Jetty plugin
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.2+is%3Aclosed+label%3Amodule%3Aplugin)
  
### Pinpoint Agent 
 
 - Bug Fix concurrency issue
 - Bug Fix classloader handler for Jboss
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.2+is%3Aclosed+label%3Abug+label%3Amodule%3Aagent)
 
 - Upgrade ASM version to 6.0  
 
 [Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.2+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aagent)


### Pinpoint Collector
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.2+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Acollector)


### Pinpoint Web

  - Bug Fix mixed view
  - Enhance CallTree
  
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.2+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aweb)
    
### Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss), [Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere), [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx)
* Spring, Spring Boot (Embedded Tomcat, Jetty)
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER
* ActiveMQ, RabbitMQ
* MySQL, Oracle, MSSQL, CUBRID, POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback



## What's New in 1.7.1

Bug Fixes
 
### Pinpoint Web

  - Bug Fix on inspector page
  
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.1+is%3Aclosed+label%3Abug+label%3Amodule%3Aweb)
    
### Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss), [Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere), [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx)
* Spring, Spring Boot (Embedded Tomcat, Jetty)
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER
* MySQL, Oracle, MSSQL, CUBRID, POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback


## What's New in 1.7.0

### Pinpoint Plugin

 - Started to support VertX Plugin 
 
   VertX APIs are mostly non-blocking so it was incredibly hard to monitor them. We hope Pinpoint can help in 1.7.0.
 
   ![f16b8170-cbab-11e7-8af1-900ad3aa4fe8](https://user-images.githubusercontent.com/10057874/33050826-4367125e-ceaa-11e7-9687-ed36946347fa.png)
 
 - RxJava 1.x / Hystrix observables Support
     ![rx](https://user-images.githubusercontent.com/10057874/33051608-1ec16ebe-ceae-11e7-8566-f389d8dcfb18.png)
     
     Trace support for RxJava 1.x and Hystrix observables have been added. Hystrix tracing now also requires enabling RxJava plugin.
     (Note that this is a beta release. You must set `profiler.rxjava=true` and `profiler.hystrix=true` in *pinpoint.config*.)    
 
 
 - Enhance MariaDB plugin to support 1.6.x and 2.x   
 - Enhance okhttp plugin to support 3.x   
 - Started to support JSP plugin
 - Started to support Netty plugin
 - Started to support MySQL connector 6
 - Bug Fix Redis plugin
 - and more...
 
[Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Amodule%3Aplugin)
 
### Pinpoint Agent
 
 - Active Trace Optimization
 - Optimize trace format
 - Reduce memory usage of buffered storage
 - Proxy HTTP header monitoring
 - Support multiple async events
 
[Enhancement Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aagent) 
[Bug Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Abug+label%3Amodule%3Aagent)

### Pinpoint Collector
 
[Enhancement Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Acollector)
[Bug Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Abug+label%3Amodule%3Acollector)

### Pinpoint Web
    
 - Application Inspector Added

    ![1](https://user-images.githubusercontent.com/10057874/33051309-a47758f4-ceac-11e7-8cab-1245c58bfd8a.png)
    
    Each application often have multiple agents running the same application, and it was difficult to check them all out in a single view.
    With the introduction of Application Inspector, you now have a bird's eye view of all the agents.
    Please refer to [this](./applicationinspector.html) guide for more detail.

 - ProxyServer Monitoring
 
    ![2](https://user-images.githubusercontent.com/10057874/33051367-ec60a918-ceac-11e7-8dfc-23adee33079d.png)
    
    With a simple configuration, you can now monitor proxy servers sitting in front of your application through Pinpoint.
    Please refer to [this](./proxyhttpheader.html) guide for more detail.


 - Server map Option added

     -  Directional search
        Until now, searching 2+ depths included nodes not relevant to the application, resulting in overly complex server maps that took a long time to query/render.
        Directional search option has been added to alleviate this issue.
        * ![Unidirectional](https://raw.githubusercontent.com/naver/pinpoint/master/web/src/main/webapp/images/bidirect_off.png) (default) : Nodes called by the application will not look for other nodes that called them. Similarly, nodes that called the application will not look for other nodes that they called.
        * ![Bidirectional](https://raw.githubusercontent.com/naver/pinpoint/master/web/src/main/webapp/images/bidirect_on.png) : Just as it is now, all nodes (regardless of their relationship to the application) will look for everything that they called, and everything called by them.
    
     -  WAS Only
        Looking for relationships between WAS nodes were often hard with database nodes, and unknown cloud nodes all over the server map.
        Now you can use the following option to trim out terminal nodes.
        * ![wasonly1](https://user-images.githubusercontent.com/10057874/33051424-30e0cae6-cead-11e7-8ef6-1dc253ff0b8c.png) Terminal nodes such as database and unknown clouds are not included in the server map.
        * ![wasonly2](https://user-images.githubusercontent.com/10057874/33051442-3d685ef0-cead-11e7-865f-fc12e54afa06.png) (default) : Everything is included in the server map, just as it is now.
    
 - Deadlock Detection
 
    ![default](https://user-images.githubusercontent.com/10057874/33051548-bf76e970-cead-11e7-9fd9-486f2b1dda0d.png)

    It is quite hard to detect and identify deadlocks.
    Pinpoint now allows you to identify deadlocks when they happen, and provides relevant thread dumps for you.

 - Average Response Time
 
    ![default](https://user-images.githubusercontent.com/10057874/33051575-f309d7a2-cead-11e7-9756-99a3f3b93d95.png)

    Response Time chart has been added!
    You may now check out your service's average response time in a blink of an eye.

[Enhancement Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aweb)
[Bug Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.7.0+is%3Aclosed+label%3Abug+label%3Amodule%3Aweb)

    
### Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Full List of Supported Modules

* JDK 6+
* Tomcat 6/7/8, [Jetty 8/9](https://github.com/naver/pinpoint/tree/master/plugins/jetty), [JBoss EAP 6](https://github.com/naver/pinpoint/tree/master/plugins/jboss), [Resin 4](https://github.com/naver/pinpoint/tree/master/plugins/resin), [Websphere 6/7/8](https://github.com/naver/pinpoint/tree/master/plugins/websphere), [Vertx 3.3/3.4/3.5](https://github.com/naver/pinpoint/tree/master/plugins/vertx)
* Spring, Spring Boot (Embedded Tomcat, Jetty)
* Apache HTTP Client 3.x/4.x, JDK HttpConnector, GoogleHttpClient, OkHttpClient, NingAsyncHttpClient
* Thrift Client, Thrift Service, DUBBO PROVIDER, DUBBO CONSUMER
* MySQL, Oracle, MSSQL, CUBRID, POSTGRESQL, MARIA
* Arcus, Memcached, Redis, CASSANDRA
* iBATIS, MyBatis
* DBCP, DBCP2, HIKARICP
* gson, Jackson, Json Lib
* log4j, Logback
