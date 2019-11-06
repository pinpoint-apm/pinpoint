## Undertow Web Server
* Version: 1.1
* Since: Pinpoint 1.8.0
* See: http://undertow.io
* See: [WildFly Application Server - Web Server](http://wildfly.org/about)
* See: [Spring Boot - Servlet Containers](https://spring.io/projects/spring-boot)
* Range: io.undertow/undertow-core [2.0.0.Final, 2.0.16.Final] (2.0.0.Final <= x <= 2.0.16.Final)

### Pinpoint Configuration
pinpoint.config

#### Set enable options.
~~~
profiler.undertow.enable=true

# Since pinpoint-1.8.2
# Servlet deployment support(e.g. Spring Boot Undertow, Wildfly)
# If it is bootstrapped Undertow, set it to false.
profiler.undertow.deploy.servlet=true

# trace param in request  ,default value is true
profiler.undertow.tracerequestparam=true
# Hide pinpoint headers.
profiler.undertow.hidepinpointheader=true
# URLs to exclude from tracing.
# Support ant style pattern. e.g. /aa/*.html, /??/exclude.html
profiler.undertow.excludeurl=
# HTTP Request methods to exclude from tracing
#profiler.undertow.excludemethod=

# original IP address header
# https://en.wikipedia.org/wiki/X-Forwarded-For
#profiler.undertow.realipheader=X-Forwarded-For
# nginx real ip header
#profiler.undertow.realipheader=X-Real-IP
# optional parameter, If the header value is ${profiler.undertow.realipemptyvalue}, Ignore header value.
#profiler.undertow.realipemptyvalue=unknown
~~~

### Web Server
* Spring Boot starter
* WildFly Application Server

