## Tomcat Web Server
* Since: Pinpoint 1.5.0
* See: https://tomcat.apache.org/
* Range: [8, 10]

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
profiler.tomcat.enable=true

# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.tomcat.bootstrap.main=org.apache.catalina.startup.Bootstrap
# Hide pinpoint headers.
profiler.tomcat.hidepinpointheader=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.tomcat.excludeurl=
# HTTP Request methods to exclude from tracing
#profiler.tomcat.excludemethod=POST,PUT
profiler.tomcat.tracerequestparam=true

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.tomcat.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.tomcat.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.tomcat.realipemptyvalue}, Ignore header value.
#profiler.tomcat.realipemptyvalue=unknown
~~~

### Web Server
* Spring Boot starter


