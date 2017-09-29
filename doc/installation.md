# Installation
In order to set up your very own Pinpoint instance, you need to run these components:

* **HBase** (for storage)
* **Pinpoint Collector** (deployed on a web container)
* **Pinpoint Web** (deployed on a web container)
* **Pinpoint Agent** (attached to a java application for profiling)

To try out a simple quickstart project, please refer to the [quick-start guide](../quickstart/README.md).

## Quick Overview
1. HBase ([details](#hbase))
	1. Set up HBase cluster - [Apache HBase](http://hbase.apache.org)
	2. Create HBase Schemas - feed `/scripts/hbase-create.hbase` to hbase shell.
2. Build Pinpoint (Optional) - You do not need to build from source to use Pinpoint (binaries [here](https://github.com/naver/pinpoint/releases)).
	1. Clone Pinpoint - `git clone $PINPOINT_GIT_REPOSITORY`
	2. Set JAVA_HOME environment variable to JDK 7+ home directory.
	3. Set JAVA_6_HOME environment variable to JDK 6 home directory (1.6.0_45 recommended).
	4. Set JAVA_7_HOME environment variable to JDK 7 home directory (1.7.0_80 recommended).
	5. Set JAVA_8_HOME environment variable to JDK 8 home directory.
	6. Run `./mvnw clean install -Dmaven.test.skip=true` (or `./mvnw.cmd` for Windows)
3. Pinpoint Collector ([details](#pinpoint-collector))
	1. Deploy *pinpoint-collector-$VERSION.war* to a web container.
	2. Configure *pinpoint-collector.properties*, *hbase.properties*.
	3. Start container.
4. Pinpoint Web ([details](#pinpoint-web))
	1. Deploy *pinpoint-web-$VERSION.war* to a web container as a ROOT application.
	2. Configure *pinpoint-web.properties*, *hbase.properties*.
	3. Start container.
5. Pinpoint Agent ([details](#pinpoint-agent))
	1. Extract/move *pinpoint-agent/* to a convenient location (`$AGENT_PATH`).
	2. Set `-javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar` JVM argument to attach the agent to a java application.
	3. Set `-Dpinpoint.agentId` and `-Dpinpoint.applicationName` command-line arguments.
	4. Launch java application with the options above.

## HBase
Pinpoint uses HBase as its storage backend for the Collector and the Web.

To set up your own cluster, take a look at the [HBase website](http://hbase.apache.org) for instructions. The HBase compatibility table is given below:

Pinpoint Version | HBase 0.94.x | HBase 0.98.x | HBase 1.0.x | HBase 1.1.x | HBase 1.2.x
---------------- | ------------ | ------------ | ----------- | ----------- | -----------
1.0.x | yes | no | no | no | no
1.1.x | no | not tested | yes | not tested | not tested
1.5.x | no | not tested | yes | not tested | not tested
1.6.x | no | not tested | not tested | not tested | yes
1.7.x | no | not tested | not tested | not tested | yes

Once you have HBase up and running, make sure the Collector and the Web are configured properly and are able to connect to HBase.

### Creating Schemas
There are 2 scripts available to create tables for Pinpoint: *hbase-create.hbase*, and *hbase-create-snappy.hbase*. Use *hbase-create-snappy.hbase* for snappy compression (requires [snappy](http://code.google.com/p/snappy)), otherwise use *hbase-create.hbase* instead.

To run these scripts, feed them into the HBase shell like below:

`$HBASE_HOME/bin/hbase shell hbase-create.hbase`

See [here](../hbase/scripts/ "Pinpoint HBase scripts") for a complete list of scripts.

## Building Pinpoint

There are two options:

1. Download the build results from our [**latest release**](https://github.com/naver/pinpoint/releases/latest) and skip the building. **Recommended.**

2. Build Pinpoint manually from the Git clone.
	
	In order to do so, the following **requirements** must be met:

	* JDK 6 installed
	* JDK 7 installed
	* JDK 8 installed
	* JAVA_HOME environment variable set to JDK 7+ home directory.
	* JAVA_6_HOME environment variable set to JDK 6 home directory (1.6.0_45 recommended).
	* JAVA_7_HOME environment variable set to JDK 7 home directory (1.7.0_80 recommended).
	* JAVA_8_HOME environment variable set to JDK 8 home directory.

	JDK 7+ and JAVA_7_HOME, JAVA_8_HOME environment variable are required to build **profiler-optional**. For more information about the optional package, please take a look [here](../profiler-optional/README.md).

	Additionally, the required Java version to run each Pinpoint component is given below:

	Pinpoint Version | Agent | Collector | Web
	---------------- | ----- | --------- | ---
	1.0.x | 6-8 | 6+ | 6+
	1.1.x | 6-8 | 7+ | 7+
	1.5.x | 6-8 | 7+ | 7+
	1.6.x | 6-8 | 7+ | 7+
	1.7.x | 6-8 | 8+ | 8+

	Once the above requirements are met, simply run the command below (you may need to add permission for **mvnw** so that it can be executed) :

	`./mvnw install -Dmaven.test.skip=true`
	
	The default agent built this way will have log level set to DEBUG by default. If you're building an agent for release and need a higher log level, you can set maven profile to *release* when building :  
	`./mvnw install -Prelease -Dmaven.test.skip=true`
	
	The guide will refer to the full path of the pinpoint home directory as `$PINPOINT_PATH`.

	
Regardless of your method, you should end up with the files and directories mentioned in the following sections.

## Pinpoint Collector
You should have the following **war** file that can be deployed to a web container. 

*pinpoint-collector-$VERSION.war*

The path to this file should look like *$PINPOINT_PATH/collector/target/pinpoint-collector-$VERSION.war* if you built it manually. 

### Installation
Since Pinpoint Collector is packaged as a deployable war file, you may deploy them to a web container as you would any other web applications.

### Configuration
There are 2 configuration files available for Pinpoint Collector: *pinpoint-collector.properties*, and *hbase.properties*.

* pinpoint-collector.properties - contains configurations for the collector. Check the following values with the agent's configuration options :
	* `collector.tcpListenPort` (agent's *profiler.collector.tcp.port* - default: 9994)
	* `collector.udpStatListenPort` (agent's *profiler.collector.stat.port* - default: 9995)
	* `collector.udpSpanListenPort` (agent's *profiler.collector.span.port* - default: 9996)
* hbase.properties - contains configurations to connect to HBase.
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

These files are located under `WEB-INF/classes/` inside the war file.

You may take a look at the default configuration files here: [pinpoint-collector.properties](../collector/src/main/resources/pinpoint-collector.properties), [hbase.properties](../collector/src/main/resources/hbase.properties)

## Pinpoint Web
You should have the following **war** file that can be deployed to a web container.

*pinpoint-web-$VERSION.war*

The path to this file should look like *$PINPOINT_PATH/web/target/pinpoint-web-$VERSION.war* if you built it manually.

### Installation
Since Pinpoint Web is packaged as a deployable war file, you may deploy them to a web container as you would any other web applications. The web module must also be deployed as a ROOT application.

### Configuration
Similar to the collector, Pinpoint Web has configuration files related to installation: *pinpoint-web.properties*, and *hbase.properties*. 

Make sure you check the following configuration options :

* hbase.properties - contains configurations to connect to HBase.
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

These files are located under `WEB-INF/classes/` inside the war file.

You may take a look at the default configuration files here: [pinpoint-web.properties](../web/src/main/resources/pinpoint-web.properties), [hbase.properties](../web/src/main/resources/hbase.properties)

## Pinpoint Agent
If downloaded, unzip the Pinpoint Agent file. You should have a **pinpoint-agent** directory with the layout below :

```
pinpoint-agent
|-- boot
|   |-- pinpoint-annotations-$VERSION.jar
|   |-- pinpoint-bootstrap-core-$VERSION.jar
|   |-- pinpoint-bootstrap-core-optional-$VERSION.jar
|   |-- pinpoint-commons-$VERSION.jar
|-- lib
|   |-- log4j.xml
|   |-- pinpoint-profiler-$VERSION.jar
|   |-- pinpoint-profiler-optional-$VERSION.jar
|   |-- pinpoint-rpc-$VERSION.jar
|   |-- pinpoint-thrift-$VERSION.jar
|   |-- ...
|-- plugin
|   |-- pinpoint-activemq-client-plugin-$VERSION.jar
|   |-- pinpoint-arcus-plugin-$VERSION.jar
|   |-- ...
|-- pinpoint-bootstrap-$VERSION.jar
|-- pinpoint.config
```
The path to this directory should look like *$PINPOINT_PATH/agent/target/pinpoint-agent* if you built it manually.

You may move/extract the contents of **pinpoint-agent** directory to any location of your choice. The guide will refer to the full path of this directory as `$AGENT_PATH`.

> Note that you may change the agent's log level by modifying the *log4j.xml* located in the *lib* directory above.

### Installation
Pinpoint Agent runs as a java agent attached to an application to be profiled (such as Tomcat). 

To wire up the agent, pass *$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar* to the *-javaagent* JVM argument when running the application:

* `-javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar`

Additionally, Pinpoint Agent requires 2 command-line arguments in order to identify itself in the distributed system:

* `-Dpinpoint.agentId` - uniquely identifies the application instance in which the agent is running on
* `-Dpinpoint.applicationName` - groups a number of identical application instances as a single service

Note that *pinpoint.agentId* must be globally unique to identify an application instance, and all applications that share the same *pinpoint.applicationName* are treated as multiple instances of a single service.

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

Most of these options are self explanatory, but the most important configuration options you must check are **collector ip address**, and the **TCP/UDP ports**. These values are required for the agent to establish connection to the *Collector* and function correctly. 

Set these values appropriately in *pinpoint.config*:

* `profiler.collector.ip` (default: 127.0.0.1)
* `profiler.collector.tcp.port` (collector's *collector.tcpListenPort* - default: 9994)
* `profiler.collector.stat.port` (collector's *collector.udpStatListenPort* - default: 9995)
* `profiler.collector.span.port` (collector's *collector.udpSpanListenPort* - default: 9996)

You may take a look at the default *pinpoint.config* file [here](../agent/src/main/resources/pinpoint.config "pinpoint.config") along with all the available configuration options.

## Miscellaneous

### Routing web requests to agents

Starting from 1.5.0, Pinpoint can send requests from the web to agents directly via the collector (and vice-versa). To make this possible, we use Zookeeper to co-ordinate the communication channels established between agents and collectors, and those between collectors and web instances. With this addition, real-time communication (for things like active thread count monitoring) is now possible.

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
