---
title: How to use Application Inspector
tags:
keywords: inspector, how, how-to
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: applicationinspector.html
disqus: true
---

# Application Inspector

## 1. Introduction

Application inspector provides an aggregate view of all the agent's resource data (cpu, memory, tps, datasource connection count, etc) registered under the same application name. A separate view is provided for the application inspector with stat charts similar to the agent inspector.

To access application inspector, click on the application inspector menu on the left side of the screen.

- 1 : application inspector menu, 2 : application stat data
![inspector_view.jpg](images/inspector_view.jpg)

The Heap Usage chart above for example, shows the average(Avg), smallest(Min), greatest(Max) heap usage of the agents registered under the same application name along with the id of the agent that had the smallest/greatest heap usage at a certain point in time. The application inspector also provides other statistics found in the agent inspector in a similar fashion.

![graph.jpg](images/graph.jpg)


Application inspector requires [flink](https://flink.apache.org) and [zookeeper](https://zookeeper.apache.org/). Please read on for more detail.

## 2. Architecture

![execute_flow.jpg](images/execute_flow.jpg)

**A.** Run a streaming job on [flink](https://flink.apache.org).  
**B.** The taskmanager server is registered to zookeeper as a data node once the job starts.  
**C.** The Collector obtains the flink server info from zookeeper to create a tcp connection with it and starts sending agent data.  
**D.** The flink server aggregates data sent by the Collector and stores them into hbase.

## 3. Configuration

In order to enable application inspector, you will need to do the following and run pinpoint.

**A.** Create **ApplicationStatAggre** table (refer to [create table script](https://github.com/naver/pinpoint/tree/master/hbase/scripts)), which stores application stat data.

**B.** Configure zookeeper address in [pinpoint-flink.properties](https://github.com/naver/pinpoint/blob/master/flink/src/main/resources/pinpoint-flink.properties) which will be used to store flink's taskmanager server information.
```properties
    flink.cluster.enable=true
    flink.cluster.zookeeper.address=YOUR_ZOOKEEPER_ADDRESS
    flink.cluster.zookeeper.sessiontimeout=3000
    flink.cluster.zookeeper.retry.interval=5000
    flink.cluster.tcp.port=19994
```

**C.** Configure job execution type and the number of listeners to receive data from the Collector in [pinpoint-flink.properties](https://github.com/naver/pinpoint/blob/master/flink/src/main/resources/pinpoint-flink.properties).
* If you are running a flink cluster, set *flink.StreamExecutionEnvironment* to **server**, and *flink.sourceFunction.Parallel* to the number of task manager servers.
* If you are running flink as a standalone, set *flink.StreamExecutionEnvironment* to **local**, and *flink.sourceFunction.Parallel* to **1**.
```properties
    flink.StreamExecutionEnvironment=server
    flink.sourceFunction.Parallel=1
```

**D.** Configure hbase address in [hbase.properties](https://github.com/naver/pinpoint/blob/master/flink/src/main/resources/hbase.properties) which will be used to store aggregated application data.
```properties
    hbase.client.host=YOUR_HBASE_ADDRESS
    hbase.client.port=2181
```

**E.** Build [pinpoint-flink](https://github.com/naver/pinpoint/tree/master/flink) and run the streaming job file created under *target* directory on the flink server.  
  - The name of the streaming job is `pinpoint-flink-job.2.0.jar`.
  - For details on how to run the job, please refer to the [flink website](https://flink.apache.org).

**F.** Configure zookeeper address in [pinpoint-collector.properties](https://github.com/naver/pinpoint/blob/master/collector/src/main/resources/pinpoint-collector.properties) so that the collector can connect to the flink server.
```properties
    flink.cluster.enable=true
    flink.cluster.zookeeper.address=YOUR_ZOOKEEPER_ADDRESS
    flink.cluster.zookeeper.sessiontimeout=3000
```

**G.** Enable application inspector in the web-ui by enabling the following configuration in [pinpoint-web.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/pinpoint-web.properties).
```properties
    config.show.applicationStat=true
```

## 4. Monitoring Streaming Jobs

There is a batch job that monitors how Pinpoint streaming jobs are running. To enable this batch job, configure the following files for *pinpoint-web*.

**batch.properties**
```properties
batch.flink.server=FLINK_MANGER_SERVER_IP_LIST
# Flink job manager server IPs, separated by ','.
# ex) batch.flink.server=123.124.125.126,123.124.125.127
```
**applicationContext-batch-schedule.xml**
```xml
<task:scheduled-tasks scheduler="scheduler">
	...
	<task:scheduled ref="batchJobLauncher" method="flinkCheckJob" cron="0 0/10 * * * *" />
</task:scheduled-tasks>
```

If you would like to send alarms in case of batch job failure, you must implement `com.navercorp.pinpoint.web.batch.JobFailMessageSender class` and register it as a Spring bean.

## 5. Others

For more details on how to install and operate flink, please refer to the [flink website](https://flink.apache.org).
