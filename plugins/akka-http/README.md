## Akka HTTP 
* Since: Pinpoint 1.8.0
* See: https://github.com/akka/akka-http
* Range: com.typesafe.akka/akka-http-core_2.12 [10.1.0, 10.4]

### Pinpoint Configuration
pinpoint.config

#### HTTP Server options.
~~~
# HTTP server
profiler.akka.http.enable=false
# original IP address header
profiler.akka.http.realipheader=Remote-Address
# URLs to exclude from tracing
profiler.akka.http.excludeurl=
# HTTP Request methods to exclude from tracing
profiler.akka.http.excludemethod=
# Set transform target
# If you are using another directive, change below config
profiler.akka.http.transform.targetname=akka.http.scaladsl.server.directives.BasicDirectives.$anonfun$mapRequestContext$2
profiler.akka.http.transform.targetparameter=scala.Function1,scala.Function1,akka.http.scaladsl.server.RequestContext
~~~
