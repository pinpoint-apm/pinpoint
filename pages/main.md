---
title: "Pinpoint 2.1.1"
keywords: pinpoint release, 2.1.1
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 2.1.1

 Few performance realted issues has been resolved and can be configured in v2.1.1.  
 Paho MQTT plugin added. Details are below.
 
### Paho MQTT Plugin

* Call Stack  

![pahomqimage](https://user-images.githubusercontent.com/10057874/99467602-1cc39880-2982-11eb-8691-85770f761712.jpg)

(Thank you @acafela for your contribution)

### Add limiter when drawing Servermap 

Add limiter on the number of link data and on build time for preventing OOM when drawing Servermap
 
* configuration

```
# Limit number of link data
web.servermap.linkData.limit=500000000
# ApplicationMap build timeout in milliseconds
web.servermap.build.timeout=600000
```

### Add limiter when making Callstack

Add limiter on the number of Span and SpanChunk Data when making Callstack

* screenshot

![3b367f00-2903-11eb-9c83-4cbae7bff5e3](https://user-images.githubusercontent.com/10057874/99467743-5dbbad00-2982-11eb-815c-6ba6ce430074.jpg)

* configuration

```
# Limit number of Span and SpanChunk data
# If -1, there is no limit
web.callstack.selectSpans.limit=10000
```

### Support cache when making Callstack

Support that reuse string value in SpanDecoder.

* configuration

```
# Limit number of string cache size in SpanMapper
# If -1, cache will be disabled
web.hbase.mapper.cache.string.size=-1
```

### Auto generate AgentId

If AgentID is not present, the agent automatically generates it.

### Header support according to Kafka Broker

Determining whether to insert a header according to the broker in Kafka Plugin

## Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Additionally, the required Java version to run each Pinpoint component is given below:

{% include_relative compatibilityJava.md %}

## Supported Modules

* JDK 6+
* Supported versions of the \* indicated library may differ from the actual version.

{% include_relative modules.md %}


