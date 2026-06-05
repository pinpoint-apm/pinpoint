## Apache Pekko HTTP
* See: https://pekko.apache.org/docs/pekko-http/current/
* Range: org.apache.pekko/pekko-http-core_2.13 [1.0.0, )

### Pinpoint Configuration
pinpoint.config

#### HTTP Server options.
~~~
# HTTP server
profiler.pekko.http.enable=false
# original IP address header
profiler.pekko.http.realipheader=Remote-Address
# URLs to exclude from tracing
profiler.pekko.http.excludeurl=
# HTTP Request methods to exclude from tracing
profiler.pekko.http.excludemethod=
# Set transform target
# If you are using another directive, change below config
profiler.pekko.http.transform.targetname=org.apache.pekko.http.scaladsl.server.directives.BasicDirectives.$anonfun$mapRequestContext$2
profiler.pekko.http.transform.targetparameter=scala.Function1,scala.Function1,org.apache.pekko.http.scaladsl.server.RequestContext
~~~
