---
title: Quick Start Guide
keywords: start, begin, quickstart, quick
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: quickstart.html
disqus: false
---

# QuickStart
Pinpoint QuickStart provides a sample TestApp for the Agent.

## Docker
Installing Pinpoint with these docker files will take approximately 10min.

Visit [Official Pinpoint-Docker repository](https://github.com/pinpoint-apm/pinpoint-docker) for more information.

## Installation
To set up your very own Pinpoint instance you can either **download the build results** from our [**latest release**](https://github.com/pinpoint-apm/pinpoint/releases/latest).

### HBase
Download, Configure, and Start HBase - [1. Hbase](https://pinpoint-apm.github.io/pinpoint/installation.html#1-hbase).

~~~
$ tar xzvf hbase-x.x.x-bin.tar.gz
$ cd hbase-x.x.x/
$ ./bin/start-hbase.sh
~~~

See [scripts](https://github.com/pinpoint-apm/pinpoint/tree/master/hbase/scripts) and Run.

~~~
$ ./bin/hbase shell hbase-create.hbase
~~~

### Pinpoint Collector
Download, and Start Collector - [3. Pinpoint Collector](https://pinpoint-apm.github.io/pinpoint/installation.html#3-pinpoint-collector)

~~~
$ java -jar -Dpinpoint.zookeeper.address=localhost pinpoint-collector-boot-2.2.2.jar
~~~

### Pinpoint Web
Download, and Start Web - [4. Pinpoint Web](https://pinpoint-apm.github.io/pinpoint/installation.html#4-pinpoint-web)

~~~
$ java -jar -Dpinpoint.zookeeper.address=localhost pinpoint-web-boot-2.2.2.jar
~~~

## Java Agent

### Requirements
In order to build Pinpoint, the following requirements must be met:

* JDK 8 installed

### When Using Released Binary(Recommended) 
Download Pinpoint from [Latest Release](https://github.com/pinpoint-apm/pinpoint/releases/latest).

Extract the downloaded file.
~~~
$ tar xvzf pinpoint-agent-2.2.2.tar.gz
~~~

Run the JAR file, as follows:
~~~
$ java -jar -javaagent:pinpoint-agent-2.2.2/pinpoint-bootstrap.jar -Dpinpoint.agentId=test-agent -Dpinpoint.applicationName=TESTAPP pinpoint-quickstart-testapp-2.2.2.jar
~~~

### When Building Manually
Download Pinpoint with `git clone https://github.com/pinpoint-apm/pinpoint.git` or [download](https://github.com/pinpoint-apm/pinpoint/archive/master.zip) the project as a zip file and unzip.

Change to the pinpoint directory, and build.
~~~
$ cd pinpoint
$ ./mvnw install -DskipTests=true 
~~~

Change to the quickstart testapp directory, and build.
Let's build and run.
~~~
$ cd quickstart/testapp
$ ./mvnw clean package
~~~

Change to the pinpoint directory, and run.
~~~
$ cd ../../
$ java -jar -javaagent:agent/target/pinpoint-agent-2.2.2/pinpoint-bootstrap.jar -Dpinpoint.agentId=test-agent -Dpinpoint.applicationName=TESTAPP quickstart/testapp/target/pinpoint-quickstart-testapp-2.2.2.jar
~~~

### Get Started
You should see some output that looks very similar to this:
~~~

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.3.2.RELEASE)

2020-08-06 17:24:59.519  INFO 19236 --- [           main] com.navercorp.pinpoint.testapp.TestApp   : Starting TestApp on AD01160256 with PID 19236 (C:\repository\github\pinpoint\quickstart\testapp\target\classes started by Naver in C:\repository\github\pinpoint)
2020-08-06 17:24:59.520  INFO 19236 --- [           main] com.navercorp.pinpoint.testapp.TestApp   : No active profile set, falling back to default profiles: default
2020-08-06 17:24:59.520 DEBUG 19236 --- [           main] o.s.boot.SpringApplication               : Loading source class com.navercorp.pinpoint.testapp.TestApp
2020-08-06 17:24:59.558 DEBUG 19236 --- [           main] o.s.b.c.c.ConfigFileApplicationListener  : Loaded config file 'file:/C:/repository/github/pinpoint/quickstart/testapp/target/classes/application.yml' (classpath:/application.yml)
2020-08-06 17:24:59.558 DEBUG 19236 --- [           main] ConfigServletWebServerApplicationContext : Refreshing org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@46185a1b
08-06 17:24:59.059 [           main] INFO  .n.p.p.DefaultDynamicTransformerRegistry:67  -- added dynamic transformer classLoader: sun.misc.Launcher$AppClassLoader@18b4aac2, className: com.navercorp.pinpoint.testapp.controller.ApisController, registry size: 1
08-06 17:24:59.059 [           main] INFO  .n.p.p.DefaultDynamicTransformerRegistry:67  -- added dynamic transformer classLoader: sun.misc.Launcher$AppClassLoader@18b4aac2, className: com.navercorp.pinpoint.testapp.controller.CallSelfController, registry size: 2
08-06 17:24:59.059 [           main] INFO  .n.p.p.DefaultDynamicTransformerRegistry:67  -- added dynamic transformer classLoader: sun.misc.Launcher$AppClassLoader@18b4aac2, className: com.navercorp.pinpoint.testapp.controller.HttpClient4Controller, registry size: 3
08-06 17:24:59.059 [           main] INFO  .n.p.p.DefaultDynamicTransformerRegistry:67  -- added dynamic transformer classLoader: sun.misc.Launcher$AppClassLoader@18b4aac2, className: com.navercorp.pinpoint.testapp.controller.SimpleController, registry size: 4
08-06 17:24:59.059 [           main] INFO  .n.p.p.DefaultDynamicTransformerRegistry:67  -- added dynamic transformer classLoader: sun.misc.Launcher$AppClassLoader@18b4aac2, className: com.navercorp.pinpoint.testapp.controller.StressController, registry size: 5
2020-08-06 17:25:00.313 DEBUG 19236 --- [           main] .s.b.w.e.t.TomcatServletWebServerFactory : Code archive: C:\Users\Naver\.m2\repository\org\springframework\boot\spring-boot\2.3.2.RELEASE\spring-boot-2.3.2.RELEASE.jar
2020-08-06 17:25:00.313 DEBUG 19236 --- [           main] .s.b.w.e.t.TomcatServletWebServerFactory : Code archive: C:\Users\Naver\.m2\repository\org\springframework\boot\spring-boot\2.3.2.RELEASE\spring-boot-2.3.2.RELEASE.jar
2020-08-06 17:25:00.314 DEBUG 19236 --- [           main] .s.b.w.e.t.TomcatServletWebServerFactory : None of the document roots [src/main/webapp, public, static] point to a directory and will be ignored.
2020-08-06 17:25:00.347  INFO 19236 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8082 (http)
2020-08-06 17:25:00.355  INFO 19236 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2020-08-06 17:25:00.356  INFO 19236 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.37]
~~~

The last couple of lines here tell us that Spring has started. Spring Boot’s embedded Apache Tomcat server is acting as a webserver and is listening for requests on localhost port 8082. Open your browser and in the address bar at the top enter http://localhost:8082
 

