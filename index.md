---
title: "Welcome to Pinpoint"
keywords: pinpoint apm, apm, pinpoint, pinpoint homepage, pinpoint gitpage
tags:
sidebar: mydoc_sidebar
permalink: index.html
summary:
toc: false
disqus: false
---

![Pinpoint](images/logo.png)

![Build Status](https://travis-ci.org/naver/pinpoint.svg?branch=master)
![codecov](https://codecov.io/gh/naver/pinpoint/branch/master/graph/badge.svg)

**Pinpoint** is an APM (Application Performance Management) tool for large-scale distributed systems written in Java. Inspired by [Dapper](http://research.google.com/pubs/pub36356.html "Google Dapper"), Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

You should definitely check **Pinpoint** out If you want to

* understand your [application topology](./overview.html) at a glance
* monitor your application in *Real-Time*
* gain code-level visibility to every transaction
* install APM Agents without changing a single line of code
* have minimal impact on the performance (approximately 3% increase in resource usage)

## Want a quick tour?
 * [Latest updates on Pinpoint](./news.html)
 * [Overview](./overview.html)/[History](./history.html)/[Tech Details](./techdetail.html) to get to know more about Pinpoint 
 * [Introduction to Pinpoint(v1.5.0)](https://www.youtube.com/watch?v=U4EwnB34Dus&feature=youtu.be) - a video introducing Pinpoint

<iframe width="560" height="315" src="https://www.youtube.com/embed/U4EwnB34Dus" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

## Getting Started
 * [Quick-start guide](./quickstart.html) for simple test run of Pinpoint
 * [Installation guide](./installation.html) for further instructions.

## Supported Modules (last updated 2018/04/01)
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


## Google Analytics
The web module has google analytics attached that tracks the number and the order of button clicks in the server map, transaction list, and the inspector view.

This data is used to better understand how users interact with the Web UI which gives us valuable information in improving Pinpoint Web's user experience.
To disable this for any reason, set the following option to false in *pinpoint-web.properties* for your web instance.
```
config.sendUsage=false
```

## License
Pinpoint is licensed under the Apache License, Version 2.0.
See [LICENSE](https://github.com/naver/pinpoint/blob/master/LICENSE) for full license text.

```
Copyright 2017 NAVER Corp.

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
