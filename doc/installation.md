---
title: Installation
keywords: pinpoint, pinpoint homepage, install, start, installation
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: installation.html
disqus: false
---

To set up your very own Pinpoint instance you can either **download the build results** from our [**latest release**](https://github.com/naver/pinpoint/releases/latest), or manually build from your Git clone.
In order to run your own Pinpoint instance, you will need to run below components:

* **HBase** (for storage)
* **Pinpoint Collector** (deployed on a web container)
* **Pinpoint Web** (deployed on a web container)
* **Pinpoint Agent** (attached to a java application for profiling)

To try out a simple quickstart project, please refer to the [quick-start guide](./quickstart.html).

## Quick Overview of Installation
1. HBase ([details](#1-hbase))
	1. Set up HBase cluster - [Apache HBase](http://hbase.apache.org)
	2. Create HBase Schemas - feed `/scripts/hbase-create.hbase` to hbase shell.
2. Build Pinpoint (Optional)([details](#2-building-pinpoint-optional)) - No need if you use the binaries.([here](https://github.com/naver/pinpoint/releases)).
	1. Clone Pinpoint - `git clone $PINPOINT_GIT_REPOSITORY`
	2. Set JAVA_HOME environment variable to JDK 8 home directory.
	3. Set JAVA_6_HOME environment variable to JDK 6 home directory (1.6.0_45 recommended).
	4. Set JAVA_7_HOME environment variable to JDK 7 home directory (1.7.0_80 recommended).
	5. Set JAVA_8_HOME environment variable to JDK 8 home directory.
	6. Set JAVA_9_HOME environment variable to JDK 9 home directory.
	7. Run `./mvnw clean install -DskipTests=true` (or `./mvnw.cmd` for Windows)
3. Pinpoint Collector ([details](#3-pinpoint-collector))
	1. Deploy *pinpoint-collector-$VERSION.war* to a web container.
	2. Configure *pinpoint-collector.properties*, *hbase.properties*.
	3. Start container.
4. Pinpoint Web ([details](#4-pinpoint-web))
	1. Deploy *pinpoint-web-$VERSION.war* to a web container as a ROOT application.
	2. Configure *pinpoint-web.properties*, *hbase.properties*.
	3. Start container.
5. Pinpoint Agent ([details](#5-pinpoint-agent))
	1. Extract/move *pinpoint-agent/* to a convenient location (`$AGENT_PATH`).
	2. Set `-javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar` JVM argument to attach the agent to a java application.
	3. Set `-Dpinpoint.agentId` and `-Dpinpoint.applicationName` command-line arguments.  
		a) If you're launching an agent in a containerized environment with dynamically changing *agent id*, consider adding `-Dpinpoint.container` command-line argument.
	4. Launch java application with the options above.

## 1. HBase
Pinpoint uses HBase as its storage backend for the Collector and the Web.

To set up your own cluster, take a look at the [HBase website](http://hbase.apache.org) for instructions. The HBase compatibility table is given below:

{% include_relative compatibilityHbase.md %}

Once you have HBase up and running, make sure the Collector and the Web are configured properly and are able to connect to HBase.

### Creating Schemas for HBase
There are 2 scripts available to create tables for Pinpoint: *hbase-create.hbase*, and *hbase-create-snappy.hbase*. Use *hbase-create-snappy.hbase* for snappy compression (requires [snappy](http://google.github.io/snappy/)), otherwise use *hbase-create.hbase* instead.

To run these scripts, feed them into the HBase shell like below:

`$HBASE_HOME/bin/hbase shell hbase-create.hbase`

See [here](https://github.com/naver/pinpoint/tree/master/hbase/scripts "Pinpoint HBase scripts") for a complete list of scripts.

## 2. Building Pinpoint (Optional)

There are two options:

1. Download the build results from our [**latest release**](https://github.com/naver/pinpoint/releases/latest) and skip building process.**(Recommended)**

2. Build Pinpoint manually from the Git clone.
	
	In order to do so, the following **requirements** must be met:

    * JDK 6 installed ([jdk1.6.0_45](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html#jdk-6u45-oth-JPR) recommended)
    * JDK 7 installed ([jdk1.7.0_80](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html#jdk-7u80-oth-JPR) recommended)
    * JDK 8 installed
    * JDK 9 installed
	* JAVA_HOME environment variable set to JDK 8 home directory.
	* JAVA_6_HOME environment variable set to JDK 6 home directory.
	* JAVA_7_HOME environment variable set to JDK 7 home directory.
	* JAVA_8_HOME environment variable set to JDK 8 home directory.
	* JAVA_9_HOME environment variable set to JDK 9 home directory.

	Additionally, the required Java version to run each Pinpoint component is given below:

	{% include_relative compatibilityPinpoint.md %}
    
	Once the above requirements are met, simply run the command below (you may need to add permission for **mvnw** so that it can be executed) :

	`./mvnw install -DskipTests=true`
	
	The default agent built this way will have log level set to DEBUG by default. If you're building an agent for release and need a higher log level, you can set maven profile to *release* when building :  
	`./mvnw install -Prelease -DskipTests=true`
	
	Note that having multibyte characters in maven local repository path, or any class paths may cause the build to fail.
	
	The guide will refer to the full path of the pinpoint home directory as `$PINPOINT_PATH`.

	
Regardless of your method, you should end up with the files and directories mentioned in the following sections.

## 3. Pinpoint Collector
You should have the following **war** file that can be deployed to a web container. 

*pinpoint-collector-$VERSION.war*

The path to this file should look like *$PINPOINT_PATH/collector/target/pinpoint-collector-$VERSION.war* if you built it manually. 

### Installation
Since Pinpoint Collector is packaged as a deployable war file, you may deploy them to a web container as you would any other web applications.

### Configuration
There are 3 configuration files available for Pinpoint Collector: *pinpoint-collector.properties*, *pinpoint-grpc-collector.properties*, and *hbase.properties*.

* pinpoint-collector.properties - contains configurations for the collector. Check the following values with the agent's configuration options :
	* `collector.receiver.base.port` (agent's *profiler.collector.tcp.port* - default: 9994)
	* `collector.receiver.stat.udp.port` (agent's *profiler.collector.stat.port* - default: 9995)
	* `collector.receiver.span.udp.port` (agent's *profiler.collector.span.port* - default: 9996)
* pinpoint-grpc-collector.properties - contains configurations for the grpc.
    * `collector.receiver.grpc.agent.port` (agent's *profiler.transport.grpc.agent.collector.port*, *profiler.transport.grpc.metadata.collector.port* - default: 9991)
	* `collector.receiver.grpc.stat.port` (agent's *profiler.transport.grpc.stat.collector.port* - default: 9992)
	* `collector.receiver.grpc.span.port` (agent's *profiler.transport.grpc.span.collector.port* - default: 9993)
* hbase.properties - contains configurations to connect to HBase.
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

These files are located under `WEB-INF/classes/` inside the war file.

You may take a look at the default configuration files here
- [pinpoint-collector.properties](https://github.com/naver/pinpoint/blob/master/collector/src/main/resources/pinpoint-collector.properties)
- [pinpoint-grpc-collector.properties](https://github.com/naver/pinpoint/blob/master/collector/src/main/resources/pinpoint-grpc-collector.properties)
- [hbase.properties](https://github.com/naver/pinpoint/blob/master/collector/src/main/resources/hbase.properties)

### Profiles
Add `-Dkey=value` to Java System Properties
* WEB-INF/classes/profiles/$PROFILE
  - `-Dspring.profiles.active=release or local`
  - Default profile : `release`
* Support external property
  - `-Dpinpoint.zookeeper.address=$MY_ZOOKEEPER_ADDRESS` `-Dcollector.receiver.span.worker.threadSize=1024` ...
* Support external config
  - `-Dpinpoint.collector.config.location=$MY_EXTERNAL_CONFIG_PATH`
* Add custom profile
  1. Create a custom profile in WEB-INF/classes/profiles/MyProfile
     - Add *-env.config & log4j.xml
  2. Add `-Dspring.profiles.active=MyProfile`


## 4. Pinpoint Web
You should have the following **war** file that can be deployed to a web container.

*pinpoint-web-$VERSION.war*

The path to this file should look like *$PINPOINT_PATH/web/target/pinpoint-web-$VERSION.war* if you built it manually.

Pinpoint Web Supported Browsers:

* Chrome

### Installation
Since Pinpoint Web is packaged as a deployable war file, you may deploy them to a web container as you would any other web applications. The web module must also be deployed as a ROOT application.

### Configuration
Similar to the Collector, Pinpoint Web has configuration files related to installation: *pinpoint-web.properties*, and *hbase.properties*. 

Make sure you check the following configuration options :

* hbase.properties - contains configurations to connect to HBase.
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

These files are located under `WEB-INF/classes/` inside the war file.

You may take a look at the default configuration files here
  - [pinpoint-web.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/pinpoint-web.properties)
  - [hbase.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/hbase.properties)

### Profiles
Add `-Dkey=value` to Java System Properties
* WEB-INF/classes/profiles/$PROFILE
  - `-Dspring.profiles.active=release or local`
  - Default profile : `release`
* Support external property
  - `-Dpinpoint.zookeeper.address=$MY_ZOOKEEPER_ADDRESS` `-Dhbase.rpc.timeout=30000` ...
* Support external config
  - `-Dpinpoint.web.config.location=$MY_EXTERNAL_CONFIG_PATH`
* Add custom profile
  1. Create a custom profile in WEB-INF/classes/profiles/MyProfile
     - Add *-env.config & log4j.xml
  2. Add `-Dspring.profiles.active=MyProfile`

## 5. Pinpoint Agent
If downloaded, unzip the Pinpoint Agent file. You should have a **pinpoint-agent** directory with the layout below :

```
pinpoint-agent
|-- boot
|   |-- pinpoint-annotations-$VERSION.jar
|   |-- pinpoint-bootstrap-core-$VERSION.jar
|   |-- pinpoint-bootstrap-java7-$VERSION.jar
|   |-- pinpoint-bootstrap-java8-$VERSION.jar
|   |-- pinpoint-bootstrap-java9-$VERSION.jar
|   |-- pinpoint-commons-$VERSION.jar
|-- lib
|   |-- pinpoint-profiler-$VERSION.jar
|   |-- pinpoint-profiler-optional-$VERSION.jar
|   |-- pinpoint-rpc-$VERSION.jar
|   |-- pinpoint-thrift-$VERSION.jar
|   |-- ...
|-- plugin
|   |-- pinpoint-activemq-client-plugin-$VERSION.jar
|   |-- pinpoint-tomcat-plugin-$VERSION.jar
|   |-- ...
|-- profiles
|   |-- local
|   |   |-- log4j.xml
|   |   |-- pinpoint-env.config
|   |-- release
|       |-- log4j.xml
|       |-- pinpoint-env.config
|-- pinpoint-bootstrap-$VERSION.jar
|-- pinpoint.config
```
The path to this directory should look like *$PINPOINT_PATH/agent/target/pinpoint-agent* if you built it manually.

You may move/extract the contents of **pinpoint-agent** directory to any location of your choice. The guide will refer to the full path of this directory as `$AGENT_PATH`.

> Note that you may change the agent's log level by modifying the *log4j.xml* located in the *profiles/$PROFILE/log4j.xml* directory above.

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}

### Installation
Pinpoint Agent runs as a java agent attached to an application to be profiled (such as Tomcat). 

To wire up the agent, pass *$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar* to the *-javaagent* JVM argument when running the application:

* `-javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar`

Additionally, Pinpoint Agent requires 2 command-line arguments in order to identify itself in the distributed system:

* `-Dpinpoint.agentId` - uniquely identifies the application instance in which the agent is running on
* `-Dpinpoint.applicationName` - groups a number of identical application instances as a single service

Note that *pinpoint.agentId* must be globally unique to identify an application instance, and all applications that share the same *pinpoint.applicationName* are treated as multiple instances of a single service.

If you're launching the agent in a containerized environment, you might have set your *agent id* to be auto-generated every time the container is launched. With frequent deployment and auto-scaling, this will lead to the Web UI being cluttered with all the list of agents that were launched and destroyed previously. For such cases, you might want to add `-Dpinpoint.container` in addition to the 2 required command-line arguments above when launching the agent.

**Tomcat Example**

Add *-javaagent*, *-Dpinpoint.agentId*, *-Dpinpoint.applicationName* to *CATALINA_OPTS* in the Tomcat startup script (*catalina.sh*).

<pre>
CATALINA_OPTS="$CATALINA_OPTS <b>-javaagent</b>:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar"
CATALINA_OPTS="$CATALINA_OPTS <b>-Dpinpoint.agentId</b>=$AGENT_ID"
CATALINA_OPTS="$CATALINA_OPTS <b>-Dpinpoint.applicationName</b>=$APPLICATION_NAME"
</pre>

Start up Tomcat to start profiling your web application.

Some application servers require additional configuration and/or may have caveats. Please take a look at the links below for further details.
* [JBoss](https://github.com/naver/pinpoint/tree/master/plugins/jboss#pinpoint-jboss-plugin-configuration)
* [Jetty](https://github.com/naver/pinpoint/blob/master/plugins/jetty/README.md)
* [Resin](https://github.com/naver/pinpoint/tree/master/plugins/resin#pinpoint-resin-plugin-configuration)

### Configuration

There are various configuration options for Pinpoint Agent available in *$AGENT_PATH/pinpoint.config*.

Most of these options are self explanatory, but the most important configuration options you must check are **Collector ip address**, and the **TCP/UDP ports**. These values are required for the agent to establish connection to the *Collector* and function correctly. 

Set these values appropriately in *pinpoint.config*:

**THRIFT**
* `profiler.collector.ip` (default: 127.0.0.1)
* `profiler.collector.tcp.port` (collector's *collector.receiver.base.port* - default: 9994)
* `profiler.collector.stat.port` (collector's *collector.receiver.stat.udp.port* - default: 9995)
* `profiler.collector.span.port` (collector's *collector.receiver.span.udp.port* - default: 9996)

**GRPC**
* `profiler.transport.grpc.collector.ip`  (default: 127.0.0.1)
* `profiler.transport.grpc.agent.collector.port` (collector's *collector.receiver.grpc.agent.port* - default: 9991)
* `profiler.transport.grpc.metadata.collector.port` (collector's *collector.receiver.grpc.agent.port* - default: 9991)
* `profiler.transport.grpc.stat.collector.port` (collector's *collector.receiver.grpc.stat.port* - default: 9992)
* `profiler.transport.grpc.span.collector.port` (collector's *collector.receiver.grpc.span.port* - default: 9993)

You may take a look at the default *pinpoint.config* file [here](https://github.com/naver/pinpoint/blob/master/agent/src/main/resources/pinpoint-real-env-lowoverhead-sample.config "pinpoint.config") along with all the available configuration options.

### Profiles
Add `-Dkey=value` to Java System Properties
* $PINPOINT_AGENT_DIR/profiles/$PROFILE
  - `-Dpinpoint.profiler.profiles.active=release or local`
  - Modify `pinpoint.profiler.profiles.active=release` in $PINPOINT_AGENT_DIR/pinpoint.config
  - Default profile : `release`
* Custom Profile
  1. Create a custom profile in $PINPOINT_AGENT_HOME/profiles/MyProfile
     - Add pinpoint-env.config & log4j.xml
  2. Add `-Dpinpoint.profiler.profiles.active=MyProfile`
* Support external config
  - `-Dpinpoint.config=$MY_EXTERNAL_CONFIG_PATH`

## Miscellaneous

### HBase region servers hostname resolution
Please note that collector/web must be able to resolve the hostnames of HBase region servers. 
This is because HBase region servers are registered to ZooKeeper by their hostnames, so when the collector/web asks ZooKeeper for a list of region servers to connect to, it receives their hostnames.
Please ensure that these hostnames are in your DNS server, or add these entries to the collector/web instances' *hosts* file.

### Routing Web requests to Agents

Starting from 1.5.0, Pinpoint can send requests from the Web to Agents directly via the Collector (and vice-versa). To make this possible, we use Zookeeper to co-ordinate the communication channels established between Agents and Collectors, and those between Collectors and Web instances. With this addition, real-time communication (for things like active thread count monitoring) is now possible.

We typically use the Zookeeper instance provided by the HBase backend so no additional Zookeeper configuration is required. Related configuration options are shown below.

* **Collector** - *pinpoint-collector.properties*
	* `cluster.enable`  
	* `cluster.zookeeper.address`
	* `cluster.zookeeper.sessiontimeout`
	* `cluster.listen.ip`
	* `cluster.listen.port`
* **Web** - *pinpoint-web.properties*
	* `cluster.enable`
	* `cluster.web.tcp.port`
	* `cluster.zookeeper.address`
	* `cluster.zookeeper.sessiontimeout`
	* `cluster.zookeeper.retry.interval`
	* `cluster.connect.address`

