## Openwhisk
* Since: Pinpoint 1.8.0
* See: https://openwhisk.apache.org/
* Range: internal.com.apache.openwhisk/openwhisk-common [2.0, 2.7]

### Pinpoint Configuration
pinpoint.config

#### Transform target options.
~~~
profiler.openwhisk.enable=false
profiler.openwhisk.logging.message=false
profiler.openwhisk.transform.targetname=org.apache.openwhisk.http.BasicHttpService.$anonfun$assignId$2
profiler.openwhisk.transform.targetparameter=org.apache.openwhisk.http.BasicHttpService,boolean,akka.http.scaladsl.server.RequestContext
~~~
