# QuickStart
Pinpoint is comprised of 3 main components (Collector, Web, Agent), and uses HBase as its storage. Collector and Web are packaged as simple WAR files, and the Agent is packaged so that it may be attached to applications as a java agent.

Pinpoint QuickStart provides a sample TestApp for the Agent to attach itself to, and launches all three components using [Tomcat Maven Plugin](http://tomcat.apache.org/maven-plugin.html).

## Requirements
In order to build Pinpoint and run QuickStart, the following requirements must be met:

* JDK 7+ installed
* Maven installed
* JAVA_7_HOME environment variable set to JDK 7 installation directory.

JDK 7+ and JAVA_7_HOME environment variable are required to build profiler-optional. If you choose to build each modules separately, you may do so with JDK 6.

QuickStart currently only supports Linux, and OSX.


## Starting 
Download Pinpoint with ```git clone https://github.com/naver/pinpoint.git``` or [download](https://github.com/naver/pinpoint/archive/master.zip) the project as a zip file and unzip.

Install Pinpoint with maven by ```cd pinpoint``` and running ```mvn install -Dmaven.test.skip=true```

### Install & Start HBase
The following script downloads HBase standalone from [Apache download site](http://apache.mirror.cdnetworks.com/hbase/).

**Download & Start** - Run ```quickstart/bin/start-hbase.sh```

**Initialize Tables** - Run ```quickstart/bin/init-hbase.sh```

### Start Pinpoint Daemons

**Collector** - Run ```quickstart/bin/start-collector.sh```

**Web UI** - Run ```quickstart/bin/start-web.sh```

**TestApp** - Run ```quickstart/bin/start-testapp.sh```

### Check Status
Once HBase and the 3 daemons are running, you may visit the following addresses to test out your very own Pinpoint instance.

* Web UI - http://localhost:28080
* TestApp - http://localhost:28081

You can feed trace data to Pinpoint using the TestApp UI, and check them using Pinpoint Web UI. TestApp registers itself as *test-agent* under *TESTAPP*.


## Stopping

**HBase** - Run ```quickstart/bin/stop-hbase.sh```

**Collector** - Run ```quickstart/bin/stop-collector.sh```

**Web UI** - Run ```quickstart/bin/stop-web.sh```

**TestApp** - Run ```quickstart/bin/stop-testapp.sh```