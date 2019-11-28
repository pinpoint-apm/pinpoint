---
title: FAQ
sidebar: mydoc_sidebar
keywords: faq, question, answer, frequently asked questions, FAQ, question and answer
last_updated: Feb 1, 2018
permalink: faq.html
toc: false
disqus: false
---

[Github issues](https://github.com/naver/pinpoint/issues)  
[Google group](https://groups.google.com/forum/#!forum/pinpoint_user)  
[Gitter](https://gitter.im/naver/pinpoint)  

Chinese groups

QQ Group: 897594820 | DING Group
:----------------: | :-----------: 
![QQ Group](images/NAVERPinpoint.png) | ![DING Group](images/NaverPinpoint交流群-DING.jpg)


### How do I get the call stack view?
Click on a server node, which will populate the scatter chart on the right. This chart shows all succeeded/failed requests that went through the server. If there are any requests that spike your interest, simply **drag on the scatter chart** to select them. This will bring up the call stack view containing the requests you've selected.

### How do I change agent's log level?
You can change the log level by modifying the agent's *log4j.xml* located in *PINPOINT_AGENT/lib* directory.

### Why is only the first/some of the requests traced?
There is a sampling rate option in the agent's pinpoint.config file (profiler.sampling.rate).
Pinpoint agent samples 1 trace every N transactions if this value was set as N.
Changing this value to 1 will allow you to trace every transaction.

### Request count in the Scatter Chart is different from the ones in Response Summary chart. Why is this?
The Scatter Chart data have a second granularity, so the requests counted here can be differentiated by a second interval.
On the other hand, the Server Map, Response Summary, and Load Chart data are stored in a minute granularity (the collector aggregates these in memory and flushes them every minute due to performance reasons).
For example, if the data is queried from 10:00:30 to 10:05:30, the Scatter Chart will show the requests counted between 10:00:30 and 10:05:30, whereas the server map, response summary, and load chart will show the requests counted between 10:00:00 and 10:05:59.

### How do I delete application name and/or agent id from HBase?
Application names and agent ids, once registered, stay in HBase until their TTL expires (default 1year).
You may however delete them proactively using [admin APIs](https://github.com/naver/pinpoint/blob/master/web/src/main/java/com/navercorp/pinpoint/web/controller/AdminController.java) once they are no longer used.
* Remove application name - `/admin/removeApplicationName.pinpoint?applicationName=$APPLICATION_NAME&password=$PASSWORD`
* Remove agent id - `/admin/removeAgentId.pinpoint?applicationName=$APPLICATION_NAME&agentId=$AGENT_ID&password=$PASSWORD`
Note that the value for the password parameter is what you defined `admin.password` property in *pinpoint-web.properties*. Leaving this blank will allow you to call admin APIs without the password parameter.

### What are the criteria for the application name?
Pinpoint's applicationName doesn't support special characters. such as @,#,$,%,*.
Pinpoint's applicationName only supports [a-zA-Z0-9], '.', '-', '_' characters.

### HBase is taking up too much space, which data should I delete first?
Hbase is very scalable so you can always add more region servers if you're running out of space. Shortening the TTL values, especially for **AgentStatV2** and **TraceV2**, can also help (though you might have to wait for a major compaction before space is reclaimed). For details on how to major compact, please refer to [this](https://github.com/naver/pinpoint/blob/master/hbase/scripts/hbase-major-compact-htable.hbase) script.

However, if you **must** make space asap, data in **AgentStatV2** and **TraceV2** tables are probably the safest to delete. You will lose agent statistic data (inspector view) and call stack data (transaction view), but deleting these will not break anything.

Note that deleting ***MetaData** tables will result in **-METADATA-NOT-FOUND* being displayed in the call stack and the only way to "fix" this is to restart all the agents, so it is generally a good idea to leave these tables alone.

### My custom jar application is not being traced. Help!
Pinpoint Agent need an entry point to start off a new trace for a transaction. This is usually done by various WAS plugins (such as Tomcat, Jetty, etc) in which a new trace is started when they receive an RPC request.
For custom jar applications, you need to set this manually as the Agent does not have knowledge of when and where to start a trace.
You can set this by configuring `profiler.entrypoint` in *pinpoint.config* file.

### Building is failing after new release. Help!
Please remember to run the command `mvn clean verify -DskipTests=true` if you've used a previous version before.

### How to set java runtime option when using atlassian OSGi
`-Datlassian.org.osgi.framework.bootdelegation=sun.,com.sun.,com.navercorp.*,org.apache.xerces.*`

### Why do I see UI send requests to https://www.google-analytics.com/collect?
Pinpoint Web module has google analytics attached which tracks the number and the order of button clicks in the Server Map, Transaction List, and the Inspector View.  
This data is used to better understand how users interact with the Web UI which gives us valuable information on improving Pinpoint Web's user experience. To disable this for any reason, set following option to false in pinpoint-web.properties for your web instance.
```
config.sendUsage=false
```

### I'd like to use Hbase 2.x for Pinpoint.
If you'd like to use Hbase 2.x for Pinpoint database, check out [Hbase upgrade guide](https://naver.github.io/pinpoint/plugindevguide.html).


### What can I do if I don't wan't to use gojs
In our next version of Pinpoint-Web, you can choose between visjs or gojs.
The [source code](https://github.com/naver/pinpoint/blob/master/web/src/main/webapp/v2/src/app/app.module.ts) of this option and
[the guide](https://naver.github.io/pinpoint/ui_v2.html)


