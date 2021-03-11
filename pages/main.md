---
title: "Pinpoint 2.2.2"
keywords: pinpoint release, 2.2.2
permalink: main.html
sidebar: mydoc_sidebar
---

# What's New in 2.2.2

v2.2.2 is a bug fix release of 2.2.1

There is a bug in the Reactor-netty plugin (v2.0.0 ~ 2.2.1) which inserts incorrect endPoint value.
It only occurs in certain circumstances relating the high overload in Pinpoint-Collector
To prevent this, it is recommended to upgrade to version 2.2.2 or higher when using the Reactor-netty plugin.
  
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


