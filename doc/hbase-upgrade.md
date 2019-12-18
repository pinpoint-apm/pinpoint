---
title: Hbase Upgrade
keywords: hbase, upgrade
last_updated: Mar 8, 2019
sidebar: mydoc_sidebar
permalink: hbaseupgrade.html
disqus: false
---

## Do you like to use Hbase 2.x for Pinpoint?

Default settings of current releases are for Hbase 1.x.

If you'd like to use Hbase 2.x for Pinpoint database,
You have to recompile Pinpoint. Version of `hbase-shaded-client` library need to be changed to 2.x.
(recommend v2.1.1, tested)

You can recompile

with `hbase2` profile added

`mvn clean install -P hbase2,release -DskipTests=true`

or with command (replacing version)

`mvn clean install -Dhbase.shaded.client.version=2.1.1 -DskipTests=true`

or after changing the version directly from pom.xml  

```java
<!-- hbase -->
<hbase.shaded.client.version>1.2.12</hbase.shaded.client.version>
```
