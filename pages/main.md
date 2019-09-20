---
title: "Pinpoint 1.9.0"
keywords: pinpoint release, 1.9.0
permalink: main.html
sidebar: mydoc_sidebar
---

[Check out updates on lastest stable release](https://naver.github.io/pinpoint/1.8.5/main.html)

## Currently Working On

\* **Scheduled for 1.9.0**

 - Node.js Agent is being tested. Node.js will be soon supported 
 - \*Support Istio
 - \*Switch to GRPC  
 - \*Features to support k8s, docker
 - \*Pinpoint UI v2 test is on progress
 - \*Enhance Alarm criteria
 - \*Support ElasticSearchBBoss
 - \*Support Webflux
 - \*Support Redisson

## Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}


## Supported Modules

* JDK 6+
* Supported versions of the \* indicated library may differ from the actual version.

{% include_relative modules.md %}
