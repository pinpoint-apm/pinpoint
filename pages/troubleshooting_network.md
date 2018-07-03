---
title: Checking network configuration 
tags:
keywords: troubleshooting
last_updated: May 17, 2018
sidebar: mydoc_sidebar
permalink: troubleshooting_network.html
disqus: true
---

We provide a simple tool that can check your network configurations.  
This tool checks the network status between Pinpoint-Agent and Pinpoint-Collector

## Pinpoint from v1.7.3 and above

### Testing with binary release
 
 If you have downloaded the build results from our [**latest release**](https://github.com/naver/pinpoint/releases/latest). 

 1. Start your collector server
 2. With any terminal that you are using, go to *tools* folder which is under *pinpoint-agent-VERSION.tar.gz* package that you have downloaded.

````
> pwd
/Users/user/Downloads/pinpoint-agent-1.8.0/tools
````
and execute the command below.

````
> java -jar pinpoint-tools-VERSION.jar PATH_TO_CONFIG_FILE/pinpoint.config
```` 

Passed value(PATH_TO_CONFIG_FILE) should be the directory where your agent's pinpoint.config file is.
If the path was correct, you will see the results as below.
All six SUCCESSes mean that you are all set and ready to go. (In this case, collector server was started locally)

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

1. Start your collector server
2. Pass the *path* of the pinpoint.config file as a *program argument* and run ***NetworkAvailabilityChecker*** class.
Results should be same as shown above.

## Pinpoint up to v1.7.2

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
CLASSPATH=./tools/pinpoint-tools-1.7.2-SNAPSHOT.jar:./boot/pinpoint-commons-1.7.2-SNAPSHOT.jar:./boot/pinpoint-annotations-1.7.2-SNAPSHOT.jar
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
3. For the few who gets JNI error while running. Please remove ````<scope>provided</scope>```` line from pom.xml under *tools* module and try again

Results should be same as shown above.

