---
title: History
keywords: history
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: history.html
disqus: false
---

Pinpoint is a platform that analyzes large-scale distributed systems and provides a solution to handle large collections of trace data. It has been developed since July 2012 and was launched as an open-source project on January 9, 2015.

This article introduces Pinpoint; it describes what motivated us to start this project, which technologies are used, and how the Pinpoint Agent can be optimized.

> 本文的中文翻译版本 [请见这里](https://github.com/skyao/leaning-pinpoint/blob/master/design/technical_overview.md)

## Motivation to Get Started & Pinpoint Characteristics

Compared to nowadays, the number of Internet users was relatively small and the architecture of Internet services was less complex. Web services were generally configured using a 2-tier (web server and database) or 3-tier (web server, application server, and database) architecture. However, today, supporting a large number of concurrent connections is required and functionalities and services should be organically integrated as the Internet has grown, resulting in much more complex combinations of the software stack. That is n-tier architecture more than 3 tiers has become more widespread. A service-oriented architecture (SOA) or the [microservices](http://en.wikipedia.org/wiki/Microservices) architecture is now a reality.

The system's complexity has consequently increased. The more complex it is, the more difficult you solve problems such as system failure or performance issues. For example, Finding solutions in a 3-tier system is far less complicated. You only need to analyze 3 main components; a web server, an application server, and a database where the number of servers is small. While, if a problem occurs in an n-tier architecture, a large number of components and servers should be investigated. Another problem is that it is difficult to see the big picture only with an analysis of individual components; a low visibility issue is raised. The higher the degree of system complexity is, the longer it takes time to find out the reasons. Even worse, probability increases in which we may not even find them at all. 

Such problems have occurred in the systems at NAVER. A variety of tools like Application Performance Management (APM) were used but they were not enough to handle the problems effectively; so we finally ended up developing a new tracing platform for n-tier architecture, which can provide solutions to systems with an n-tier architecture.

Pinpoint, began development in July 2012 and was launched as an open-source project in January 2015, is an n-tier architecture tracing platform for large-scale distributed systems. The characteristics of Pinpoint are as follows:
*	Distributed transaction tracing to trace messages across distributed applications
*	Automatically detecting the application topology that helps you to figure out the configurations of an application
*	Horizontal scalability to support large-scale server group
*	Providing code-level visibility to easily identify points of failure and bottlenecks
*	Adding a new functionality without code modifications, using the bytecode instrumentation technique
