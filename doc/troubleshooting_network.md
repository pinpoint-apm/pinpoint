---
title: Checking network configuration 
keywords: troubleshooting
last_updated: Aug 14, 2018
sidebar: mydoc_sidebar
permalink: troubleshooting_network.html
disqus: false
---

We provide a simple tool that can check your network configurations.  
This tool checks the network status between Pinpoint-Agent and Pinpoint-Collector

### Testing with binary release

If you have downloaded the build results from our [**latest release**](https://github.com/naver/pinpoint/releases/latest). 

1. Start your collector server
2. With any terminal that you are using, go to *script* folder which is under *pinpoint-agent-VERSION.tar.gz* package that you have downloaded.

````
> pwd
/Users/user/Downloads/pinpoint-agent-1.7.2-SNAPSHOT/script
````
and run *networktest.sh* shell script
````
> sh networktest.sh
````

You will see some CLASSPATH and configuration you have made in the *pinpoint.config* file as below
````
CLASSPATH=./tools/pinpoint-tools-1.7.2-SNAPSHOT.jar
...Remainder Omitted...
2018-04-10 17:36:30 [INFO ](com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig) profiler.enable=true
2018-04-10 17:36:30 [INFO ](com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig) profiler.instrument.engine=ASM
2018-04-10 17:36:30 [INFO ](com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig) profiler.instrument.matcher.enable=true
...Remainder Omitted...
````

And after that, you will see the results. (In this case, collector server was started locally)
If you receive all six SUCCESSes as below, then you are all set and ready to go.

````
UDP-STAT:// localhost
    => 127.0.0.1:9995 [SUCCESS]
    => 0:0:0:0:0:0:0:1:9995 [SUCCESS]

UDP-SPAN:// localhost
    => 127.0.0.1:9996 [SUCCESS]
    => 0:0:0:0:0:0:0:1:9996 [SUCCESS]

TCP:// localhost
    => 127.0.0.1:9994 [SUCCESS]
    => 0:0:0:0:0:0:0:1:9994 [SUCCESS]
```` 

### Testing with source code

The idea is basically the same. 

1. Start your collector server
2. Pass the *path* of the pinpoint.config file as a *program argument* and run ***NetworkAvailabilityChecker*** class.
3. (only for under v1.7.2)For the few who gets JNI error while running. Please remove ````<scope>provided</scope>```` line from pom.xml under *tools* module and try again

Results should be same as shown above.

 >  If you face error for v1.7.3 take a look at this [issue](https://github.com/naver/pinpoint/issues/4668)